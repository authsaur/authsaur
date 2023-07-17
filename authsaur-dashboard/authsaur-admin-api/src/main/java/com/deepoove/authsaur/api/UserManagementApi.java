package com.deepoove.authsaur.api;

import com.deepoove.authsaur.jpa.*;
import com.deepoove.authsaur.request.UserCmd;
import com.deepoove.authsaur.response.OrganizationDTO;
import com.deepoove.authsaur.response.Result;
import com.deepoove.authsaur.response.UserDTO;
import com.deepoove.authsaur.response.UserDirectoryDTO;
import com.deepoove.authsaur.result.Page;
import com.deepoove.authsaur.setting.PrincipalNamedPolicy;
import com.deepoove.authsaur.setting.SettingKey;
import com.deepoove.authsaur.setting.SettingsConf;
import com.deepoove.authsaur.util.JSONHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.RegexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/usermanagement")
public class UserManagementApi {

    public static final String DEFAULT_USER_SOURCE = "-1024";
    @Autowired
    private JpaAuthenticationHandlerRegistry jpaAuthenticationHandlerRegistry;
    @Autowired
    private JpaUserRegistry jpaUserRegistry;
    @Autowired
    private JpaOrgRegistry jpaOrgRegistry;
    @Autowired
    private PrincipalNamedPolicy principalNamedPolicy;
    @Autowired
    private JpaOrgUserRegistry jpaOrgUserRegistry;
    @Autowired
    private SettingsConf settingsConf;


    @GetMapping("/source/{source}")
    public Result<Page<UserDTO>> query(@PathVariable String source,
                                       @RequestParam(required = false) String orgId,
                                       @RequestParam(required = false) boolean onlyNextLevel,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String email,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) Integer current,
                                       @RequestParam(required = false) Integer pageSize) {
        JpaOrgEntity rootBySource = jpaOrgRegistry.findRootBySource(source);
        if (rootBySource == null) {
            return Result.fail("不存在此用户目录");
        }
        if (StringUtils.isBlank(orgId)) {
            orgId = rootBySource.getId();
        }

        UserQuery query = new UserQuery();
        query.setOrgId(orgId);
        query.setName(name);
        query.setEmail(email);
        query.setPhone(phone);
        query.setCurrent(Page.wrapPageCurrent(current));
        query.setPageSize(Page.wrapPageSize(pageSize));
        Page<JpaUserEntity> page;
        if (!onlyNextLevel) {
            page = jpaUserRegistry.queryAllSubUserByOrgId(query);
        } else {
            page = jpaUserRegistry.querySubUserByOrgId(query);
        }
        List<JpaUserEntity> users = page.getList();
        List<UserDTO> collect = users.stream().map(p -> {
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(p, dto);
            return dto;

        }).collect(Collectors.toList());

        return Result.success(page.toNew(collect));
    }

    @GetMapping("/directory")
    public Result<Map<String, List<UserDirectoryDTO>>> queryDirectory() {
        List<JpaOrgEntity> jpaOrgEntities = jpaOrgRegistry.queryDirectory();
        return Result.successWithList(jpaOrgEntities.stream().map(s -> {
            UserDirectoryDTO dto = new UserDirectoryDTO();
            BeanUtils.copyProperties(s, dto);
            JpaAuthenticationEntity byId = jpaAuthenticationHandlerRegistry.findById(s.getSource());
            if (null != byId) {
                dto.setAuthnName(byId.getName());
                dto.setRelated(byId.getRelatedAuthnId());
            }
            dto.setCount(jpaUserRegistry.countByUserSource(s.getSource()));
            return dto;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/search")
    public Result<Map<String, List<Map<String, String>>>> search(@RequestParam String qs) {
        return Result.successWithList(jpaUserRegistry.queryByUserName(qs).stream().map(user -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", user.getPrincipal());
            map.put("name", user.getName());
            map.put("source", user.getSource());
            return map;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public Result<Object> getUser(@PathVariable String id) {
        JpaUserEntity handler = jpaUserRegistry.findByPrincipal(id);
        if (null == handler) {
            return Result.fail("用户不存在");
        }
        Map map = JSONHelper.objToMap(handler);
        map.remove("password");
        String principal = handler.getPrincipal();

        // get org
        List<OrganizationDTO> list = getOrgs(principal);
        map.put("orgs", list);
        return Result.success(map);
    }

    private List<OrganizationDTO> getOrgs(String principal) {
        List<JpaOrgUserEntity> jpaOrgUserEntities = jpaOrgUserRegistry.queryOrgByPrincipal(principal);
        List<OrganizationDTO> list = new ArrayList<>();
        for (JpaOrgUserEntity orgUser : jpaOrgUserEntities) {
            String orgId = orgUser.getOrgId();
            JpaOrgEntity byId = jpaOrgRegistry.findById(orgId);
            OrganizationDTO dto = new OrganizationDTO();
            dto.setId(orgId);
            dto.setName(byId.getName());
            list.add(dto);
        }
        return list;
    }

    @PostMapping
    public Result<JpaUserEntity> saveOrUpdate(@RequestBody UserCmd cmd) {
        JpaUserEntity original = null;
        String named = principalNamedPolicy.named(cmd.getUserId(), DEFAULT_USER_SOURCE);
        if (!"-1".equals(cmd.getPrincipal())) {
            original = jpaUserRegistry.findByPrincipal(cmd.getPrincipal());
            if (original == null) {
                return Result.fail("用户不存在");
            }
        } else {
            original = jpaUserRegistry.findByUserSource(cmd.getUserId(), DEFAULT_USER_SOURCE);
            if (original != null) {
                return Result.fail("用户名已存在");
            }
            original = jpaUserRegistry.findByPrincipal(named);
            if (original != null) {
                return Result.fail("用户名已存在");
            }
        }
        JpaUserEntity entity = new JpaUserEntity();
        if ("-1".equals(cmd.getPrincipal())) {
            entity.setId(-1);
            entity.setPrincipal(named);
            entity.setCreated(new Date());
        } else {
            entity.setId(original.getId());
            entity.setPrincipal(cmd.getPrincipal());
            entity.setPassword(original.getPassword());
            entity.setCreated(original.getCreated());
        }
        entity.setUserId(cmd.getUserId());
        entity.setName(cmd.getName());
        entity.setSource(DEFAULT_USER_SOURCE);
        entity.setEmail(cmd.getEmail());
        entity.setMobile(cmd.getMobile());
        if (StringUtils.isNotBlank(cmd.getPassword())) {
            Map<String, Object> setting = settingsConf.getSetting(SettingKey.SAFE);
            Object passwordPolicyPattern = setting.get(SettingKey.SAFE_PASSWORD_POLICY_PATTERN);
            if (!RegexUtils.find(passwordPolicyPattern.toString(), cmd.getPassword())) {
                return Result.fail("密码不符合策略");
            }
            entity.setPassword(DigestUtils.md5Hex(cmd.getPassword()).toLowerCase());
        }
        entity.setState(cmd.isState());
        entity.setUpdated(new Date());
        jpaUserRegistry.saveOrUpdate(entity);

        // save user-org
        List<String> orgIds = cmd.getOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            JpaOrgEntity rootBySource = jpaOrgRegistry.findRootBySource(DEFAULT_USER_SOURCE);
            orgIds = new ArrayList<>();
            orgIds.add(rootBySource.getId());
        }
        List<JpaOrgUserEntity> userOrgs = new ArrayList<>();
        for (String orgId : orgIds) {
            JpaOrgUserEntity orgUser = new JpaOrgUserEntity();
            orgUser.setPrincipal(entity.getPrincipal());
            JpaOrgEntity orgEntity = jpaOrgRegistry.findById(orgId);
            orgUser.setOrgId(orgEntity.getId());
            orgUser.setOrgPath(orgEntity.getPath());
            userOrgs.add(orgUser);
        }
        jpaOrgUserRegistry.saveOrUpdateOrgsByPrincipal(userOrgs);
        return Result.success(entity);
    }


    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) throws IllegalAccessException {
        JpaUserEntity handler = jpaUserRegistry.findByPrincipal(id);
        if (null == handler) {
            return Result.fail("用户不存在");
        }
        jpaUserRegistry.deleteByUserId(id);
        // remove org
        jpaOrgUserRegistry.deleteOrgsByPrincipalId(handler.getPrincipal());
        return Result.success(null);
    }


}
