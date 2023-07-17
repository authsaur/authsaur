package com.deepoove.authsaur.config;

import com.deepoove.authsaur.jpa.JpaOrgRegistry;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import com.deepoove.authsaur.jpa.JpaUserEntity;
import com.deepoove.authsaur.jpa.JpaUserRegistry;
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
public class JpaUserStorageConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaUserPersistenceProviderConfigurer")
    public JpaPersistenceProviderConfigurer jpaUserPersistenceProviderConfigurer() {
        return context -> {
            val entities = CollectionUtils.wrapList(JpaUserEntity.class.getName());
            context.getIncludeEntityClasses().addAll(entities);
        };
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public JpaVendorAdapter jpaUserVendorAdapter(final CasConfigurationProperties casProperties,
                                                 @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceProvider jpaUserPersistenceProvider(final CasConfigurationProperties casProperties,
                                                          @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newPersistenceProvider(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaUserPackagesToScan")
    public BeanContainer<String> jpaUserPackagesToScan() {
        return BeanContainer.of(CollectionUtils.wrapSet(JpaUserEntity.class.getPackage().getName()));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean userEntityManagerFactory(final CasConfigurationProperties casProperties, @Qualifier("dataSourceUser") final DataSource dataSourceUser, @Qualifier("jpaUserVendorAdapter") final JpaVendorAdapter jpaUserVendorAdapter, @Qualifier("jpaUserPersistenceProvider") final PersistenceProvider jpaUserPersistenceProvider, @Qualifier("jpaUserPackagesToScan") final BeanContainer<String> jpaUserPackagesToScan, @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        val ctx = JpaConfigurationContext.builder().dataSource(dataSourceUser).persistenceUnitName(
                "jpaUserRegistryContext").jpaVendorAdapter(jpaUserVendorAdapter).persistenceProvider(
                jpaUserPersistenceProvider).packagesToScan(jpaUserPackagesToScan.toSet()).build();
        return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    public PlatformTransactionManager transactionManagerUser(@Qualifier("userEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @ConditionalOnMissingBean(name = "jdbcUserTransactionTemplate")
    @Bean
    public TransactionTemplate jdbcUserTransactionTemplate(final CasConfigurationProperties casProperties,
                                                           final ConfigurableApplicationContext applicationContext) {
        val t = new TransactionTemplate(applicationContext.getBean(JpaUserRegistry.BEAN_NAME_TRANSACTION_MANAGER,
                PlatformTransactionManager.class));
        t.setIsolationLevelName(casProperties.getAuthn().getJdbc().getQuery().get(0).getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAuthn().getJdbc().getQuery().get(0).getPropagationBehaviorName());
        return t;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceUser")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSourceUser(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaUserRegistry")
    public JpaUserRegistry jpaUserRegistry(@Qualifier("jdbcUserTransactionTemplate") final TransactionTemplate jdbcUserTransactionTemplate,
                                           final JpaOrgRegistry jpaOrgRegistry) {
        return new JpaUserRegistry(jdbcUserTransactionTemplate, jpaOrgRegistry);
    }
}
