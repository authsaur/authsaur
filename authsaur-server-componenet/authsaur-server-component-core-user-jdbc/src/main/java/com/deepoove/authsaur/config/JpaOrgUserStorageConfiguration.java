package com.deepoove.authsaur.config;

import com.deepoove.authsaur.jpa.JpaOrgRegistry;
import com.deepoove.authsaur.jpa.JpaOrgUserEntity;
import com.deepoove.authsaur.jpa.JpaOrgUserRegistry;
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

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaOrgUserStorageConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaOrgUserPersistenceProviderConfigurer")
    public JpaPersistenceProviderConfigurer jpaOrgUserPersistenceProviderConfigurer() {
        return context -> {
            val entities = CollectionUtils.wrapList(JpaOrgUserEntity.class.getName());
            context.getIncludeEntityClasses().addAll(entities);
        };
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public JpaVendorAdapter jpaOrgUserVendorAdapter(final CasConfigurationProperties casProperties,
                                                    @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceProvider jpaOrgUserPersistenceProvider(final CasConfigurationProperties casProperties,
                                                             @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newPersistenceProvider(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaOrgUserPackagesToScan")
    public BeanContainer<String> jpaOrgUserPackagesToScan() {
        return BeanContainer.of(CollectionUtils.wrapSet(JpaOrgUserEntity.class.getPackage().getName()));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean orgUserEntityManagerFactory(final CasConfigurationProperties casProperties, @Qualifier("dataSourceOrgUser") final DataSource dataSourceOrgUser, @Qualifier("jpaOrgUserVendorAdapter") final JpaVendorAdapter jpaOrgUserVendorAdapter, @Qualifier("jpaOrgUserPersistenceProvider") final PersistenceProvider jpaOrgUserPersistenceProvider, @Qualifier("jpaOrgUserPackagesToScan") final BeanContainer<String> jpaOrgUserPackagesToScan, @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        val ctx = JpaConfigurationContext.builder().dataSource(dataSourceOrgUser).persistenceUnitName(
                "jpaOrgUserRegistryContext").jpaVendorAdapter(jpaOrgUserVendorAdapter).persistenceProvider(
                jpaOrgUserPersistenceProvider).packagesToScan(jpaOrgUserPackagesToScan.toSet()).build();
        return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    public PlatformTransactionManager transactionManagerOrgUser(@Qualifier("orgUserEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @ConditionalOnMissingBean(name = "jdbcOrgUserTransactionTemplate")
    @Bean
    public TransactionTemplate jdbcOrgUserTransactionTemplate(final CasConfigurationProperties casProperties,
                                                              final ConfigurableApplicationContext applicationContext) {
        val t = new TransactionTemplate(applicationContext.getBean(JpaOrgUserRegistry.BEAN_NAME_TRANSACTION_MANAGER,
                PlatformTransactionManager.class));
        t.setIsolationLevelName(casProperties.getAuthn().getJdbc().getQuery().get(0).getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAuthn().getJdbc().getQuery().get(0).getPropagationBehaviorName());
        return t;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceOrgUser")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSourceOrgUser(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaOrgUserRegistry")
    public JpaOrgUserRegistry jpaOrgUserRegistry(@Qualifier("jdbcOrgUserTransactionTemplate") final TransactionTemplate jdbcOrgUserTransactionTemplate, final JpaOrgRegistry jpaOrgRegistry) {
        return new JpaOrgUserRegistry(jdbcOrgUserTransactionTemplate, jpaOrgRegistry);
    }
}
