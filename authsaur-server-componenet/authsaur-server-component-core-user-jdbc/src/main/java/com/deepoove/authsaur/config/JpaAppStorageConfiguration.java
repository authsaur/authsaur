package com.deepoove.authsaur.config;

import com.deepoove.authsaur.jpa.JpaAppRegistry;
import lombok.val;
import com.deepoove.authsaur.jpa.JpaAppEntity;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.BeanContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaAppStorageConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaAppPersistenceProviderConfigurer")
    public JpaPersistenceProviderConfigurer jpaAppPersistenceProviderConfigurer() {
        return context -> {
            val entities = CollectionUtils.wrapList(JpaAppEntity.class.getName());
            context.getIncludeEntityClasses().addAll(entities);
        };
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public JpaVendorAdapter jpaAppVendorAdapter(final CasConfigurationProperties casProperties,
                                                @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceProvider jpaAppPersistenceProvider(final CasConfigurationProperties casProperties,
                                                         @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newPersistenceProvider(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaAppPackagesToScan")
    public BeanContainer<String> jpaAppPackagesToScan() {
        return BeanContainer.of(CollectionUtils.wrapSet(JpaAppEntity.class.getPackage().getName()));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean appEntityManagerFactory(final CasConfigurationProperties casProperties, @Qualifier("dataSourceApp") final DataSource dataSourceApp, @Qualifier("jpaAppVendorAdapter") final JpaVendorAdapter jpaAppVendorAdapter, @Qualifier("jpaAppPersistenceProvider") final PersistenceProvider jpaAppPersistenceProvider, @Qualifier("jpaAppPackagesToScan") final BeanContainer<String> jpaAppPackagesToScan, @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        val ctx = JpaConfigurationContext.builder().dataSource(dataSourceApp).persistenceUnitName(
                "jpaAppRegistryContext").jpaVendorAdapter(jpaAppVendorAdapter).persistenceProvider(
                jpaAppPersistenceProvider).packagesToScan(jpaAppPackagesToScan.toSet()).build();
        return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    public PlatformTransactionManager transactionManagerApp(@Qualifier("appEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @ConditionalOnMissingBean(name = "jdbcAppTransactionTemplate")
    @Bean
    public TransactionTemplate jdbcAppTransactionTemplate(final CasConfigurationProperties casProperties,
                                                          final ConfigurableApplicationContext applicationContext) {
        val t = new TransactionTemplate(applicationContext.getBean(JpaAppRegistry.BEAN_NAME_TRANSACTION_MANAGER,
                PlatformTransactionManager.class));
        t.setIsolationLevelName(casProperties.getAuthn().getJdbc().getQuery().get(0).getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAuthn().getJdbc().getQuery().get(0).getPropagationBehaviorName());
        return t;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceApp")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSourceApp(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaAppRegistry")
    public JpaAppRegistry jpaAppRegistry(@Qualifier("jdbcAppTransactionTemplate") final TransactionTemplate jdbcAppTransactionTemplate) {
        return new JpaAppRegistry(jdbcAppTransactionTemplate);
    }
}
