package com.deepoove.authsaur.web;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.BaseDelegatedAuthenticationController;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class DelegatedNavigationController extends BaseDelegatedAuthenticationController {

    @Autowired
    CasConfigurationProperties casProperties;

    private final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    public DelegatedNavigationController(
            final DelegatedClientAuthenticationConfigurationContext context,
            final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager) {
        super(context);
        this.delegatedClientAuthenticationWebflowManager = delegatedClientAuthenticationWebflowManager;
    }

    /**
     * Redirect to provider. Receive the client name from the request and then try to determine and build the endpoint url
     * for the redirection. The redirection data/url must contain a delegated client ticket id so that the request be can
     * restored on the trip back. SAML clients use the relay-state session attribute while others use request parameters.
     *
     * @param request  the request
     * @param response the response
     * @return the view
     */
    @GetMapping("stredirect")
    public View redirectToProvider(final HttpServletRequest request, final HttpServletResponse response) {
        var clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        if (StringUtils.isBlank(clientName)) {
            clientName = (String) request.getAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        }

        try {
            if (StringUtils.isBlank(clientName)) {
                throw new UnauthorizedServiceException("No client name parameter is provided in the incoming request");
            }
            Optional<Client> clientResult = getConfigurationContext().getClients().findClient(clientName);
            if (clientResult.isEmpty()) {
                throw new UnauthorizedServiceException("Unable to locate client " + clientName);
            }
            IndirectClient client = IndirectClient.class.cast(clientResult.get());
            client.init();
            val webContext = new JEEContext(request, response);
            val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);

            StringBuffer requestURL = request.getRequestURL();
            String str = requestURL.toString();
            str = str.replaceAll("stredirect", "login");
            requestURL = new StringBuffer(str);
//            requestURL.append("?client_name=").append(clientName).append("&authCode=")
//                    .append(request.getParameter("authCode"));
//            requestURL.append("?state=").append(ticket.getId());
//            requestURL.append("?delegatedclientid=").append(ticket.getId());
            requestURL.append("?").append(request.getQueryString());
            getResultingView(client, webContext, ticket);
            return new RedirectView(requestURL.toString());
        } catch (final Exception e) {
            e.printStackTrace();
            val message = String.format("Authentication request was denied from the provider %s", clientName);
            //LoggingUtils.warn(LOGGER, message, e);
            throw new UnauthorizedServiceException(e.getMessage(), e);
        }
    }
    @GetMapping("callback/{clientName}")
    public View redirect(final HttpServletRequest request, final HttpServletResponse response, @PathVariable String clientName) {
            return new RedirectView(casProperties.getServer().getLoginUrl() + "?" + request.getQueryString() + "&client_name=" + clientName);
    }
    // https://cas.sayi.com:8443/cas/callback/weibo
    @PostMapping("callback/{clientName}")
    public View redirectPost(final HttpServletRequest request, final HttpServletResponse response, @PathVariable String clientName) {
            return new RedirectView(casProperties.getServer().getLoginUrl() + "?" + request.getQueryString() + "&client_name=" + clientName);
    }


}
