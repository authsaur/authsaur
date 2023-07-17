package com.deepoove.authsaur.configurer.action;

import com.deepoove.authsaur.services.ServicePropertyKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apereo.cas.CentralAuthenticationService;
import com.deepoove.authsaur.audit.AccountAuditActionContext;
import com.deepoove.authsaur.audit.AuditQuery;
import com.deepoove.authsaur.audit.JdbcAuditTrailManager;
import com.deepoove.authsaur.jpa.JpaAuthenticationEntity;
import com.deepoove.authsaur.jpa.JpaAuthenticationHandlerRegistry;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.deepoove.authsaur.jpa.JpaOrgEntity;
import com.deepoove.authsaur.jpa.JpaOrgRegistry;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import com.deepoove.authsaur.principal.MinePrincipal;
import org.apereo.cas.services.*;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Prepare the authenticated account for view.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class PrepareAccountAppViewAction extends BaseCasWebflowAction {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
                                                                         .defaultTypingEnabled(false).build()
                                                                         .toObjectMapper();

    private final CentralAuthenticationService centralAuthenticationService;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    private final AuditTrailManager auditTrailManager;

    final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    final JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;
    final JpaOrgRegistry jpaOrgRegistry;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        val ticketGrantingTicket = FunctionUtils.doAndHandle(
                () -> Optional.of(centralAuthenticationService.getTicket(tgt, TicketGrantingTicket.class)),
                throwable -> Optional.<TicketGrantingTicket>empty()).get();

        ticketGrantingTicket.ifPresent(ticket -> {
            WebUtils.putAuthentication(ticket.getAuthentication(), requestContext);
            MinePrincipal minePrincipal = new MinePrincipal(ticket.getAuthentication().getPrincipal());
            List<JpaOrgEntity> orgList = jpaOrgRegistry.queryByOrgList(minePrincipal.getDept());
            List<String> collect = orgList.stream().map(JpaOrgEntity::getName).collect(Collectors.toList());
            requestContext.getFlowScope().put("dept", StringUtils.join(collect, ","));
            val service = WebUtils.getService(requestContext);
            if (casProperties.getView().isAuthorizedServicesOnSuccessfulLogin()) {
                buildAuthorizedServices(requestContext, ticket, service);
            }
            buildAuditLogRecords(requestContext, ticket);
        });

        ticketGrantingTicket.ifPresent(ticket -> {
            String id = ticket.getAuthentication().getPrincipal().getId();
            List<JpaAuthenticationEntity> lists = jpaAuthenticationHandlerRegistry.queryActiveMFA();
            int otpEnabled = -1;
            if (lists.size() > 0) {
                otpEnabled = 0;
                if (googleAuthenticatorAccountRegistry.count(id) > 0) {
                    otpEnabled = 1;
                }
            }
            requestContext.getFlowScope().put("otpEnabled", otpEnabled);
        });

        return success();
    }

    protected void buildAuthorizedServices(final RequestContext requestContext, final TicketGrantingTicket ticket,
                                           final WebApplicationService service) {
        val authzAttributes = (Map) CollectionUtils.merge(ticket.getAuthentication().getAttributes(),
                                                          ticket.getAuthentication().getPrincipal().getAttributes());
        val authorizedServices = servicesManager.getAllServices()
                                                .stream()
                                                .filter(registeredService -> {
                                                    try {
                                                        return RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(
                                                                service,
                                                                registeredService,
                                                                ticket.getAuthentication().getPrincipal().getId(),
                                                                authzAttributes);
                                                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
                                                        return false;
                                                    }
                                                })
//                .filter(registeredService -> FunctionUtils.doAndHandle(
//                        () -> RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
//                                registeredService, ticket.getAuthentication().getPrincipal().getId(),
//                                authzAttributes),
//                        throwable -> false).get())
                                                // filter internal services
                                                .filter(s -> !s.getName()
                                                               .equals("RegexRegisteredService") && !s.getName().equals(
                                                        "CasRegisteredService"))
                                                .sorted(new Comparator<RegisteredService>() {
                                                    @Override
                                                    public int compare(RegisteredService the, RegisteredService other) {
                                                        return new CompareToBuilder()
                                                                .append(the.getEvaluationOrder(),
                                                                        other.getEvaluationOrder())
                                                                .append(StringUtils.defaultIfBlank(the.getName(),
                                                                                                   StringUtils.EMPTY)
                                                                                   .toLowerCase(),
                                                                        StringUtils.defaultIfBlank(other.getName(),
                                                                                                   StringUtils.EMPTY)
                                                                                   .toLowerCase())
                                                                .append(the.getServiceId(), other.getServiceId())
                                                                .append(the.getId(), other.getId())
                                                                .toComparison();
                                                    }
                                                })
                                                .collect(Collectors.toList());
        WebUtils.putAuthorizedServices(requestContext, authorizedServices);
        Map<String, List<ServiceVO>> authorizedServicesGroupMap = new LinkedHashMap<>();
        authorizedServices.forEach(s -> {
            String group = null;
            Map<String, RegisteredServiceProperty> properties = s.getProperties();
            RegisteredServiceProperty registeredServiceProperty = properties.get(ServicePropertyKey.TAG.getAttr());
            if (null != registeredServiceProperty) {
                group = registeredServiceProperty.getValue();
                if (StringUtils.isBlank(group)) {
                    group = null;
                }
            }
            authorizedServicesGroupMap.computeIfAbsent(group, k -> new ArrayList<>())
                                      .add(convert(s));
        });
        List<ServiceGroup> groups = new ArrayList<>();

        authorizedServicesGroupMap.forEach((k, v) -> {
            if (null != k) {
                groups.add(new ServiceGroup(k, v));
            } else {
                groups.add(0, new ServiceGroup(k, v));
            }
        });
        requestContext.getFlowScope().put("authorizedServiceGroups", groups);
    }

    private ServiceVO convert(RegisteredService service) {
        Map<String, RegisteredServiceProperty> properties = service.getProperties();
        ServiceVO vo = new ServiceVO();
        vo.setId(service.getId());
        vo.setServiceId(service.getServiceId());
        vo.setLogo(service.getLogo());
        vo.setDescription(service.getDescription());
        vo.setName(
                Optional.ofNullable(properties.get(ServicePropertyKey.ALIAS.getAttr())).map(p -> p.getValue()).orElse(
                        service.getName()));
        vo.setTag(Optional.ofNullable(properties.get(ServicePropertyKey.TAG.getAttr())).map(p -> p.getValue()).orElse(
                null));
        vo.setHomePage(Optional.ofNullable(properties.get(ServicePropertyKey.HOME_PAGE.getAttr())).map(
                p -> p.getValue()).orElse(null));
        vo.setSaml(Optional.ofNullable(properties.get(ServicePropertyKey.SAML_REQUEST.getAttr())).map(
                p -> p.getValue()).orElse(null));
        return vo;
    }

    protected void buildAuditLogRecords(final RequestContext requestContext, final TicketGrantingTicket ticket) {
        AuditQuery query = new AuditQuery();
        query.setPrincipalId(ticket.getAuthentication().getPrincipal().getId());
        query.setCurrent(1);
        query.setPageSize(100);
        val page = ((JdbcAuditTrailManager) auditTrailManager).queryRecord(query);

        var auditLog = page.getList().stream().map(l -> {
            AccountAuditActionContext c = new AccountAuditActionContext(l);
            return c;
        }).collect(Collectors.toList());

        FunctionUtils.doIf(!auditLog.isEmpty(), u -> requestContext.getFlowScope().put("auditLog", auditLog)).accept(
                auditLog);
    }


}
