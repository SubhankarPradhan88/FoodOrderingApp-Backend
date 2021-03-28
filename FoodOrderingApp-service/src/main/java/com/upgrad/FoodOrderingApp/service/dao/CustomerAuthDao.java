package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.common.UnexpectedException;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.ATHR_005;
import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.GEN_001;

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
                    .createNamedQuery("cutomerAuthByAccessToken", CustomerAuthTokenEntity.class).setParameter("access_token", accessToken).getSingleResult();
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

    /**
     * Takes a basic authorization token removes prefix
     *
     * @param headerParam Basic Authorization Token
     * @return Basic Authorization Token with Prefix Removed
     */
    public static String getBearerAuthToken(String headerParam) {
        if (!headerParam.startsWith("Bearer ")) {
            throw new UnexpectedException(ATHR_005);
        } else {
            String bearerToken = StringUtils.substringAfter(headerParam, "Bearer ");
            if (bearerToken == null || bearerToken.isEmpty()) {
                throw new UnexpectedException(GEN_001);
            } else {
                return bearerToken;
            }
        }
    }
}

