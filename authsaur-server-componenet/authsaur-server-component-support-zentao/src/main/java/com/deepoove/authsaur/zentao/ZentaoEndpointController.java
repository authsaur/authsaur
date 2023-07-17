package com.deepoove.authsaur.zentao;

import com.deepoove.authsaur.services.ZentaoRegisteredService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import com.deepoove.authsaur.principal.PrincipalAttr;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.DigestUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class ZentaoEndpointController {

    @Autowired
    ZentaoConfigurationContext zentaoConfigurationContext;

    // https://cas.sayi.com:8443/cas/zentao/sso?client_id=zantao
    @GetMapping("/zentao/sso")
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {

        String clientId = request.getParameter("client_id");
        if (StringUtils.isBlank(clientId)) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    "Cannot find clientid linked to " + clientId);
        }
        ServicesManager servicesManager = zentaoConfigurationContext.getServicesManager();
        ZentaoRegisteredService registeredService = getRegisteredServiceByClientId(clientId);
        if (registeredService == null) {
            String msg = String.format("Unauthorized Service Access. Service [%s] is not found in service registry.",
                    clientId);
//            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        String url = constructServiceUrl(request, response);

        String initialUrl = CommonUtils.constructRedirectUrl(
                zentaoConfigurationContext.getCasProperties().getServer().getLoginUrl(),
                CasProtocolConstants.PARAMETER_SERVICE, url, false,
                false);

        return new ModelAndView(new RedirectView(initialUrl));
    }

    @GetMapping("/zentao/Callback")
    protected ModelAndView handleCallbackProfileRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Exception {
        // System.out.println("Received pwdless callback profile request [{}]" + request.getRequestURI());

        String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
//            LOGGER.error("Can not validate the request because no [{}] is provided via the request",
//                    CasProtocolConstants.PARAMETER_TICKET);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    StringUtils.EMPTY);
        }

        String serviceUrl = constructServiceUrl(request, response);
        Assertion assertion = zentaoConfigurationContext.getTicketValidator().validate(ticket, serviceUrl);

        ZentaoRegisteredService registeredService = getRegisteredServiceByClientId(
                request.getParameter("client_id"));

        // get attribute
        val cookie = zentaoConfigurationContext.getTicketGrantingTicketCookieGenerator().retrieveCookieValue(
                request);
        if (StringUtils.isBlank(cookie)) {
            LOGGER.debug("Single sign-on session cannot be found or determined. Ignoring single sign-on session");
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    StringUtils.EMPTY);
        }

        val ticketGrantingTicket = zentaoConfigurationContext.getTicketRegistrySupport().getTicketGrantingTicket(
                cookie);
        if (ticketGrantingTicket == null) {
            LOGGER.debug("Authentication transaction linked to single sign-on session cannot determined.");
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    StringUtils.EMPTY);
        }

        val authentication = ticketGrantingTicket.getAuthentication();
        LOGGER.debug("Located single sign-on authentication for principal [{}]", authentication.getPrincipal());

        val service = zentaoConfigurationContext.getWebApplicationServiceFactory().createService(
                registeredService.getServiceId());
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(service)
                .principal(authentication.getPrincipal())
                .build();
        val attributes = registeredService.getAttributeReleasePolicy().getAttributes(context);
        val principalId = registeredService.getUsernameAttributeProvider()
                .resolveUsername(authentication.getPrincipal(), service, registeredService);
//        attributes.putAll(assertion.getAttributes());
//        System.out.println(principalId + attributes);


        Map<String, Object> attributes1 = assertion.getPrincipal().getAttributes();
        Object actualids = attributes1.get(PrincipalAttr.USER_ID.getAttr());
        String actualid = null;
        if (null != actualids) {
            actualid = String.valueOf(((List) actualids).get(0));
        }
        String userName = null == actualid ? assertion.getPrincipal().getName() : actualid;
        String serviceId = registeredService.getServiceId();
        long second = System.currentTimeMillis() / 1000;
        serviceId += "&time=" + second;
        serviceId += "&account=" + userName;

        String redirect = request.getParameter("redirect");
        if (redirect != null) {
            int lastIndexOf = redirect.lastIndexOf("user-login-");
            if (-1 != lastIndexOf) {
                redirect = redirect.substring(lastIndexOf + "user-login-".length(),
                        redirect.length() - ".html".length());
                redirect = new String(Base64.decodeBase64(redirect));
            } else {
                redirect = "/";
            }
        } else {
            redirect = "/";
        }
        String code = registeredService.getCode();
        serviceId += "&code=" + code;
        redirect = URLEncoder.encode(redirect, StandardCharsets.UTF_8.toString());
        serviceId += "&redirect=" + redirect;
        String secret = registeredService.getSecret();
        String source = code + secret + second;
        String md5 = DigestUtils.digest(MessageDigestAlgorithms.MD5, source);
        serviceId += "&token=" + md5;
        return new ModelAndView(new RedirectView(serviceId));

    }

    protected ZentaoRegisteredService getRegisteredServiceByClientId(final String clientId) {
        Collection<RegisteredService> services = zentaoConfigurationContext.getServicesManager().getAllServices();
        return services.stream()
                .filter(ZentaoRegisteredService.class::isInstance)
                .map(ZentaoRegisteredService.class::cast)
                .filter(s -> s.getClientId().equals(clientId))
                .findFirst()
                .orElse(null);
    }

    protected String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(zentaoConfigurationContext.getCallbackService().getId());
        sb.append("?client_id=").append(request.getParameter("client_id"));
        sb.append("&redirect=").append(request.getParameter("redirect"));

        String url = sb.toString();

//        System.out.println("Built pwdless service callback url [{}]" + url);
        return CommonUtils.constructServiceUrl(request, response,
                url, zentaoConfigurationContext.getCasProperties().getServer().getName(),
                CasProtocolConstants.PARAMETER_SERVICE,
                CasProtocolConstants.PARAMETER_TICKET, false);
    }
}

