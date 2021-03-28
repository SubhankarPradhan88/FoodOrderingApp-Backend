package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CustomerAuthDao customerAuthDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    /**
     * SignUp method for users and add salt, encryption to password
     *
     * @throws SignUpRestrictedException : throw exception if user already exists
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {
        if(isContactNumberInUse(customerEntity)) {
            throw new SignUpRestrictedException("SGR-001","This contact number is already registered! Try other contact number.");
        }
        if(!validateCustomer(customerEntity)) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }
        if(!isValidEmailID(customerEntity)) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }
        if(!isValidPhoneNumber(customerEntity)) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }
        if(!isValidPassword(customerEntity.getPassword())) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        }

        // Encrypted password and salt assigned to the customer that is being created.
        final String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);

        return customerDao.saveCustomer(customerEntity);
    }

    /**
     * the Login user method
     *
     * @param contactNumber : Username that you want to signin
     * @param password : Password of user
     * @throws AuthenticationFailedException : If user not found or invalid password
     * @return CustomerAuthEntity access-token and singin response.
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(final String contactNumber, final String password) throws AuthenticationFailedException {
        final CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);

        if(customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());

        if(!encryptedPassword.equals(customerEntity.getPassword())) {
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
        final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
        final CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
        customerAuthEntity.setUuid(UUID.randomUUID().toString());
        customerAuthEntity.setCustomer(customerEntity);
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime expiresAt = now.plusHours(8);
        customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));
        customerAuthEntity.setLoginAt(now);
        customerAuthEntity.setExpiresAt(expiresAt);

        customerAuthDao.createAuthToken(customerAuthEntity);
        customerDao.updateCustomerEntity(customerEntity);
        return customerAuthEntity;
    }

    /**
     * The signout method
     *
     * @param accessToken : required to logout the user
     * @throws AuthorizationFailedException : if the access-token is not found in the DB.
     * @return CustomerEntity : the customer who has been logged out.
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity logout(final String accessToken) throws AuthorizationFailedException {
        final CustomerAuthEntity customerAuthEntity = customerAuthDao.getCustomer(accessToken);
        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }else if(customerAuthEntity != null && customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }else if (customerAuthEntity != null && customerAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }else {
            customerAuthEntity.setLogoutAt(ZonedDateTime.now());
            customerAuthDao.updateCustomerAuth(customerAuthEntity);
            return customerAuthEntity.getCustomer();
        }
    }

    /**
     * The updateCustomerPassword method
     *
     * @param accessToken : required to signout the user
     * @throws UpdateCustomerException : if the access-token is not found in the DB.
     * @return CustomerEntity : the customer whose account password has been updated.
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(String oldPassword, String newPassword, String accessToken)
            throws UpdateCustomerException, AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(accessToken);
        if(oldPassword.length()==0 || newPassword.length()==0) {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        if(customerAuthEntity != null && customerAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        if(customerAuthEntity != null && customerAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        if(!isValidPassword(newPassword)) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        CustomerEntity existingRecord = customerAuthEntity.getCustomer();
        final String encryptedOldPassword = cryptographyProvider.encrypt(oldPassword, existingRecord.getSalt());
        if(!encryptedOldPassword.equals(existingRecord.getPassword())) {
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }
        final String encryptedNewPassword = cryptographyProvider.encrypt(newPassword, existingRecord.getSalt());
        existingRecord.setPassword(encryptedNewPassword);
        customerDao.updateCustomerDetails(existingRecord);
        return existingRecord;
    }

    /**
     * The getCustomer method
     *
     * @param accessToken : required to signout the user
     * @throws AuthorizationFailedException : if the access-token is not found in the DB.
     * @return CustomerEntity : the selected customer current details in the DB
     */

    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(accessToken);
        //  Checking if Customer not logged In
        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        //  Checking if customer is logged Out
        if(customerAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();
        //  Checking accessToken is Expired
        if(customerAuthEntity.getExpiresAt().compareTo(now) <= 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        return customerAuthEntity.getCustomer();
    }

    /**
     * The updateCustomer method
     *
     * @param customerEntity : selected customer whose details will be updated
     * @throws UpdateCustomerException : if the customer is not found in the DB.
     * @return CustomerEntity : update the selected customer current details and persist in the DB
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(CustomerEntity customerEntity, String accessToken) throws UpdateCustomerException, AuthorizationFailedException {

        //  Getting the CustomerEntity by getCustomerByUuid of customerDao
        CustomerEntity customerToBeUpdated = customerDao.getCustomerByUuid(customerEntity.getUuid());
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(accessToken);

        if(customerEntity.getFirstName().trim() == "") {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }
        if(customerAuthEntity == null) {
            //if access token does not exist then throw ATHR-001
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }else if (customerAuthEntity.getLogoutAt() != null) {
            //if customer with this accestoken has already logged out then throw ATHR-002
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }else if(ZonedDateTime.now().isAfter(customerAuthEntity.getExpiresAt())) {
            //if expiry date of this token is already past the current date then throw ATHR-003
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        //  Setting the new details to the customer entity .
        customerToBeUpdated.setFirstName(customerEntity.getFirstName());
        customerToBeUpdated.setLastName(customerEntity.getLastName());

        //  Calls updateCustomer of customerDao to update the customer data in the DB
        CustomerEntity updatedCustomer = customerDao.updateCustomer(customerEntity);

        return updatedCustomer;
    }

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

    // To check if the username exist in the database
    private boolean isContactNumberInUse(CustomerEntity customer) {
        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(customer.getContactNumber());
        if(customerEntity != null) {
            return true;
        }else {
            return false;
        }
    }
    // Validate mandatory fields for customer registration
    private boolean validateCustomer(CustomerEntity customer) {
        if((customer.getEmailAddress() == "" || customer.getEmailAddress() == null) ||
                (customer.getContactNumber() == "" || customer.getContactNumber() == null) ||
                (customer.getFirstName() == "" || customer.getFirstName() == null) ||
                (customer.getPassword() == "" || customer.getPassword() == null)) {
            return false;
        }else {
            return true;
        }
    }
    // Check for valid emailId format using Regular Exp.
    private boolean isValidEmailID(CustomerEntity customer) {
        Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(customer.getEmailAddress());
        return matcher.find();
    }
    // Check for valid contact no. format using Regular Exp.
    private boolean isValidPhoneNumber(CustomerEntity customer) {
        Pattern pattern = Pattern.compile("^[1-9]+\\d{9}$");
        Matcher matcher = pattern.matcher(customer.getContactNumber());
        return matcher.matches();
    }
    // Check strength of the password using Regular Exp.
    private boolean isValidPassword(String password) {
        Pattern p1 = Pattern.compile("^(?=.*\\d)(?=.*[A-Z])(?=.*\\W).*$");
        Matcher matcher = p1.matcher(password);
        return password.length() >= 8 && matcher.matches();
    }
}
