package com.deepoove.authsaur.jpa;

import lombok.ToString;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@EnableTransactionManagement
@Transactional(transactionManager = JpaOrgUserRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
public class JpaOrgUserRegistry {

    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerOrgUser";

    @PersistenceContext(unitName = "orgUserEntityManagerFactory")
    private EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;
    private final JpaOrgRegistry jpaOrgRegistry;

    public JpaOrgUserRegistry(TransactionTemplate transactionTemplate, JpaOrgRegistry jpaOrgRegistry) {
        this.transactionTemplate = transactionTemplate;
        this.jpaOrgRegistry = jpaOrgRegistry;
    }

//    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
//    public JpaOrgUserEntity findUserByOrgId(final String orgId, final String principal) {
//        val query = String.format("SELECT r FROM %s r WHERE r.orgId=:orgId and r.principal=:principal",
//                JpaOrgUserEntity.ENTITY_NAME);
//        val results = entityManager.createQuery(query, JpaOrgUserEntity.class).setParameter("principal",
//                principal).setParameter("orgId", orgId).getResultList();
//        return results.isEmpty() ? null : results.get(0);
//    }

//    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER)
//    public boolean deleteUserByOrgId(final String orgId, final String principal) {
//        val entity = findUserByOrgId(orgId, principal);
//        if (null != entity) {
//            System.out.println("remoddve entity");
//            entityManager.remove(entity);
//        }
//        return true;
//    }


    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgUserEntity> querySubUserByOrgId(String orgId) {
        val query = String.format(
                "SELECT distinct(r.principal) as principal,r.org_Id as orgId,r.org_Path as orgPath,r.id as id FROM " +
                        "org_user r WHERE r.org_Id=:orgId",
                JpaOrgUserEntity.ENTITY_NAME);
        List<Object[]> resultList = this.entityManager.createNativeQuery(query).setParameter("orgId",
                orgId).getResultList();
        val list = new ArrayList<JpaOrgUserEntity>();
        for (Object[] objects : resultList) {
            JpaOrgUserEntity orgUser = new JpaOrgUserEntity();
            orgUser.setPrincipal(objects[0].toString());
            orgUser.setOrgId(objects[1].toString());
//            orgUser.setId(Long.valueOf(objects[2]));
            list.add(orgUser);
        }
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgUserEntity> queryAllSubUserByOrgId(String orgId) {
        JpaOrgEntity byId = jpaOrgRegistry.findById(orgId);
        val query = String.format(
                "SELECT distinct(r.principal) as principal,r.org_Id as orgId,r.org_Path as orgPath,r.id as id FROM " +
                        "org_user r WHERE r.org_Id=:orgId OR r.org_Path LIKE " +
                        "concat(:orgPath,'%%')",
                JpaOrgUserEntity.ENTITY_NAME);
        String path = byId.getPath() + "/";
        List<Object[]> resultList = this.entityManager.createNativeQuery(query).setParameter("orgId",
                orgId).setParameter("orgPath", path).getResultList();
        val list = new ArrayList<JpaOrgUserEntity>();
        for (Object[] objects : resultList) {
            JpaOrgUserEntity orgUser = new JpaOrgUserEntity();
            orgUser.setPrincipal(objects[0].toString());
            orgUser.setOrgId(objects[1].toString());
//            orgUser.setId(Long.valueOf(objects[2]));
            list.add(orgUser);
        }
        return list;
    }
//
//    public JpaOrgUserEntity findById(final long id) {
//        return Optional.ofNullable(this.entityManager.find(JpaOrgUserEntity.class, id)).orElse(null);
//    }


    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgUserEntity> queryOrgByPrincipal(String principal) {
        val query = String.format("SELECT r FROM %s r WHERE r.principal=:principal", JpaOrgUserEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaOrgUserEntity.class).setParameter("principal",
                principal).getResultList();
        return list;
    }


//    public void saveOrUpdateOrgByPrincipal(final JpaOrgUserEntity entity) {
//        this.transactionTemplate.execute(status -> {
//            val isNew = entity.getId() == -1;
//            if (isNew) {
//                entityManager.persist(entity);
//            }
//            entityManager.merge(entity);
//            return entity;
//        });
//    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER)
    public void saveOrUpdateOrgsByPrincipal(final List<JpaOrgUserEntity> entities) {
        if (null == entities || entities.isEmpty()) return;
        String principal = entities.get(0).getPrincipal();
//        this.transactionTemplate.executeWithoutResult(status -> {
        deleteOrgsByPrincipalId(principal);
        entityManager.flush();
        entities.forEach(e -> {
            entityManager.persist(e);
        });
//        });
    }

    public boolean deleteOrgsByPrincipalId(final String principal) {
        List<JpaOrgUserEntity> jpaOrgUserEntities = queryOrgByPrincipal(principal);
        jpaOrgUserEntities.forEach(org -> {
            val entity = findById(org.getId());
            entityManager.remove(entity);
        });
        return true;
    }

    public JpaOrgUserEntity findById(final long id) {
        return Optional.ofNullable(this.entityManager.find(JpaOrgUserEntity.class, id)).orElse(null);
    }
}
