package com.deepoove.authsaur.config;

import com.deepoove.authsaur.resolvers.AuthsaurEchoingPrincipalResolver;
import com.deepoove.authsaur.person.PersonManager;
import com.deepoove.authsaur.resolvers.RelatedPersonDirectoryPrincipalResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import com.deepoove.authsaur.setting.PrincipalNamedPolicy;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import org.apereo.cas.authentication.principal.DefaultPrincipalResolutionExecutionPlan;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.*;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.jpa.JpaOrgRegistry;
import com.deepoove.authsaur.jpa.JpaOrgUserRegistry;
import com.deepoove.authsaur.jpa.JpaUserRegistry;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

@Configuration
@AutoConfigureBefore({CasCoreAuthenticationPrincipalConfiguration.class})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class AuthsaurRelatedPrincipalConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean("relatedPersonDirectoryPrincipalResolver")
    public PrincipalResolver relatedPersonDirectoryPrincipalResolver(
            final CasConfigurationProperties casProperties,
            final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry,
            final JpaUserRegistry jpaUserRegistry,
            final PersonManager personManager,
            @Qualifier("personDirectoryPrincipalFactory") final PrincipalFactory personDirectoryPrincipalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY) final IPersonAttributeDao attributeRepository) {
        val personDirectory = casProperties.getPersonDirectory();
        val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(
                casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
        RelatedPersonDirectoryPrincipalResolver principalResolver =
                CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(
                        personDirectoryPrincipalFactory,
                        attributeRepository, attributeMerger, RelatedPersonDirectoryPrincipalResolver.class,
                        personDirectory);
        principalResolver.setCasProperties(casProperties);
        principalResolver.setJpaAuthenticationHandlerRegistry(jpaAuthenticationHandlerRegistry);
        principalResolver.setJpaUserRegistry(jpaUserRegistry);
        principalResolver.setPersonManager(personManager);
        return principalResolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalResolver defaultPrincipalResolver(
            @Qualifier("relatedPersonDirectoryPrincipalResolver") PrincipalResolver relatedPersonDirectoryPrincipalResolver,
            final ObjectProvider<List<PrincipalResolutionExecutionPlanConfigurer>> configurers,
            final CasConfigurationProperties casProperties,
            final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry,
            final PersonManager personManager,
            @Qualifier(PrincipalElectionStrategy.BEAN_NAME) final PrincipalElectionStrategy principalElectionStrategy) {
        val plan = new DefaultPrincipalResolutionExecutionPlan();
        plan.registerPrincipalResolver(relatedPersonDirectoryPrincipalResolver);
        plan.registerPrincipalResolver(
                new AuthsaurEchoingPrincipalResolver(jpaAuthenticationHandlerRegistry, personManager));

        val registeredPrincipalResolvers = plan.getRegisteredPrincipalResolvers();
        val resolver = new ChainingPrincipalResolver(principalElectionStrategy, casProperties);
        resolver.setChain(registeredPrincipalResolvers);
        return resolver;
    }

    @Bean
    public PersonManager personManager(final CasConfigurationProperties casProperties,
                                       final JpaUserRegistry jpaUserRegistry,
                                       final JpaOrgUserRegistry jpaOrgUserRegistry,
                                       final JpaOrgRegistry jpaOrgRegistry,
                                       PrincipalNamedPolicy principalNamedPolicy) {
        return new PersonManager(casProperties, jpaUserRegistry,
                jpaOrgUserRegistry, jpaOrgRegistry, principalNamedPolicy);
    }



}

