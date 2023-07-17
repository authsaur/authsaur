package com.deepoove.authsaur.config;

import lombok.val;
import com.deepoove.authsaur.jpa.JpaAppConfigEntity;
import com.deepoove.authsaur.jpa.JpaAppConfigRegistry;
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
public class JpaAppConfigStorageConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaAppConfigPersistenceProviderConfigurer")
    public JpaPersistenceProviderConfigurer jpaAppConfigPersistenceProviderConfigurer() {
        return context -> {
            val entities = CollectionUtils.wrapList(JpaAppConfigEntity.class.getName());
            context.getIncludeEntityClasses().addAll(entities);
        };
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public JpaVendorAdapter jpaAppConfigVendorAdapter(final CasConfigurationProperties casProperties,
                                                      @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceProvider jpaAppConfigPersistenceProvider(final CasConfigurationProperties casProperties,
                                                               @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newPersistenceProvider(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaAppConfigPackagesToScan")
    public BeanContainer<String> jpaAppConfigPackagesToScan() {
        return BeanContainer.of(CollectionUtils.wrapSet(JpaAppConfigEntity.class.getPackage().getName()));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean appConfigEntityManagerFactory(final CasConfigurationProperties casProperties, @Qualifier("dataSourceAppConfig") final DataSource dataSourceAppConfig, @Qualifier("jpaAppConfigVendorAdapter") final JpaVendorAdapter jpaAppConfigVendorAdapter, @Qualifier("jpaAppConfigPersistenceProvider") final PersistenceProvider jpaAppConfigPersistenceProvider, @Qualifier("jpaAppConfigPackagesToScan") final BeanContainer<String> jpaAppConfigPackagesToScan, @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        val ctx = JpaConfigurationContext.builder().dataSource(dataSourceAppConfig).persistenceUnitName(
                "jpaAppConfigRegistryContext").jpaVendorAdapter(jpaAppConfigVendorAdapter).persistenceProvider(
                jpaAppConfigPersistenceProvider).packagesToScan(jpaAppConfigPackagesToScan.toSet()).build();
        return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    public PlatformTransactionManager transactionManagerAppConfig(@Qualifier("appConfigEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @ConditionalOnMissingBean(name = "jdbcAppConfigTransactionTemplate")
    @Bean
    public TransactionTemplate jdbcAppConfigTransactionTemplate(final CasConfigurationProperties casProperties,
                                                                final ConfigurableApplicationContext applicationContext) {
        val t = new TransactionTemplate(applicationContext.getBean(JpaAppConfigRegistry.BEAN_NAME_TRANSACTION_MANAGER,
                PlatformTransactionManager.class));
        t.setIsolationLevelName(casProperties.getAuthn().getJdbc().getQuery().get(0).getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAuthn().getJdbc().getQuery().get(0).getPropagationBehaviorName());
        return t;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceAppConfig")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSourceAppConfig(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaAppConfigRegistry")
    public JpaAppConfigRegistry jpaAppConfigRegistry(@Qualifier("jdbcAppConfigTransactionTemplate") final TransactionTemplate jdbcAppConfigTransactionTemplate) {
        return new JpaAppConfigRegistry(jdbcAppConfigTransactionTemplate);
    }
}
