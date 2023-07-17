package com.deepoove.authsaur.api;

import com.deepoove.authsaur.audit.AccountAuditActionContext;
import com.deepoove.authsaur.audit.AuditQuery;
import com.deepoove.authsaur.audit.JdbcAuditTrailManager;
import com.deepoove.authsaur.jpa.JpaUserEntity;
import com.deepoove.authsaur.jpa.JpaUserRegistry;
import com.deepoove.authsaur.response.Result;
import com.deepoove.authsaur.result.Page;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/audit")
public class AuditApi {

    @Autowired
    private JpaUserRegistry jpaUserRegistry;

    @Autowired
    private AuditTrailManager auditTrailManager;

    @GetMapping("/user")
    public Result<Page<AccountAuditActionContext>> query(@RequestParam(required = false) String principal,
                                                         @RequestParam(required = false) String clientIpAddress,
                                                         @RequestParam(required = false) String startTime,
                                                         @RequestParam(required = false) String endTime,
                                                         @RequestParam(required = false) Integer current,
                                                         @RequestParam(required = false) Integer pageSize) {
        AuditQuery query = new AuditQuery();
        query.setPrincipalId(principal);
        query.setIp(clientIpAddress);
        query.setStartDate(startTime);
        query.setEndDate(endTime);
        query.setCurrent(Page.wrapPageCurrent(current));
        query.setPageSize(Page.wrapPageSize(pageSize));
        val page = ((JdbcAuditTrailManager) auditTrailManager).queryRecord(query);
        List<AuditActionContext> logs = page.getList();
        Set<String> principals = logs.stream().map(AuditActionContext::getPrincipal).collect(Collectors.toSet());
        Map<String, JpaUserEntity> map = jpaUserRegistry.queryByPrincipalList(new ArrayList<>(principals)).stream()
                .collect(Collectors.toMap(JpaUserEntity::getPrincipal, e -> e));
        return Result.success(page.toNew(logs.stream().map(l -> {
            AccountAuditActionContext context = new AccountAuditActionContext(l);
            JpaUserEntity user = map.get(context.getPrincipal());
            if (null != user) {
                context.setPrincipalName(user.getName());
            }
            return context;
        }).collect(Collectors.toList())));
    }

}
