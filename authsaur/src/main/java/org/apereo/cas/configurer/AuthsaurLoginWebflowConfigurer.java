package org.apereo.cas.configurer;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.action.ViewFactoryActionAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class AuthsaurLoginWebflowConfigurer extends DefaultLoginWebflowConfigurer {

    public AuthsaurLoginWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                          FlowDefinitionRegistry flowDefinitionRegistry,
                                          ConfigurableApplicationContext applicationContext,
                                          CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        super.doInitialize();

        val flow = getLoginFlow();

        if (flow != null) {
            customInitialFlowActions(flow);
        }
    }

    private void customInitialFlowActions(Flow flow) {
        val startActionList = flow.getStartActionList();
        startActionList.add(createEvaluateAction("prepareLoginSetupAction"));

        EndState endState = (EndState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS);
        endState.setFinalResponseAction(
                new ViewFactoryActionAdapter(
                        createExternalRedirectViewFactory("'contextRelative:app'")));
    }


}
