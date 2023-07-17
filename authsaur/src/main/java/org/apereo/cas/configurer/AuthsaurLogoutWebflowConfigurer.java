package org.apereo.cas.configurer;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.action.ViewFactoryActionAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class AuthsaurLogoutWebflowConfigurer extends DefaultLogoutWebflowConfigurer {

    public AuthsaurLogoutWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                           FlowDefinitionRegistry flowDefinitionRegistry,
                                           ConfigurableApplicationContext applicationContext,
                                           CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        super.doInitialize();

        val flow = getLogoutFlow();

        if (flow != null) {
            EndState endState = (EndState) flow.getState(CasWebflowConstants.STATE_ID_LOGOUT_VIEW);
            endState.setFinalResponseAction(
                    new ViewFactoryActionAdapter(
                            createExternalRedirectViewFactory("'contextRelative:login'")));
        }
    }
}
