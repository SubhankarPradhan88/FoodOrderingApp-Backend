package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/")

public class CustomerController {
    @Autowired
    private CustomerService customerService;

    /**
     * Request mapping for user signup. This method receives the object of SignupUserRequest type with its attributes being set.
     *
     * @return SignupCustomerResponse - UUID of the user created.
     * @throws SignUpRestrictedException - if the username or email already exist in the database.
     */

    // Translate request model into entity model, and pass down the entity object to the business service, to persist the data in the DB
    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid((UUID.randomUUID().toString()));
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        // Handle the response object that will be send to the caller
        final CustomerEntity createdCustomerEntity = customerService.signup(customerEntity);
        SignupCustomerResponse customerResponse = new SignupCustomerResponse().id(createdCustomerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);
    }

    /**
     * Request mapping for a user to login.
     *
     * @param authorization for the basic authentication
     * @return Signin response which has userId and access-token in response header.
     * @throws AuthenticationFailedException : if username or password is invalid
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {
        final String[] decodedArray;
        try{
            final byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            final String decodedText = new String(decode);
            decodedArray = decodedText.split(":");
            if(decodedArray.length != 2) {
                throw new Exception();
            }
        }catch(Exception e) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        final CustomerAuthEntity customerAuthEntity = customerService.login(decodedArray[0], decodedArray[1]);
        final CustomerEntity customerEntity = customerAuthEntity.getCustomer();

        final HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());

        final LoginResponse loginResponse = new LoginResponse().id(customerEntity.getUuid()).firstName(customerEntity.getFirstName())
                .lastName(customerEntity.getLastName()).emailAddress(customerEntity.getEmailAddress())
                .contactNumber(customerEntity.getContactNumber()).message("SIGNED IN SUCCESSFULLY");

        loginResponse.setMessage("LOGGED IN SUCCESSFULLY");
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    /**
     * Request mapping to log-out customer
     *
     * @param accessToken
     * @return LogoutResponse
     * @throws AuthorizationFailedException
     */

    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> signOut(@RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException {
        final CustomerEntity customerEntity = customerService.logout(accessToken);

        final LogoutResponse logoutResponse = new LogoutResponse().id(customerEntity.getUuid()).message("LOGGED OUT SUCCESSFULLY");
        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    /**
     * Request mapping to update-password of customer
     *
     * @param accessToken
     * @return UpdatePasswordResponse
     * @throws UpdateCustomerException
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/customer/password", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updateCustomerPassword(@RequestHeader("authorization") final String accessToken,
                                                                         @RequestBody UpdatePasswordRequest updatePasswordRequest) throws UpdateCustomerException, AuthorizationFailedException {
        System.out.println("Access token from UPDATECUSTOMERPASSWORD:\t\t" + accessToken);
        final CustomerEntity customerEntity = customerService.updateCustomerPassword(updatePasswordRequest.getOldPassword(),
                updatePasswordRequest.getNewPassword(), accessToken);

        final UpdatePasswordResponse response = new UpdatePasswordResponse().id(customerEntity.getUuid()).status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdatePasswordResponse>(response, HttpStatus.OK);
    }

    /**
     * Request mapping to update-customer-details of customer
     *
     * @param authorization
     * @return UpdateCustomerResponse
     * @throws UpdateCustomerException
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateCustomerDetails(@RequestHeader("authorization") final String authorization,
                                                                        @RequestBody(required = false) UpdateCustomerRequest updateCustomerRequest) throws UpdateCustomerException, AuthorizationFailedException {
        System.out.println("Access token from updateCustomerDetails:\t\t" + authorization);

        // Access the accessToken from the request Header
        String accessToken = authorization.split("Bearer ")[1];

        // Calls utilityService getCustomer method to check the validity of the customer. This methods returns the customerEntity to be updated.
        CustomerEntity toBeUpdatedCustomerEntity = customerService.getCustomer(accessToken);

        // Update the customer entity
        toBeUpdatedCustomerEntity.setFirstName(updateCustomerRequest.getFirstName());
        toBeUpdatedCustomerEntity.setLastName(updateCustomerRequest.getLastName());

        //  Calls utilityService updateCustomer to persist the updated Entity.
        CustomerEntity updatedCustomerEntity = customerService.updateCustomer(toBeUpdatedCustomerEntity);

        //  Creating the Update CustomerResponse with updated details.
        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse()
                .firstName(updatedCustomerEntity.getFirstName())
                .lastName(updatedCustomerEntity.getLastName())
                .id(updatedCustomerEntity.getUuid())
                .status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse,HttpStatus.OK);
    }

}