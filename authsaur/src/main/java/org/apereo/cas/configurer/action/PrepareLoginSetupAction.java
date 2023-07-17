package org.apereo.cas.configurer.action;

import com.deepoove.authsaur.pac4j.dingtalk.DingTalkClient;
import com.deepoove.authsaur.pac4j.dingtalkless.DingTalklessClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Getter
public class PrepareLoginSetupAction extends BaseCasWebflowAction {

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;
    private final CasConfigurationProperties casProperties;
    private final Clients clients;


    @Override
    public Event doExecute(final RequestContext context) {
        Set<String> list = new HashSet<>();
        if (StringUtils.isNotBlank(casProperties.getAuthn().getPasswordless().getTokens().getMail().getText()) ||
                StringUtils.isNotBlank(casProperties.getAuthn().getPasswordless().getTokens().getSms().getText())) {
            list.add("sms");
            list.add("form");
        }
        Set<AuthenticationHandler> authenticationHandlers = authenticationEventExecutionPlan.getAuthenticationHandlers();
        long count = authenticationHandlers.stream().filter(s -> (s.getState() == AuthenticationHandlerStates.ACTIVE && (s instanceof LdapAuthenticationHandler || s.getName().equals("-1024")))).count();
        if (count > 0) {
            list.add("pwd");
            list.add("form");
        }

        List<Client> allClients = clients.findAllClients();
        allClients.stream().forEach(c -> {
            if (c instanceof DingTalkClient || c instanceof DingTalklessClient) {
                list.add("qr");
                if (c instanceof DingTalklessClient) {
                    context.getFlowScope().put("dingtalkCorpId",
                            ((DingTalklessClient) c).getCropId());
                    context.getFlowScope().put("dingtalkLessName",
                            c.getName());
                } else if (c instanceof DingTalkClient) {
                    context.getFlowScope().put("dingtalkName",
                            c.getName());
                    context.getFlowScope().put("dingtalkKey",
                            ((DingTalkClient) c).getConfiguration().getKey());

                }
            } else {
                list.add(c.getName());
            }
        });


        context.getFlowScope().put("methods", list);
        return success();
    }


}
