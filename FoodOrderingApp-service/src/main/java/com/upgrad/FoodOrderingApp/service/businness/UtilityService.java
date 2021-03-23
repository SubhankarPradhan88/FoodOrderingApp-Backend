package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UtilityService {

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
    public CustomerEntity signup(CustomerEntity customerEntity) throws SignUpRestrictedException {
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

        return customerDao.createCustomer(customerEntity);
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
    public CustomerAuthTokenEntity login(final String contactNumber, final String password) throws AuthenticationFailedException {
        final CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);

        if(customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());

        if(!encryptedPassword.equals(customerEntity.getPassword())) {
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
        final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
        final CustomerAuthTokenEntity customerAuthTokenEntity = new CustomerAuthTokenEntity();
        customerAuthTokenEntity.setUuid(UUID.randomUUID().toString());
        customerAuthTokenEntity.setCustomer(customerEntity);
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime expiresAt = now.plusHours(8);
        customerAuthTokenEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));
        customerAuthTokenEntity.setLoginAt(now);
        customerAuthTokenEntity.setExpiresAt(expiresAt);

        customerAuthDao.createAuthToken(customerAuthTokenEntity);
        customerDao.updateCustomerEntity(customerEntity);
        return customerAuthTokenEntity;
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
        if(customer.getEmailAddress().length() == 0 || customer.getContactNumber().length() == 0 ||
        customer.getFirstName().length() == 0 || customer.getPassword().length() == 0) {
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
