package com.deepoove.authsaur.api;

import com.deepoove.authsaur.event.AuthsaurEvent;
import com.deepoove.authsaur.event.AuthsaurEventTrigger;
import com.deepoove.authsaur.jpa.*;
import com.deepoove.authsaur.principal.PrincipalAttr;
import com.deepoove.authsaur.response.AccessStrategyDTO;
import com.deepoove.authsaur.response.OrgStrategyDTO;
import com.deepoove.authsaur.response.Result;
import com.deepoove.authsaur.response.UserStrategyDTO;
import org.apereo.cas.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/service_strategy")
public class ServiceStrategyApi {

    public static final String ALLOWED = "Allowed";
    public static final String DENIED = "Denied";
    @Autowired
    @Qualifier("jpaServiceRegistry")
    private ServiceRegistry serviceRegistry;
    @Autowired
    private JpaAppRegistry jpaAppRegistry;
    @Autowired
    private JpaUserRegistry jpaUserRegistry;
    @Autowired
    private JpaOrgRegistry jpaOrgRegistry;
    @Autowired
    private AuthsaurEventTrigger eventTrigger;

    @GetMapping("/{appId}")
    public Result<AccessStrategyDTO> get(@PathVariable long appId) {
        RegisteredService service = serviceRegistry.findServiceById(appId);
        if (null == service) {
            return Result.fail("应用不存在");
        }
        RegisteredServiceAccessStrategy accessStrategy = service.getAccessStrategy();
        JpaAppEntity byApp = jpaAppRegistry.findByApp(appId);
        if (null == byApp) {
            byApp = new JpaAppEntity();
            byApp.setAppId(appId);
            byApp.setAccessStrategyType(accessStrategy.getClass().getName());
            byApp.fromStrategy(accessStrategy);
            jpaAppRegistry.saveOrUpdate(byApp);
        }
        AccessStrategyDTO dto = convert(byApp);
        return Result.success(dto);
    }

    private AccessStrategyDTO convert(JpaAppEntity byApp) {
        AccessStrategyDTO dto = new AccessStrategyDTO();
        dto.setAppId(byApp.getAppId());
        DefaultRegisteredServiceAccessStrategy strategy = (DefaultRegisteredServiceAccessStrategy) byApp.readStrategy();
        Map<String, Set<String>> requiredAttributes = strategy.getRequiredAttributes();
        populate(dto, requiredAttributes, ALLOWED);
        populate(dto, strategy.getRejectedAttributes(), DENIED);
        return dto;
    }

    private void populate(AccessStrategyDTO dto, Map<String, Set<String>> attributes, String action) {
        Set<String> uprincipal = attributes.get(PrincipalAttr.PRINCIPAL.getAttr());
        if (null != uprincipal) {
            List<UserStrategyDTO> users = new ArrayList<>();
            for (String p : uprincipal) {
                UserStrategyDTO user = new UserStrategyDTO();
                user.setPrincipal(p);
                JpaUserEntity byPrincipal = jpaUserRegistry.findByPrincipal(p);
                if (null != byPrincipal) {
                    user.setName(byPrincipal.getName());
                }
                user.setAction(action);
                users.add(user);
            }
            dto.getUsers().addAll(users);
        }
        Set<String> udept = attributes.get(PrincipalAttr.DEPT.getAttr());
        if (null != udept) {
            List<OrgStrategyDTO> orgs = new ArrayList<>();
            for (String d : udept) {
                OrgStrategyDTO org = new OrgStrategyDTO();
                org.setId(d);
                JpaOrgEntity byId = jpaOrgRegistry.findById(d);
                if (null != byId) {
                    List<JpaOrgEntity> jpaOrgEntities = jpaOrgRegistry.queryByOrgList(Arrays.asList(byId.getPath()
                            .split("/")));
                    String[] array = jpaOrgEntities.stream().map(o -> o.getName()).toArray(String[]::new);
                    org.setName(String.join("/", array));
                }
                org.setAction(action);
                orgs.add(org);
            }
            dto.getOrgs().addAll(orgs);
        }
    }

    @PostMapping
    public Result<Void> saveOrUpdate(@RequestBody AccessStrategyDTO cmd) {
        JpaAppEntity byApp = apply(cmd);
        jpaAppRegistry.saveOrUpdate(byApp);

        DefaultRegisteredServiceAccessStrategy strategy = (DefaultRegisteredServiceAccessStrategy) byApp.readStrategy();
        AbstractRegisteredService serviceBy =
                (AbstractRegisteredService) serviceRegistry.findServiceById(cmd.getAppId());
        serviceBy.setAccessStrategy(strategy);
        serviceRegistry.save(serviceBy);

        eventTrigger.trigger(AuthsaurEvent.SERVICE);
        return Result.success();
    }

    private JpaAppEntity apply(AccessStrategyDTO cmd) {
        JpaAppEntity entity = new JpaAppEntity();
        entity.setAppId(cmd.getAppId());
        entity.setAccessStrategyType(DefaultRegisteredServiceAccessStrategy.class.getName());
        DefaultRegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setRequireAllAttributes(false);
        Map<String, Set<String>> requiredAttributes = new HashMap<>();
        Map<String, Set<String>> rejectAttributes = new HashMap<>();
        List<UserStrategyDTO> users = cmd.getUsers();
        Set<String> userSets = users.stream().filter(s -> ALLOWED.equals(s.getAction())).map(s -> s.getPrincipal())
                .collect(Collectors.toSet());
        if (!userSets.isEmpty()) {
            requiredAttributes.put(PrincipalAttr.PRINCIPAL.getAttr(), userSets);
        }
        Set<String> userRejectSets = users.stream().filter(s -> !ALLOWED.equals(s.getAction()))
                .map(s -> s.getPrincipal()).collect(Collectors.toSet());
        if (!userRejectSets.isEmpty()) {
            rejectAttributes.put(PrincipalAttr.PRINCIPAL.getAttr(), userRejectSets);
        }
        List<OrgStrategyDTO> orgs = cmd.getOrgs();
        Set<String> orgSets = orgs.stream().filter(s -> ALLOWED.equals(s.getAction())).map(s -> s.getId())
                .collect(Collectors.toSet());
        if (!orgSets.isEmpty()) {
            requiredAttributes.put(PrincipalAttr.DEPT.getAttr(), orgSets);
        }
        Set<String> orgRejectSets = orgs.stream().filter(s -> !ALLOWED.equals(s.getAction())).map(s -> s.getId())
                .collect(Collectors.toSet());
        if (!orgRejectSets.isEmpty()) {
            rejectAttributes.put(PrincipalAttr.DEPT.getAttr(), orgRejectSets);
        }
        accessStrategy.setRequiredAttributes(requiredAttributes);
        accessStrategy.setRejectedAttributes(rejectAttributes);
        entity.fromStrategy(accessStrategy);
        return entity;
    }


}
