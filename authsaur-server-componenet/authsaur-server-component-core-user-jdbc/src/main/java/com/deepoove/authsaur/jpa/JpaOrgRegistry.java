package com.deepoove.authsaur.jpa;

import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@EnableTransactionManagement
@Transactional(transactionManager = JpaOrgRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
public class JpaOrgRegistry {

    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerOrg";

    @PersistenceContext(unitName = "orgEntityManagerFactory")
    private EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;

    public JpaOrgRegistry(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public boolean delete(final String id) {
//        List<JpaOrgEntity> jpaOrgEntities = querySubByParentId(id);
//        if (jpaOrgEntities.size() >= 1) {
//            throw new IllegalStateException("部门存在子部门，无法删除");
//        }
        val entity = findById(id);
        entityManager.remove(entity);
        return true;
    }


    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgEntity> queryDirectory() {
        val query = String.format("SELECT r FROM %s r WHERE r.parentId is null", JpaOrgEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaOrgEntity.class).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgEntity> queryByOrgList(List<String> orgIds) {
        val query = String.format("SELECT r FROM %s r WHERE r.id in :orgIds", JpaOrgEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("orgIds",
                orgIds).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgEntity> queryByName(String source, String name) {
        if (StringUtils.isBlank(source)) {
            val query = String.format("SELECT r FROM %s r WHERE r.name LIKE concat(:name,'%%')",
                    JpaOrgEntity.ENTITY_NAME);
            val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("name",
                    name).getResultList();
            return list;
        }
        val query = String.format("SELECT r FROM %s r WHERE r.source=:source AND r.name LIKE concat(:name,'%%')",
                JpaOrgEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("source",
                source).setParameter("name", name).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgEntity> querySubByParentId(String source, String parentId) {
        if (StringUtils.isBlank(parentId)) {
            val query = String.format("SELECT r FROM %s r WHERE r.source=:source AND r.parentId is null",
                    JpaOrgEntity.ENTITY_NAME);
            val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("source",
                    source).getResultList();
            return list;
        }
        val query = String.format("SELECT r FROM %s r WHERE r.parentId=:parentId", JpaOrgEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("parentId",
                parentId).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgEntity> queryAllSubByParentId(String source, String parentId) {
        if (StringUtils.isBlank(parentId)) {
            val query = String.format("SELECT r FROM %s r WHERE r.source=:source", JpaOrgEntity.ENTITY_NAME);
            val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("source",
                    source).getResultList();
            return list;
        }
        JpaOrgEntity byId = findById(parentId);
        val query = String.format("SELECT r FROM %s r WHERE r.path LIKE concat(:path,'%%')", JpaOrgEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("path",
                byId.getPath() + "/").getResultList();
        return list;
    }

    public JpaOrgEntity findById(final String id) {
        return Optional.ofNullable(this.entityManager.find(JpaOrgEntity.class, id)).orElse(null);
    }

    public void saveOrUpdate(final JpaOrgEntity entity) {
        this.transactionTemplate.execute(status -> {
            val isNew = entity.getId() == null;
            if (isNew) {
                entityManager.persist(entity);
            }
            entityManager.merge(entity);
            return entity;
        });
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaOrgEntity findRootBySource(final String source) {
        val query = String.format("SELECT r FROM %s r WHERE r.source=:source and r.parentId is null",
                JpaOrgEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaOrgEntity.class).setParameter("source",
                source).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaOrgEntity> queryAllBySource(String source) {
        val query = String.format("SELECT r FROM %s r WHERE r.source=:source", JpaOrgEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaOrgEntity.class).setParameter("source",
                source).getResultList();
        return list;
    }
}
