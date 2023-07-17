package com.deepoove.authsaur.config;

import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import lombok.val;
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

@Configuration(value = "JpaAuthenticationStorageConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthsaurJpaAuthenticationStorageConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaAuthPersistenceProviderConfigurer")
    public JpaPersistenceProviderConfigurer jpaAuthPersistenceProviderConfigurer() {
        return context -> {
            val entities = CollectionUtils.wrapList(JpaAuthenticationEntity.class.getName());
            context.getIncludeEntityClasses().addAll(entities);
        };
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public JpaVendorAdapter jpaAuthVendorAdapter(final CasConfigurationProperties casProperties,
                                                 @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceProvider jpaAuthPersistenceProvider(final CasConfigurationProperties casProperties,
                                                          @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newPersistenceProvider(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaAuthPackagesToScan")
    public BeanContainer<String> jpaAuthPackagesToScan() {
        return BeanContainer.of(CollectionUtils.wrapSet(JpaAuthenticationEntity.class.getPackage().getName()));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean authEntityManagerFactory(final CasConfigurationProperties casProperties, @Qualifier("dataSourceService") final DataSource dataSourceService, @Qualifier("jpaAuthVendorAdapter") final JpaVendorAdapter jpaAuthVendorAdapter, @Qualifier("jpaAuthPersistenceProvider") final PersistenceProvider jpaAuthPersistenceProvider, @Qualifier("jpaAuthPackagesToScan") final BeanContainer<String> jpaAuthPackagesToScan, @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        val ctx = JpaConfigurationContext.builder().dataSource(dataSourceService).persistenceUnitName(
                "jpaAuthRegistryContext").jpaVendorAdapter(jpaAuthVendorAdapter).persistenceProvider(
                jpaAuthPersistenceProvider).packagesToScan(jpaAuthPackagesToScan.toSet()).build();
        return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    public PlatformTransactionManager transactionManagerAuth(@Qualifier("authEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @ConditionalOnMissingBean(name = "jdbcAuthTransactionTemplate")
    @Bean
    public TransactionTemplate jdbcAuthTransactionTemplate(final CasConfigurationProperties casProperties,
                                                           final ConfigurableApplicationContext applicationContext) {
        val t = new TransactionTemplate(
                applicationContext.getBean(JpaAuthenticationHandlerRegistry.BEAN_NAME_TRANSACTION_MANAGER,
                        PlatformTransactionManager.class));
        t.setIsolationLevelName(casProperties.getAuthn().getJdbc().getQuery().get(0).getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAuthn().getJdbc().getQuery().get(0).getPropagationBehaviorName());
        return t;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceService")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSourceService(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaAuthenticationHandlerRegistry")
    public JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry(@Qualifier("jdbcAuthTransactionTemplate") final TransactionTemplate jdbcAuthTransactionTemplate) {
        return new JpaAuthenticationHandlerRegistry(jdbcAuthTransactionTemplate);
    }
}
