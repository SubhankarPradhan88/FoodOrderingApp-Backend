package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.businness.UtilityService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class RestaurantController {


    @Autowired
    RestaurantService restaurantService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private ItemService itemService;

    /**
     * @return List of all restaurants in the database
     * @throws RestaurantNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getAllRestaurants() throws RestaurantNotFoundException {

        List<RestaurantEntity> restaurantEntityList = restaurantService.restaurantsByRating();

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

        for (RestaurantEntity restaurantEntity : restaurantEntityList) {
            RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = new RestaurantDetailsResponseAddressState()
                    .id(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()))
                    .stateName(restaurantEntity.getAddress().getState().getStateName());

            RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = new RestaurantDetailsResponseAddress()
                    .id(UUID.fromString(restaurantEntity.getAddress().getUuid()))
                    .flatBuildingName(restaurantEntity.getAddress().getFlatBuilNo())
                    .locality(restaurantEntity.getAddress().getLocality())
                    .city(restaurantEntity.getAddress().getCity())
                    .pincode(restaurantEntity.getAddress().getPinCode())
                    .state(restaurantDetailsResponseAddressState);

            String categoriesString = categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid())
                    .stream()
                    .map(o -> String.valueOf(o.getCategoryName()))
                    .collect(Collectors.joining(", "));
            Double temp3 = BigDecimal.valueOf(restaurantEntity.getCustomerRating()).setScale(1, RoundingMode.HALF_UP).doubleValue();
            RestaurantList restaurantList = new RestaurantList()
                    .id(UUID.fromString(restaurantEntity.getUuid()))
                    .restaurantName(restaurantEntity.getRestaurantName())
                    .photoURL(restaurantEntity.getPhotoUrl())
                    .customerRating(new BigDecimal(temp3))
                    .averagePrice(restaurantEntity.getAvgPrice())
                    .numberCustomersRated(restaurantEntity.getNumberCustomersRated())
                    .address(restaurantDetailsResponseAddress)
                    .categories(categoriesString);
            restaurantListResponse.addRestaurantsItem(restaurantList);
        }

        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }


    /**
     * @param restaurantName
     * @return List of all restaurants in the database
     * @throws RestaurantNotFoundException - when restaurant name field is empty
     */
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/name/{restaurant_name}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> restaurantsByName(@PathVariable(
            "restaurant_name") final String restaurantName) throws RestaurantNotFoundException {

        // Getting the list of all restaurants with help of restaurant business service based on input restaurant name
        final List<RestaurantEntity> allRestaurants =
                restaurantService.restaurantsByName(restaurantName.trim());

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

        if (allRestaurants.isEmpty()) {
            return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
        }

        for (RestaurantEntity restaurantEntity : allRestaurants) {
            RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = new RestaurantDetailsResponseAddressState()
                    .id(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()))
                    .stateName(restaurantEntity.getAddress().getState().getStateName());

            RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = new RestaurantDetailsResponseAddress()
                    .id(UUID.fromString(restaurantEntity.getAddress().getUuid()))
                    .flatBuildingName(restaurantEntity.getAddress().getFlatBuilNo())
                    .locality(restaurantEntity.getAddress().getLocality())
                    .city(restaurantEntity.getAddress().getCity())
                    .pincode(restaurantEntity.getAddress().getPinCode())
                    .state(restaurantDetailsResponseAddressState);

            String categoriesString = categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid())
                    .stream()
                    .map(o -> String.valueOf(o.getCategoryName()))
                    .collect(Collectors.joining(", "));
            Double temp2 = BigDecimal.valueOf(restaurantEntity.getCustomerRating()).setScale(1, RoundingMode.HALF_UP).doubleValue();
            RestaurantList restaurantList = new RestaurantList()
                    .id(UUID.fromString(restaurantEntity.getUuid()))
                    .restaurantName(restaurantEntity.getRestaurantName())
                    .photoURL(restaurantEntity.getPhotoUrl())
                    .customerRating(new BigDecimal(temp2))
                    .averagePrice(restaurantEntity.getAvgPrice())
                    .numberCustomersRated(restaurantEntity.getNumberCustomersRated())
                    .address(restaurantDetailsResponseAddress)
                    .categories(categoriesString);
            restaurantListResponse.addRestaurantsItem(restaurantList);
        }

        // return response entity with RestaurantLists(restaurantLists) and Http status
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }


    /**
     * @param categoryID
     * @return List of all restaurants having given category id
     * @throws CategoryNotFoundException   - When Given category id  field is empty
     * @throws RestaurantNotFoundException - When given restaurant id field is empty
     */
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/category/{category_id}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> restaurantByCategory(@PathVariable(
            "category_id") final String categoryID) throws CategoryNotFoundException, RestaurantNotFoundException {

        List<RestaurantEntity> restaurantEntityList =
                restaurantService.restaurantByCategory(categoryID.trim());

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();

        for (RestaurantEntity restaurantEntity : restaurantEntityList) {
            RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = new RestaurantDetailsResponseAddressState()
                    .id(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()))
                    .stateName(restaurantEntity.getAddress().getState().getStateName());

            RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = new RestaurantDetailsResponseAddress()
                    .id(UUID.fromString(restaurantEntity.getAddress().getUuid()))
                    .flatBuildingName(restaurantEntity.getAddress().getFlatBuilNo())
                    .locality(restaurantEntity.getAddress().getLocality())
                    .city(restaurantEntity.getAddress().getCity())
                    .pincode(restaurantEntity.getAddress().getPinCode())
                    .state(restaurantDetailsResponseAddressState);

            String categoriesString = categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid())
                    .stream()
                    .map(o -> String.valueOf(o.getCategoryName()))
                    .collect(Collectors.joining(", "));
            Double temp1 = BigDecimal.valueOf(restaurantEntity.getCustomerRating()).setScale(1, RoundingMode.HALF_UP).doubleValue();
            RestaurantList restaurantList = new RestaurantList()
                    .id(UUID.fromString(restaurantEntity.getUuid()))
                    .restaurantName(restaurantEntity.getRestaurantName())
                    .photoURL(restaurantEntity.getPhotoUrl())
                    .customerRating(new BigDecimal(temp1))
                    .averagePrice(restaurantEntity.getAvgPrice())
                    .numberCustomersRated(restaurantEntity.getNumberCustomersRated())
                    .address(restaurantDetailsResponseAddress)
                    .categories(categoriesString);
            restaurantListResponse.addRestaurantsItem(restaurantList);
        }

        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }


    /**
     * @param restaurantID
     * @return Restaurant with details based on given restaurant id
     * @throws RestaurantNotFoundException - When given restaurant id field is empty
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/restaurant/{restaurant_id}", produces =
            MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantDetailsResponse> getRestaurantByRestaurantID(@PathVariable(
            "restaurant_id") final String restaurantID) throws RestaurantNotFoundException {

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantID.trim());

        RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = new RestaurantDetailsResponseAddressState()
                .id(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()))
                .stateName(restaurantEntity.getAddress().getState().getStateName());

        RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = new RestaurantDetailsResponseAddress()
                .id(UUID.fromString(restaurantEntity.getAddress().getUuid()))
                .flatBuildingName(restaurantEntity.getAddress().getFlatBuilNo())
                .locality(restaurantEntity.getAddress().getLocality())
                .city(restaurantEntity.getAddress().getCity())
                .pincode(restaurantEntity.getAddress().getPinCode())
                .state(restaurantDetailsResponseAddressState);
        Double temp = BigDecimal.valueOf(restaurantEntity.getCustomerRating()).setScale(1, RoundingMode.HALF_UP).doubleValue();
        RestaurantDetailsResponse restaurantDetailsResponse = new RestaurantDetailsResponse()
                .id(UUID.fromString(restaurantEntity.getUuid()))
                .restaurantName(restaurantEntity.getRestaurantName())
                .photoURL(restaurantEntity.getPhotoUrl())
                .customerRating(new BigDecimal(temp))
                .averagePrice(restaurantEntity.getAvgPrice())
                .numberCustomersRated(restaurantEntity.getNumberCustomersRated())
                .address(restaurantDetailsResponseAddress);

        for (CategoryEntity categoryEntity : categoryService.getCategoriesByRestaurant(restaurantID.trim())) {
            CategoryList categoryList = new CategoryList()
                    .id(UUID.fromString(categoryEntity.getUuid()))
                    .categoryName(categoryEntity.getCategoryName());

            for (ItemEntity itemEntity : itemService.getItemsByCategoryAndRestaurant(restaurantID.trim(), categoryEntity.getUuid())) {
                ItemList itemList = new ItemList()
                        .id(UUID.fromString(itemEntity.getUuid()))
                        .itemName(itemEntity.getItemName())
                        .price(itemEntity.getPrice())
                        .itemType(ItemList.ItemTypeEnum.fromValue(itemEntity.getType().getValue()));

                categoryList.addItemListItem(itemList);
            }

            restaurantDetailsResponse.addCategoriesItem(categoryList);
        }

        return new ResponseEntity<RestaurantDetailsResponse>(restaurantDetailsResponse, HttpStatus.OK);

    }


    /**
     * @param authorization
     * @param restaurantId
     * @param customerRating
     * @return Restaurant uuid of the rating updated restaurant
     * @throws AuthorizationFailedException - When customer is not logged in or logged out or login expired
     * @throws RestaurantNotFoundException  - When given restaurant id field is empty
     * @throws InvalidRatingException       - When the Rating value provided is invalid
     */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT, path = "/api/restaurant/{restaurant_id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantUpdatedResponse> updateRestaurantDetails(
            @RequestParam(name = "customer_rating") final Double customerRating,
            @PathVariable("restaurant_id") final String restaurantId,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, RestaurantNotFoundException, InvalidRatingException {

        String[] bearerToken = authorization.split("Bearer ");
        CustomerEntity customerEntity = null;
        if (bearerToken.length == 1) {
            throw new AuthorizationFailedException("ATHR-005", "Use valid authorization format <Bearer accessToken>");
        } else {
            customerEntity = utilityService.getCustomer(bearerToken[1]);
        }

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantId.trim());
        restaurantService.updateRestaurantRating(restaurantEntity, customerRating);

        RestaurantUpdatedResponse restaurantUpdatedResponse = new RestaurantUpdatedResponse()
                .id(UUID.fromString(restaurantId.trim()))
                .status("RESTAURANT RATING UPDATED SUCCESSFULLY");
        return new ResponseEntity<RestaurantUpdatedResponse>(restaurantUpdatedResponse, HttpStatus.OK);
    }

}
