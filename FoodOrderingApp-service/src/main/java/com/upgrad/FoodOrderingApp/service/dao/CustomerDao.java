package com.upgrad.FoodOrderingApp.service.dao;


import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create new customer in database.
     *
     * @param customerEntity : userEntity body
     * @return User details
     */
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) {
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    /**
     * Method to get customer by contactNumber
     *
     * @param contactNumber : Fetch customer via contactNumber
     * @return customer details
     */
    public CustomerEntity getCustomerByContactNumber(final String contactNumber) {
        try{
            return entityManager.createNamedQuery("customerByContactNumber", CustomerEntity.class).setParameter("contact_number", contactNumber).getSingleResult();
        }catch(NoResultException nre) {
            return null;
        }
    }

    /**
     * Method to update user in database
     *
     * @param updateCustomerEntity : CustomerEntity body
     * @return updated response
     */
    public void updateCustomerEntity(final CustomerEntity updateCustomerEntity) {
        entityManager.merge(updateCustomerEntity);
    }

    public CustomerAuthEntity getCustomerAuthToken(final String accessToken) {
        try{
            return entityManager.createNamedQuery("cutomerAuthByAccessToken", CustomerAuthEntity.class).setParameter("access_token", accessToken).getSingleResult();
        }catch (NoResultException nre) {
            return null;
        }
    }

    public CustomerEntity updateCustomerDetails(CustomerEntity customer) {
        try{
            return entityManager.merge(customer);
        }catch (NoResultException nre) {
            return null;
        }
    }

    //To get Customer By Uuid if no results return null
    public CustomerEntity getCustomerByUuid (final String uuid){
        try {
            return entityManager.createNamedQuery("customerByUuid",CustomerEntity.class).setParameter("uuid",uuid).getSingleResult();
        }catch (NoResultException nre){
            return null;
        }
    }

    //To update customer
    public CustomerEntity updateCustomer(CustomerEntity customerToBeUpdated){
        entityManager.merge(customerToBeUpdated);
        return customerToBeUpdated;
    }
}