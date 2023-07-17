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
@Transactional(transactionManager = JpaAppConfigRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
public class JpaAppConfigRegistry {

    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerAppConfig";

    @PersistenceContext(unitName = "appConfigEntityManagerFactory")
    private EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;

    public JpaAppConfigRegistry(TransactionTemplate transactionTemplate) {
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

    public JpaAppConfigEntity findById(final long id) {
        return Optional.ofNullable(this.entityManager.find(JpaAppConfigEntity.class, id)).orElse(null);
    }


    @Transactional(transactionManager = BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public JpaAppConfigEntity findByApp(final long app) {
        val query = String.format("SELECT r FROM %s r WHERE r.appId=:appId", JpaAppConfigEntity.ENTITY_NAME);
        val results = entityManager.createQuery(query, JpaAppConfigEntity.class).setParameter("appId",
                app).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void saveOrUpdate(final JpaAppConfigEntity entity) {
        this.transactionTemplate.execute(status -> {
            JpaAppConfigEntity byApp = findByApp(entity.getAppId());
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
