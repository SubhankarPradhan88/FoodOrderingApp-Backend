package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService_Old;
import com.upgrad.FoodOrderingApp.service.common.UnexpectedException;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RestController
public class AddressController {

    @Autowired private CustomerService_Old customerServiceOld;

    @Autowired private AddressService addressService;

    /**
     * Method takes Customer's SaveAddress request, stores customer address into DB.
     *
     * @param saveAddressRequest
     * @return ResponseEntity with Address Id
     * @throws SaveAddressException on invalid saveAddress request
     * @throws AddressNotFoundException on invalid state id
     * @throws AuthorizationFailedException on invalid customer access-token
     * @throws UnexpectedException on any other errors
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/address",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(
            @RequestHeader("authorization") final String authorization,
            @RequestBody(required = false) SaveAddressRequest saveAddressRequest)
            throws AuthorizationFailedException, AddressNotFoundException, SaveAddressException {

        // Get Bearer Authorization Token.
        String accessToken = CustomerAuthDao.getBearerAuthToken(authorization);

        // Get customer details on successful authentication of accessToken.
        CustomerEntity customerEntity = customerServiceOld.getCustomer(accessToken);

        // Generate Address entity for insert.
        AddressEntity address = new AddressEntity();

        address.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        address.setCity(saveAddressRequest.getCity());
        address.setLocality(saveAddressRequest.getLocality());
        address.setPincode(saveAddressRequest.getPincode());
        address.setUuid(UUID.randomUUID().toString());

        address.setActive(1);
        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());
        address.setCustomers(customerEntity);

        // Store Address entity in the database.
        AddressEntity savedAddress = addressService.saveAddress(address, stateEntity);

        // Map persisted Address Entity to Response Object.
        SaveAddressResponse addressResponse =
                new SaveAddressResponse()
                        .id(savedAddress.getUuid())
                        .status("ADDRESS SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SaveAddressResponse>(addressResponse, HttpStatus.CREATED);
    }

    /**
     * This method takes authorization from customer, returns all address of authorised user.
     *
     * @param authorization  access token as request header
     * @return ResponseEntity with list of Addresses
     * @throws AuthorizationFailedException on invalid customer access-token
     * @throws UnexpectedException on any other errors
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/address/customer",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddressListResponse> getAllAddresses(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {

        // Get Bearer Authorization Token.
        final String accessToken = CustomerAuthDao.getBearerAuthToken(authorization);

        // Get customer details on successful authentication of accessToken.
        final CustomerEntity customerEntity = customerServiceOld.getCustomer(accessToken);

        // Retrieve addresses from database.
        List<AddressEntity> sortedAddress = addressService.getAllAddress(customerEntity);

        List<AddressList> addressesList = new ArrayList<>();

        // Map retrieved Address Entity to Response Object List.
        sortedAddress.forEach(
                address -> {
                    AddressListState addressListState = new AddressListState();
                    addressListState.setId(UUID.fromString(address.getState().getUuid()));
                    addressListState.setStateName(address.getState().getStateName());

                    AddressList addressList =
                            new AddressList()
                                    .id(UUID.fromString(address.getUuid()))
                                    .flatBuildingName(address.getFlatBuilNo())
                                    .city(address.getCity())
                                    .locality(address.getLocality())
                                    .pincode(address.getPincode())
                                    .state(addressListState);
                    addressesList.add(addressList);
                });

        AddressListResponse addressListResponse = new AddressListResponse().addresses(addressesList);
        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }

    /**
     * This Method takes address id from customer, delete address of authorised user.
     *
     * @param authorization Customer access token in header
     * @param addressId Address id to be deleted
     * @return ResponseEntity with id of address deleted
     * @throws AddressNotFoundException if invalid address id
     * @throws AuthorizationFailedException if invalid customer access-token
     * @throws UnexpectedException if any other errors
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.DELETE,
            path = "/address/{address_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(
            @RequestHeader("authorization") final String authorization,
            @PathVariable(value = "address_id") final String addressId)
            throws AuthorizationFailedException, AddressNotFoundException {
        // Get Bearer Authorization Token.
        String accessToken = CustomerAuthDao.getBearerAuthToken(authorization);

        // Get customer details on successful authentication of accessToken.
        CustomerEntity customerEntity = customerServiceOld.getCustomer(accessToken);

        // Get address entity of address to be deleted.
        AddressEntity address = addressService.getAddressByUUID(addressId, customerEntity);
        if (address == null){//Checking if null throws corresponding exception.
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        AddressEntity deletedAddress;

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse();

        address.setActive(0);
        deletedAddress = addressService.deleteAddress(address);

        // Map deleted Address status & id to Response Object.
        deleteAddressResponse.id(UUID.fromString(deletedAddress.getUuid()))
                .status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

    /**
     * This method  returns all states json.
     * @return ResponseEntity with list of States
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/states",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatesListResponse> getAllStates() {

        // Retrieve all states.
        List<StateEntity> states = addressService.getAllStates();

        // Map retrieved State Entity with Response Object List.
        if (!states.isEmpty()) {
            List<StatesList> statesList = new LinkedList<>();
            states.forEach(
                    state -> {
                        StatesList stateList = new StatesList();
                        stateList.setId(UUID.fromString(state.getUuid()));
                        stateList.setStateName(state.getStateName());

                        statesList.add(stateList);
                    });
            StatesListResponse statesListResponse = new StatesListResponse().states(statesList);
            return new ResponseEntity<StatesListResponse>(statesListResponse, HttpStatus.OK);
        } else return new ResponseEntity<StatesListResponse>(new StatesListResponse(), HttpStatus.OK);
    }
}
