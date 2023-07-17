package com.deepoove.authsaur.rest.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final CentralAuthenticationService centralAuthenticationService;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.info("[preHandle][" + request + "]" + "[" + request.getMethod() + "]" + request.getRequestURI());

        val cookie = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (null != cookie) {
            LOGGER.debug("Attempting to locate ticket-granting ticket from cookie value [{}]", cookie);
            val ticket = FunctionUtils.doAndHandle(
                    () -> centralAuthenticationService.getTicket(cookie, TicketGrantingTicket.class),
                    throwable -> null).get();
            if (ticket != null) {
                LOGGER.info("Found ticket-granting ticket [{}]", ticket.getId());
                AuthHolder.setUser(ticket.getAuthentication().getPrincipal());
                return true;
            }
        }
        response.setStatus(401);
        return false;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        AuthHolder.resetUser();
    }
}
