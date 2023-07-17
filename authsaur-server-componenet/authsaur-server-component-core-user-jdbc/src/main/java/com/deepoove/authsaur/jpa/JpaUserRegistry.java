package com.deepoove.authsaur.jpa;

import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import com.deepoove.authsaur.result.Page;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@EnableTransactionManagement
@Transactional(transactionManager = JpaUserRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
public class JpaUserRegistry {

    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerUser";

    @PersistenceContext(unitName = "userEntityManagerFactory")
    private EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;
    private final JpaOrgRegistry jpaOrgRegistry;

    public JpaUserRegistry(TransactionTemplate transactionTemplate, JpaOrgRegistry jpaOrgRegistry) {
        this.transactionTemplate = transactionTemplate;
        this.jpaOrgRegistry = jpaOrgRegistry;
    }

    public boolean delete(final long id) {
        val entity = findById(id);
        entityManager.remove(entity);
        return true;
    }

    public boolean deleteByUserSrouce(final String user, final String source) {
        val entity = findByUserSource(user, source);
        return delete(entity.getId());
    }

    public boolean deleteByUserId(final String principal) {
        val entity = findByPrincipal(principal);
        return delete(entity.getId());
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaUserEntity> query() {
        val query = String.format("SELECT r FROM %s r", JpaUserEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaUserEntity.class).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaUserEntity> queryByUserSource(String source) {
        val query = String.format("SELECT r FROM %s r WHERE r.source=:source", JpaUserEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaUserEntity.class).setParameter("source",
                source).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public int countByUserSource(String userSource) {
        String countSql = "select count(*) from user where source=:source";
        Query query1 = this.entityManager.createNativeQuery(countSql)
                .setParameter("source", userSource);
        Object singleResult = query1.getSingleResult();
        return Integer.parseInt(singleResult.toString());
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaUserEntity> queryByUserName(String name) {
        val query = String.format(
                "SELECT r FROM %s r WHERE r.name LIKE concat(:name,'%%') or r.userId LIKE concat(:userId,'%%')",
                JpaUserEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaUserEntity.class).setParameter("name",
                name).setParameter("userId",
                name).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public Page<JpaUserEntity> querySubUserByOrgId(UserQuery userQuery) {
        String query =
                "(select distinct(principal) from org_user where org_id = :orgId) a left join user b on a.principal =" +
                        " b.principal where" +
                        " 1=1 ";
        if (StringUtils.isNotBlank(userQuery.getName())) {
            query += " AND user_Id like concat('%', concat(:name,'%'))";
        }
        if (StringUtils.isNotBlank(userQuery.getEmail())) {
            query += " AND email like concat(:email,'%')";
        }
        if (StringUtils.isNotBlank(userQuery.getPhone())) {
            query += " AND mobile like concat(:phone,'%')";
        }
        String countSql = "select count(*) from " + query;
        Query query1 = this.entityManager.createNativeQuery(countSql)
                .setParameter("orgId",
                        userQuery.getOrgId());
        if (StringUtils.isNotBlank(userQuery.getName())) {
            query1.setParameter("name", userQuery.getName());
        }
        if (StringUtils.isNotBlank(userQuery.getEmail())) {
            query1.setParameter("email", userQuery.getEmail());
        }
        if (StringUtils.isNotBlank(userQuery.getPhone())) {
            query1.setParameter("phone", userQuery.getPhone());
        }
        Object singleResult = query1.getSingleResult();


        query += " order by created desc LIMIT " + userQuery.getPageSize() + " OFFSET " + (userQuery.getCurrent() - 1) * userQuery.getPageSize();
        query1 = this.entityManager.createNativeQuery(
                        "select a.principal,user_Id,email,mobile,name,state,created from " + query)
                .setParameter("orgId",
                        userQuery.getOrgId());
        if (StringUtils.isNotBlank(userQuery.getName())) {
            query1.setParameter("name", userQuery.getName());
        }
        if (StringUtils.isNotBlank(userQuery.getEmail())) {
            query1.setParameter("email", userQuery.getEmail());
        }
        if (StringUtils.isNotBlank(userQuery.getPhone())) {
            query1.setParameter("phone", userQuery.getPhone());
        }
        List<Object[]> resultList = query1.getResultList();
        System.out.println(Arrays.toString(resultList.toArray()));
        val list = new ArrayList<JpaUserEntity>();
        for (Object[] objects : resultList) {
            JpaUserEntity orgUser = new JpaUserEntity();
            orgUser.setPrincipal(objects[0].toString());
            orgUser.setUserId(objects[1].toString());
            orgUser.setEmail(null == objects[2] ? null : String.valueOf(objects[2]));
            orgUser.setMobile(null == objects[3] ? null : String.valueOf(objects[3]));
            orgUser.setName(null == objects[4] ? null : String.valueOf(objects[4]));
            orgUser.setState((Boolean) objects[5]);
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:MM::ss");
                orgUser.setCreated(df.parse(String.valueOf(objects[6])));
            } catch (ParseException e) {
                System.out.println(e.getMessage());
//                throw new RuntimeException(e);
            }
            list.add(orgUser);
        }
        Page<JpaUserEntity> objectPage = new Page<>();
        objectPage.setTotal(Integer.parseInt(singleResult.toString()));
        objectPage.setList(list);
        objectPage.setCurrent(userQuery.getCurrent());
        objectPage.setPageSize(userQuery.getPageSize());
        return objectPage;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public Page<JpaUserEntity> queryAllSubUserByOrgId(UserQuery userQuery) {
        JpaOrgEntity byId = jpaOrgRegistry.findById(userQuery.getOrgId());
        String path = byId.getPath() + "/";
        String query =
                "(select distinct(principal) from org_user where org_Id=:orgId OR org_Path LIKE concat(:orgPath,'%'))" +
                        " a left join user" +
                        " b on a.principal = b.principal where 1=1";
        if (StringUtils.isNotBlank(userQuery.getName())) {
            query += " AND user_Id like concat('%', concat(:name,'%'))";
        }
        if (StringUtils.isNotBlank(userQuery.getEmail())) {
            query += " AND email like concat(:email,'%')";
        }
        if (StringUtils.isNotBlank(userQuery.getPhone())) {
            query += " AND mobile like concat(:phone,'%')";
        }
        String countSql = "select count(*) from " + query;
        System.out.println(countSql);
        Query query1 = this.entityManager.createNativeQuery(countSql)
                .setParameter("orgId",
                        userQuery.getOrgId()).setParameter("orgPath", path);
        if (StringUtils.isNotBlank(userQuery.getName())) {
            query1.setParameter("name", userQuery.getName());
        }
        if (StringUtils.isNotBlank(userQuery.getEmail())) {
            query1.setParameter("email", userQuery.getEmail());
        }
        if (StringUtils.isNotBlank(userQuery.getPhone())) {
            query1.setParameter("phone", userQuery.getPhone());
        }
        Object singleResult = query1.getSingleResult();


        query += " order by created desc LIMIT " + userQuery.getPageSize() + " OFFSET " + (userQuery.getCurrent() - 1) * userQuery.getPageSize();
        query1 = this.entityManager.createNativeQuery(
                        "select a.principal,user_Id,email,mobile,name,state,created from " + query)
                .setParameter("orgId",
                        userQuery.getOrgId()).setParameter("orgPath", path);
        if (StringUtils.isNotBlank(userQuery.getName())) {
            query1.setParameter("name", userQuery.getName());
        }
        if (StringUtils.isNotBlank(userQuery.getEmail())) {
            query1.setParameter("email", userQuery.getEmail());
        }
        if (StringUtils.isNotBlank(userQuery.getPhone())) {
            query1.setParameter("phone", userQuery.getPhone());
        }
        List<Object[]> resultList = query1.getResultList();
        System.out.println(Arrays.toString(resultList.toArray()));
        val list = new ArrayList<JpaUserEntity>();
        for (Object[] objects : resultList) {
            JpaUserEntity orgUser = new JpaUserEntity();
            orgUser.setPrincipal(objects[0].toString());
            orgUser.setUserId(objects[1].toString());
            orgUser.setEmail(null == objects[2] ? null : String.valueOf(objects[2]));
            orgUser.setMobile(null == objects[3] ? null : String.valueOf(objects[3]));
            orgUser.setName(null == objects[4] ? null : String.valueOf(objects[4]));
            orgUser.setState((Boolean) objects[5]);
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:MM::ss");
                orgUser.setCreated(df.parse(String.valueOf(objects[6])));
            } catch (ParseException e) {
                System.out.println(e.getMessage());
//                throw new RuntimeException(e);
            }
            list.add(orgUser);
        }
        Page<JpaUserEntity> objectPage = new Page<>();
        objectPage.setTotal(Integer.parseInt(singleResult.toString()));
        objectPage.setList(list);
        objectPage.setCurrent(userQuery.getCurrent());
        objectPage.setPageSize(userQuery.getPageSize());
        return objectPage;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaUserEntity> queryByPrincipalList(List<String> principals) {
        val query = String.format("SELECT r FROM %s r WHERE r.principal in :principals", JpaUserEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaUserEntity.class).setParameter("principals",
                principals).getResultList();
        return list;
    }

    public JpaUserEntity findById(final long id) {
        return Optional.ofNullable(this.entityManager.find(JpaUserEntity.class, id)).orElse(null);
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaUserEntity findByUserSource(final String userId, final String source) {
        val query = String.format("SELECT r FROM %s r WHERE r.userId=:userId and r.source=:source",
                JpaUserEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaUserEntity.class).setParameter("userId", userId).setParameter(
                "source", source).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaUserEntity findByPrincipal(final String principal) {
        val query = String.format("SELECT r FROM %s r WHERE r.principal=:principal", JpaUserEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaUserEntity.class).setParameter("principal",
                principal).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaUserEntity findByEmail(final String email, final String source) {
        val query = String.format("SELECT r FROM %s r WHERE r.email=:email and r.source=:source",
                JpaUserEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaUserEntity.class).setParameter("email",
                email).setParameter(
                "source", source).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaUserEntity findByMobile(final String mobile, final String source) {
        val query = String.format("SELECT r FROM %s r WHERE r.mobile=:mobile and r.source=:source",
                JpaUserEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaUserEntity.class).setParameter("mobile",
                mobile).setParameter(
                "source", source).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void saveOrUpdate(final JpaUserEntity entity) {
        this.transactionTemplate.execute(status -> {
            val isNew = entity.getId() == -1;
            if (isNew) {
                entityManager.persist(entity);
            }
            entityManager.merge(entity);
            return entity;
        });
    }


}
