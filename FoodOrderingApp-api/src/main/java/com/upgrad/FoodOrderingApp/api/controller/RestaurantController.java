package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.RestaurantDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.RestaurantList;
import com.upgrad.FoodOrderingApp.api.model.RestaurantListResponse;
import com.upgrad.FoodOrderingApp.api.model.RestaurantUpdatedResponse;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantBusinessService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
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
    public ResponseEntity<RestaurantListResponse> getRestaurantByCategoryID(@PathVariable(
            "category_id") final String categoryID) throws CategoryNotFoundException {

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
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
