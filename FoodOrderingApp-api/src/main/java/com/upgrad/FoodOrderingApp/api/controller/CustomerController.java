package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.UtilityService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
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
    private UtilityService utilityService;

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
        customerEntity.setFirstname(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        // Handle the response object that will be send to the caller
        final CustomerEntity createdCustomerEntity = utilityService.signup(customerEntity);
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
        final CustomerAuthTokenEntity customerAuthTokenEntity = utilityService.login(decodedArray[0], decodedArray[1]);
        final CustomerEntity customerEntity = customerAuthTokenEntity.getCustomer();

        final HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthTokenEntity.getAccessToken());

        final LoginResponse loginResponse = new LoginResponse().id(customerEntity.getUuid()).firstName(customerEntity.getFirstName())
                .lastName(customerEntity.getLastName()).emailAddress(customerEntity.getEmailAddress())
                .contactNumber(customerEntity.getContactNumber()).message("SIGNED IN SUCCESSFULLY");

        loginResponse.setMessage("LOGGED IN SUCCESSFULLY");
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }
}

