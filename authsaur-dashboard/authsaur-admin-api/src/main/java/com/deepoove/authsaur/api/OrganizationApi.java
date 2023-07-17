package com.deepoove.authsaur.api;

import com.deepoove.authsaur.jpa.JpaOrgEntity;
import com.deepoove.authsaur.jpa.JpaOrgRegistry;
import com.deepoove.authsaur.jpa.JpaOrgUserEntity;
import com.deepoove.authsaur.jpa.JpaOrgUserRegistry;
import com.deepoove.authsaur.request.OrganizationCmd;
import com.deepoove.authsaur.response.OrganizationDTO;
import com.deepoove.authsaur.response.Result;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/organization")
public class OrganizationApi {

    @Autowired
    private JpaOrgRegistry jpaOrgRegistry;
    @Autowired
    private JpaOrgUserRegistry jpaOrgUserRegistry;

    @GetMapping("/search")
    public Result<Map<String, List<OrganizationDTO>>> search(@RequestParam String source, @RequestParam String qs) {
        return Result.successWithList(jpaOrgRegistry.queryByName(source, qs).stream().map(org -> {
            OrganizationDTO dto = new OrganizationDTO();
            dto.setId(org.getId());
            dto.setName(org.getName());
            List<JpaOrgEntity> jpaOrgEntities = jpaOrgRegistry.queryByOrgList(Arrays.asList(org.getPath().split("/")));
            String[] array = jpaOrgEntities.stream().map(o -> o.getName()).toArray(String[]::new);
            dto.setPath(String.join("/", array));
            return dto;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/{source}")
    public Result<Map<String, List<OrganizationDTO>>> queryAll(@RequestParam String source) {
        List<JpaOrgEntity> jpaOrgEntities = jpaOrgRegistry.queryAllBySource(source);
        return Result.successWithList(jpaOrgEntities.stream().map(this::convert).collect(Collectors.toList()));
    }

    @GetMapping
    public Result<Map<String, List<OrganizationDTO>>> query(@RequestParam String source,
                                                            @RequestParam(required = false) String parentId) {
        List<JpaOrgEntity> jpaOrgEntities = jpaOrgRegistry.querySubByParentId(source, parentId);
        return Result.successWithList(jpaOrgEntities.stream().map(this::convert).collect(Collectors.toList()));
    }


    private OrganizationDTO convert(JpaOrgEntity entity) {
        OrganizationDTO dto = new OrganizationDTO();
        BeanUtils.copyProperties(entity, dto);
        List<JpaOrgEntity> jpaOrgEntities = jpaOrgRegistry.querySubByParentId(entity.getSource(), entity.getId());
        dto.setSubOrgs(null == jpaOrgEntities ? 0 : jpaOrgEntities.size());
        return dto;
    }

    @PostMapping
    public Result<JpaOrgEntity> saveOrUpdate(@RequestBody OrganizationCmd cmd) {
        JpaOrgEntity original = null;
        JpaOrgEntity entity = new JpaOrgEntity();

        if (null != cmd.getId()) {
            // update
            original = jpaOrgRegistry.findById(cmd.getId());
            if (original == null) {
                return Result.fail("部门不存在");
            }
            entity = original;
            entity.setId(cmd.getId());
            entity.setParentId(original.getParentId());
            entity.setSource(original.getSource());
            entity.setPath(original.getPath());
        } else {
            // save
            JpaOrgEntity parent = jpaOrgRegistry.findById(cmd.getParentId());
            if (null == parent) {
                return Result.fail("父部门不存在");
            }
            entity.setId(UUID.randomUUID().toString().substring(0, 4));
            entity.setParentId(parent.getId());
            entity.setSource(parent.getSource());
            entity.setPath(parent.getPath() + "/" + entity.getId());
        }
        entity.setName(cmd.getName());
        jpaOrgRegistry.saveOrUpdate(entity);
        return Result.success(entity);
    }


    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) throws IllegalAccessException {
        JpaOrgEntity handler = jpaOrgRegistry.findById(id);
        if (null == handler) {
            return Result.fail("部门不存在");
        }
        List<JpaOrgEntity> jpaOrgEntities = jpaOrgRegistry.queryAllSubByParentId(handler.getSource(), id);
        if (CollectionUtils.isNotEmpty(jpaOrgEntities)) {
            return Result.fail("存在子部门，无法删除");
        }
        List<JpaOrgUserEntity> jpaOrgUserEntities = jpaOrgUserRegistry.querySubUserByOrgId(id);
        if (CollectionUtils.isNotEmpty(jpaOrgUserEntities)) {
            return Result.fail("部门存在用户，无法删除");
        }
        jpaOrgRegistry.delete(id);
        return Result.success();
    }


}
