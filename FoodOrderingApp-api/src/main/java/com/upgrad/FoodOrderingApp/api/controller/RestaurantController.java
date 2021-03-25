package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantBusinessService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/")
public class RestaurantController {


    @Autowired
    RestaurantBusinessService restaurantBusinessService;

    @Autowired
    private CategoryBusinessService categoryBusinessService;

    @RequestMapping(method = RequestMethod.GET, path = "/restaurant", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getAllRestaurants() {


        // Getting the list of all restaurants with help of restaurant business service
        final List<RestaurantEntity> allRestaurants = restaurantBusinessService.getAllRestaurants();

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

        // Adding the list of restaurants to RestaurantList
        List<RestaurantList> restaurantLists = new ArrayList<>();
        for (RestaurantEntity n: allRestaurants) {
            RestaurantList restaurant = new RestaurantList();
            restaurant.setId(UUID.fromString(n.getUuid()));
            restaurant.setRestaurantName(n.getRestaurantName());
            restaurant.setPhotoURL(n.getPhotoUrl());
            restaurant.setCustomerRating(n.getCustomerRating());
            restaurant.setAveragePrice(n.getAvgPriceForTwo());
            restaurant.setNumberCustomersRated(n.getNumCustomersRated());

             /*// Getting address of restaurant from address entity

            // Getting state for current address from state entity*/

            // Looping categories and setting name values only
            List<String> categoryLists = new ArrayList();
            for (CategoryEntity categoryEntity :n.getCategoryEntities()) {
                categoryLists.add(categoryEntity.getCategoryName());
            }

            // Sorting category list on name
            Collections.sort(categoryLists);

            // Joining List items as string with comma(,)
            restaurant.setCategories(String.join(",", categoryLists));

            // Add category restaurant to restaurantLists(RestaurantList)
            //restaurantLists.add(restaurant);
            restaurantListResponse.addRestaurantsItem(restaurant);
        }

        // return response entity with RestaurantLists(restaurantLists) and Http status
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/name/{restaurant_name}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByName(@PathVariable(
            "restaurant_name") final String restaurantName) throws RestaurantNotFoundException {

        // Getting the list of all restaurants with help of restaurant business service based on input restaurant name
        final List<RestaurantEntity> allRestaurants = restaurantBusinessService.getRestaurantsByName(restaurantName);

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

        // Adding the list of restaurants to RestaurantList
        List<RestaurantList> restaurantLists = new ArrayList<RestaurantList>();

        for (RestaurantEntity n: allRestaurants) {
            RestaurantList restaurant = new RestaurantList();
            restaurant.setId(UUID.fromString(n.getUuid()));
            restaurant.setRestaurantName(n.getRestaurantName());
            restaurant.setPhotoURL(n.getPhotoUrl());
            restaurant.setCustomerRating(n.getCustomerRating());
            restaurant.setAveragePrice(n.getAvgPriceForTwo());
            restaurant.setNumberCustomersRated(n.getNumCustomersRated());

        /*    // Getting address of restaurant from address entity
            // Getting state for current address from state entity
            // Setting address with state into restaurant obj*/


            // Looping categories and setting name values only
            List<String> categoryLists = new ArrayList();
            for (CategoryEntity categoryEntity :n.getCategoryEntities()) {
                categoryLists.add(categoryEntity.getCategoryName());
            }

            // Sorting category list on name
            Collections.sort(categoryLists);

            // Joining List items as string with comma(,)
            restaurant.setCategories(String.join(",", categoryLists));

            // Add category restaurant to restaurantLists(RestaurantList)
            //restaurantLists.add(restaurant);

            restaurantListResponse.addRestaurantsItem(restaurant);

        }

        // return response entity with RestaurantLists(restaurantLists) and Http status
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/category/{category_id}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<RestaurantList>> getRestaurantByCategoryID(@PathVariable(
            "category_id") final String categoryID) throws CategoryNotFoundException, RestaurantNotFoundException {

        // Throw exception if path variable(category_id) is empty
        if(categoryID == null || categoryID.isEmpty() || categoryID.equalsIgnoreCase("\"\"")){
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        // Getting category which matched with given category_id with help of category business service
        CategoryEntity matchedCategory = categoryBusinessService.getCategoryEntityByUuid(categoryID);

        // Throw exception if given category_id not matched with any category in DB
        if(matchedCategory == null){
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }

        // If given category_id match with any category in DB, then get all restaurants having matched category
        final List<RestaurantCategoryEntity> allRestaurantCategories = restaurantBusinessService.getRestaurantByCategoryId(matchedCategory.getId());

        // Adding the list of restaurants to RestaurantList
        List<RestaurantList> details = new ArrayList<>();
        for (RestaurantCategoryEntity restaurantCategoryEntity:allRestaurantCategories) {
            RestaurantEntity n = restaurantCategoryEntity.getRestaurant();
            RestaurantList detail = new RestaurantList();
            detail.setId(UUID.fromString(n.getUuid()));
            detail.setRestaurantName(n.getRestaurantName());
            detail.setPhotoURL(n.getPhotoUrl());
            detail.setCustomerRating(n.getCustomerRating());
            detail.setAveragePrice(n.getAvgPriceForTwo());
            detail.setNumberCustomersRated(n.getNumCustomersRated());

            // Getting address of restaurant from address entity
            AddressEntity addressEntity = addressService.getAddressById(n.getAddress().getId());
            RestaurantDetailsResponseAddress responseAddress = new RestaurantDetailsResponseAddress();

            responseAddress.setId(UUID.fromString(addressEntity.getUuid()));
            responseAddress.setFlatBuildingName(addressEntity.getFlatBuildingNumber());
            responseAddress.setLocality(addressEntity.getLocality());
            responseAddress.setCity(addressEntity.getCity());
            responseAddress.setPincode(addressEntity.getPincode());

            // Getting state for current address from state entity
            StateEntity stateEntity = stateBusinessService.getStateById(addressEntity.getState().getId());
            RestaurantDetailsResponseAddressState responseAddressState = new RestaurantDetailsResponseAddressState();

            responseAddressState.setId(UUID.fromString(stateEntity.getUuid()));
            responseAddressState.setStateName(stateEntity.getStateName());
            responseAddress.setState(responseAddressState);

            // Setting address with state into restaurant obj
            detail.setAddress(responseAddress);

            // Looping categories and setting name values only
            List<String> categoryLists = new ArrayList();
            for (CategoryEntity categoryEntity :n.getCategoryEntities()) {
                categoryLists.add(categoryEntity.getCategoryName());
            }

            // Sorting category list on name
            Collections.sort(categoryLists);

            // Joining List items as string with comma(,)
            detail.setCategories(String.join(",", categoryLists));

            // Add category detail to details(RestaurantList)
            details.add(detail);
        }

        // return response entity with RestaurantLists(details) and Http status
        return new ResponseEntity<>(details, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/api/restaurant/{restaurant_id}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantDetailsResponse> getRestaurantByRestaurantID(@PathVariable(
            "restaurant_id") final String restaurantID) throws RestaurantNotFoundException {

        RestaurantDetailsResponse restaurantDetailsResponse = new RestaurantDetailsResponse();

        return new ResponseEntity<RestaurantDetailsResponse>(restaurantDetailsResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PUT, path = "/api/restaurant/{restaurant_id}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantUpdatedResponse> updateRestaurantByRestaurantID(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("restaurant_id") final String restaurantID,
            @RequestParam("customer_rating") final Double customerRating)
            throws AuthorizationFailedException, RestaurantNotFoundException, InvalidRatingException {

        RestaurantUpdatedResponse restaurantUpdatedResponse = new RestaurantUpdatedResponse();

        return new ResponseEntity<RestaurantUpdatedResponse>(restaurantUpdatedResponse,HttpStatus.OK);
    }

}
