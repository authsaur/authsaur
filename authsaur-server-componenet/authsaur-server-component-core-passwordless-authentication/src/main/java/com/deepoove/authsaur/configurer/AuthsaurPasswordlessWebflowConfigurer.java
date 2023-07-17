package com.deepoove.authsaur.configurer;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;


public class AuthsaurPasswordlessWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public AuthsaurPasswordlessWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                 final ConfigurableApplicationContext applicationContext,
                                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createStateInitialPasswordless(flow);
        }
    }

    private void createStateInitialPasswordless(Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);

        // send verify code
        createTransitionForState(state, "verifyCode", "verifyCodeId");
        ActionState verifyState = createActionState(flow, "verifyCodeId",
                CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN);
        val submission = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        val transition = (Transition) submission.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        val targetStateId = transition.getTargetStateId();
        createTransitionForState(verifyState, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetStateId);
//            createTransitionForState(verifyState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
//                    CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
        createTransitionForState(verifyState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }
}
