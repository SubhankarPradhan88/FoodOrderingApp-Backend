package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UtilityService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

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
