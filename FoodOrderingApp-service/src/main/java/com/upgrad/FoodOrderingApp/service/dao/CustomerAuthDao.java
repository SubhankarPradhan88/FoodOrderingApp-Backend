package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerAuthDao {
    @PersistenceContext
    private EntityManager entityManager;
    /**
     * get User auth by token
     *
     * @param accessToken : access token to authenticate
     * @return single user auth details
     */
    public CustomerAuthTokenEntity getCustomer(final String accessToken) {
        try {
            return entityManager
                    .createNamedQuery("cutomerAuthByAccessToken", CustomerAuthTokenEntity.class)
                    .setParameter("access_Token", accessToken)
                    .getSingleResult();
        }catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Persists customer auth entity in database.
     *
     * @param customerAuthTokenEntity to be persisted in the DB.
     * @return UserAuthEntity
     */
    public CustomerAuthTokenEntity createAuthToken(final CustomerAuthTokenEntity customerAuthTokenEntity) {
        entityManager.persist(customerAuthTokenEntity);
        return customerAuthTokenEntity;
    }
    /**
     * Update UserAuthEntity in Database
     *
     * @param updatedUserAuthEntity: CustomerAuthTokenEntity object
     */
    public void updateCustomerAuth(final CustomerAuthTokenEntity updatedUserAuthEntity) {
        entityManager.merge(updatedUserAuthEntity);
    }
}
