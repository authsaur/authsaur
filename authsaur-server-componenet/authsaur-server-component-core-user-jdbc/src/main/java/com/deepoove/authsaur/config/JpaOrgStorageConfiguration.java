package com.deepoove.authsaur.config;

import com.deepoove.authsaur.jpa.JpaOrgEntity;
import com.deepoove.authsaur.jpa.JpaOrgRegistry;
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
public class JpaOrgStorageConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaOrgPersistenceProviderConfigurer")
    public JpaPersistenceProviderConfigurer jpaOrgPersistenceProviderConfigurer() {
        return context -> {
            val entities = CollectionUtils.wrapList(JpaOrgEntity.class.getName());
            context.getIncludeEntityClasses().addAll(entities);
        };
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public JpaVendorAdapter jpaOrgVendorAdapter(final CasConfigurationProperties casProperties,
                                                @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceProvider jpaOrgPersistenceProvider(final CasConfigurationProperties casProperties,
                                                         @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newPersistenceProvider(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaOrgPackagesToScan")
    public BeanContainer<String> jpaOrgPackagesToScan() {
        return BeanContainer.of(CollectionUtils.wrapSet(JpaOrgEntity.class.getPackage().getName()));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean orgEntityManagerFactory(final CasConfigurationProperties casProperties, @Qualifier("dataSourceOrg") final DataSource dataSourceOrg, @Qualifier("jpaOrgVendorAdapter") final JpaVendorAdapter jpaOrgVendorAdapter, @Qualifier("jpaOrgPersistenceProvider") final PersistenceProvider jpaOrgPersistenceProvider, @Qualifier("jpaOrgPackagesToScan") final BeanContainer<String> jpaOrgPackagesToScan, @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        val ctx = JpaConfigurationContext.builder().dataSource(dataSourceOrg).persistenceUnitName(
                "jpaOrgRegistryContext").jpaVendorAdapter(jpaOrgVendorAdapter).persistenceProvider(
                jpaOrgPersistenceProvider).packagesToScan(jpaOrgPackagesToScan.toSet()).build();
        return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    public PlatformTransactionManager transactionManagerOrg(@Qualifier("orgEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @ConditionalOnMissingBean(name = "jdbcOrgTransactionTemplate")
    @Bean
    public TransactionTemplate jdbcOrgTransactionTemplate(final CasConfigurationProperties casProperties,
                                                          final ConfigurableApplicationContext applicationContext) {
        val t = new TransactionTemplate(applicationContext.getBean(JpaOrgRegistry.BEAN_NAME_TRANSACTION_MANAGER,
                PlatformTransactionManager.class));
        t.setIsolationLevelName(casProperties.getAuthn().getJdbc().getQuery().get(0).getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAuthn().getJdbc().getQuery().get(0).getPropagationBehaviorName());
        return t;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceOrg")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSourceOrg(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getJdbc().getQuery().get(0));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jpaOrgRegistry")
    public JpaOrgRegistry jpaOrgRegistry(@Qualifier("jdbcOrgTransactionTemplate") final TransactionTemplate jdbcOrgTransactionTemplate) {
        return new JpaOrgRegistry(jdbcOrgTransactionTemplate);
    }
}
