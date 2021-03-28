package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private CategoryService categoryService;


    //List all restaurants sorted by rating - Descending order
    public List<RestaurantEntity> restaurantsByRating() {
        return this.restaurantDao.restaurantsByRating();
    }

    //List restaurant details by restaurant name
    public List<RestaurantEntity> restaurantsByName(String restaurantName) throws RestaurantNotFoundException {

        // Throw exception if path variable(restaurant_name) is empty
        if (restaurantName == null || restaurantName.isEmpty() || restaurantName.equalsIgnoreCase("\"\"")) {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }
        return restaurantDao.restaurantsByName(restaurantName);
    }

    //List restaurants belonging to certain category
    public List<RestaurantEntity> restaurantByCategory(final String categoryId) throws CategoryNotFoundException {

        if (categoryId.equals("")) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        CategoryEntity categoryEntity = categoryService.getCategoryById(categoryId);

        if (categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }

        List<RestaurantEntity> restaurantEntityList = categoryEntity.getRestaurants();
        restaurantEntityList.sort(Comparator.comparing(RestaurantEntity::getRestaurantName));
        return restaurantEntityList;
    }

    //Display restaurant details by restaurant UUID
    public RestaurantEntity restaurantByUUID(String restaurantUUID) throws RestaurantNotFoundException {

        if (restaurantUUID.equals("")) {
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }

        RestaurantEntity restaurantEntity = restaurantDao.restaurantByUUID(restaurantUUID);

        if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }
        return restaurantEntity;
    }

    //Updating restaurant rating
    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurantEntity, Double newRating) throws InvalidRatingException {

        //Allowing ratings value only if it is or between 1.0 and 5.0
        if (newRating < 1.0 || newRating > 5.0) {
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }

        //Re-calculating average rating including the new rating
        //Also updating number of customers ratings
        Double newAverageRating = (
                ((restaurantEntity.getNumberCustomersRated() * restaurantEntity.getCustomerRating()) + newRating) /
                        (restaurantEntity.getNumberCustomersRated() + 1));
        restaurantEntity.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated() + 1);
        newAverageRating = BigDecimal.valueOf(newAverageRating).setScale(2, RoundingMode.HALF_UP).doubleValue();
        restaurantEntity.setCustomerRating(newAverageRating);
        return restaurantDao.updateRestaurantEntity(restaurantEntity);
    }

}
