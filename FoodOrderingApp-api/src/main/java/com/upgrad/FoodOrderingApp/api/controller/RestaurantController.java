package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.RestaurantDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.RestaurantListResponse;
import com.upgrad.FoodOrderingApp.api.model.RestaurantUpdatedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/")
public class RestaurantController {

    @RequestMapping(method = RequestMethod.GET, path = "/restaurant", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getAllRestaurants() {

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/name/{restaurant_name}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByName(@PathVariable(
            "restaurant_name") final String restaurantName) throws RestaurantNotFoundException {

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

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
