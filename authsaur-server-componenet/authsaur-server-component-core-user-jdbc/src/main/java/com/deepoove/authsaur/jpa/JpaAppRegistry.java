package com.deepoove.authsaur.jpa;

import lombok.ToString;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@EnableTransactionManagement
@Transactional(transactionManager = JpaAppRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
public class JpaAppRegistry {

    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerApp";

    @PersistenceContext(unitName = "appEntityManagerFactory")
    private EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;

    public JpaAppRegistry(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public boolean delete(final long id) {
        val entity = findById(id);
        entityManager.remove(entity);
        return true;
    }

    public boolean deleteByApp(final long app) {
        val entity = findByApp(app);
        if (null == entity) return true;
        return delete(entity.getId());
    }

    public JpaAppEntity findById(final long id) {
        return Optional.ofNullable(this.entityManager.find(JpaAppEntity.class, id)).orElse(null);
    }


    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaAppEntity findByApp(final long app) {
        val query = String.format("SELECT r FROM %s r WHERE r.appId=:appId", JpaAppEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaAppEntity.class).setParameter("appId",
                app).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void saveOrUpdate(final JpaAppEntity entity) {
        this.transactionTemplate.execute(status -> {
            JpaAppEntity byApp = findByApp(entity.getAppId());
            if (null == byApp) {
                entityManager.persist(entity);
                return entity;
            } else {
                entity.setId(byApp.getId());
                entityManager.merge(entity);
                return entity;
            }
        });
    }
}
