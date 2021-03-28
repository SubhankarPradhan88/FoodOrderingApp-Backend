package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;

@Service
public class CustomerService_Old {

    @Autowired private CustomerDao customerDao;

    /**
     * helper method to check the authentication of user through accesstoken
     *
     * @param accessToken token of the customer
     * @return CustomerAuthentity object
     * @throws AuthorizationFailedException exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity getCustomerAuthEntity(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(accessToken);
        if (customerAuthEntity == null) {
            //if access token does not exist then throw ATHR-001
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthEntity.getLogoutAt() != null) {
            //if customer with this accestoken has already logged out then throw ATHR-002
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (ZonedDateTime.now().isAfter(customerAuthEntity.getExpiresAt())) {
            //if expiry date of this token is already past the current date then throw ATHR-003
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        return customerAuthEntity;
    }

    /**
     * Method takes customer's access token and fetches his details
     *
     * @param accessToken Customer's accessToken
     * @return CustomerAuthEntity with customer's authentication information
     * @throws AuthorizationFailedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity getCustomer(final String accessToken) throws AuthorizationFailedException {
        final CustomerAuthEntity customerAuthEntity =
                getCustomerAuthEntity(accessToken);
        return customerAuthEntity.getCustomer();
    }
}
