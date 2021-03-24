package com.upgrad.FoodOrderingApp.service.dao;


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
    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
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
        }catch (NoResultException nre) {
            return null;
        }
    }
}
