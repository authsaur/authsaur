package com.deepoove.authsaur.jpa;

import lombok.ToString;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EnableTransactionManagement
@Transactional(transactionManager = JpaAuthenticationHandlerRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
public class JpaAuthenticationHandlerRegistry {

    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerAuth";

    @PersistenceContext(unitName = "authEntityManagerFactory")
    private EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;

    public JpaAuthenticationHandlerRegistry(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public boolean delete(final String id) {
        val entity = findById(id);
        entityManager.remove(entity);
        return true;
    }

    public boolean delete(final JpaAuthenticationEntity entity) {
        entityManager.remove(entity);
        return true;
    }

    public boolean deleteByName(final String name) {
        val entity = findByName(name);
        entityManager.remove(entity);
        return true;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaAuthenticationEntity> query() {
        val query = String.format("SELECT r FROM %s r", JpaAuthenticationEntity.ENTITY_NAME);
        val list = this.entityManager.createQuery(query, JpaAuthenticationEntity.class).getResultList();
        return list;
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public List<JpaAuthenticationEntity> queryActiveMFA() {
        val query = "SELECT r FROM " + JpaAuthenticationEntity.ENTITY_NAME + " r WHERE r.type LIKE 'mfa%' AND r.state" +
                " = 'ACTIVE'";
        val list = this.entityManager.createQuery(query, JpaAuthenticationEntity.class).getResultList();
        return list;
    }

    public JpaAuthenticationEntity findById(final String id) {
        return Optional.ofNullable(this.entityManager.find(JpaAuthenticationEntity.class, id)).orElse(null);
    }

    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaAuthenticationEntity findByName(final String name) {
        val query = String.format("SELECT r FROM %s r WHERE r.name=:name", JpaAuthenticationEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaAuthenticationEntity.class)
                .setParameter("name", name)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void saveOrUpdate(final JpaAuthenticationEntity entity) {
        this.transactionTemplate.execute(status -> {
            val isNew = "-1".equals(entity.getId());
            if (isNew) {
                entity.setId(UUID.randomUUID().toString().substring(0, 6));
                entityManager.persist(entity);
                return entity;
            } else {
                JpaAuthenticationEntity entity1 = this.entityManager.find(JpaAuthenticationEntity.class,
                        entity.getId());
                if (null == entity1) {
                    entityManager.persist(entity);
                    return entity;
                }
            }
            entityManager.merge(entity);
            return entity;
        });
    }
}
