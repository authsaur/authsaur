package com.deepoove.authsaur.configurer;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class UserWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public static final String FLOW_ID = "user";

    private final FlowDefinitionRegistry loginFlowRegistry;

    public UserWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                 final FlowDefinitionRegistry mainFlowDefinitionRegistry,
                                 final FlowDefinitionRegistry loginFlowRegistry,
                                 final ConfigurableApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder());
        this.loginFlowRegistry = loginFlowRegistry;
    }

    @Override
    protected void doInitialize() {
        val userFlow = buildFlow(FLOW_ID);
        userFlow.getStartActionList().add(createEvaluateAction("fetchTGTAction"));

        val myAccountView = createViewState(userFlow, "boxAccountUser", "boxAcct/casMyUser");
        myAccountView.getRenderActionList().add(createEvaluateAction("prepareAccountAppViewAction"));

        val validate = createActionState(userFlow, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
                CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK);
        createTransitionForState(validate, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID,
                myAccountView.getId());
        createStateDefaultTransition(validate, CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN);

        val view = createExternalRedirectViewFactory(String.format("'%s'", casProperties.getServer().getLoginUrl()));
        createEndState(userFlow, CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN, view);

        userFlow.setStartState(validate);
        mainFlowDefinitionRegistry.registerFlowDefinition(userFlow);
    }

}
