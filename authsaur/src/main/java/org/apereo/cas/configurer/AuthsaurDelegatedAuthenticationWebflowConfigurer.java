package org.apereo.cas.configurer;

import lombok.val;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Map;

public class AuthsaurDelegatedAuthenticationWebflowConfigurer extends DelegatedAuthenticationWebflowConfigurer {

    private static final String DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE = "checkDelegatedAuthnFailureDecision";

    public AuthsaurDelegatedAuthenticationWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                                            FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                            FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                            ConfigurableApplicationContext applicationContext,
                                                            CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, logoutFlowDefinitionRegistry, applicationContext,
                casProperties);
    }

    protected void createStopWebflowViewState(final Flow flow) {
        createDecisionState(flow, DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE,
                "flowScope.unauthorizedRedirectUrl != null",
                CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);

        val stopWebflowState = createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW,
                CasWebflowConstants.VIEW_ID_PAC4J_STOP_WEBFLOW);
        stopWebflowState.getEntryActionList().add(new BaseCasWebflowAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) {
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                val mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request, response.getStatus());
                mv.ifPresent(modelAndView -> modelAndView.getModel().forEach(
                        (k, v) -> requestContext.getFlowScope().put(k, v)));
                processError(requestContext);
                return null;
            }

            private void processError(RequestContext requestContext) {
                AttributeMap<Object> attributes = requestContext.getCurrentEvent().getAttributes();
                Object error = attributes.get("error");
                requestContext.getFlowScope().put("rootCauseException", error);
                if (error instanceof AuthenticationException) {
                    Map<String, Throwable> handlerErrors = ((AuthenticationException) error).getHandlerErrors();
                    Collection<Throwable> values = handlerErrors.values();
                    if (!values.isEmpty()) {
                        Throwable o = values.toArray(new Throwable[0])[0];
                        if (o instanceof PreventedException) {
                            requestContext.getFlowScope().put("rootCauseException", o.getCause());
                        }
                    }
                }
            }
        });
        createTransitionForState(stopWebflowState, CasWebflowConstants.TRANSITION_ID_RETRY,
                CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY);
        val retryState = createEndState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY);
        retryState.setFinalResponseAction(
                createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY));
    }
}
