package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RestaurantDao {
    @PersistenceContext
    private EntityManager entityManager;

    //Return restaurant list sorted based on customer rating
    public List<RestaurantEntity> restaurantsByRating() {
        try {
            return entityManager.createNamedQuery("allRestaurants", RestaurantEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Return restaurant list based out of name
    public List<RestaurantEntity> restaurantsByName(String restaurantName) {
        try {
            return entityManager.createNamedQuery("findByName", RestaurantEntity.class).setParameter("restaurantName", "%" + restaurantName.toLowerCase() + "%").getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Return restaurant details by restaurant UUID
    public RestaurantEntity restaurantByUUID(String restaurantUUID) {
        try {
            return entityManager.createNamedQuery("findRestaurantByUUId", RestaurantEntity.class).setParameter("restaurantUUID", restaurantUUID.toLowerCase()).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Update modifying restaurant details in DB
    public RestaurantEntity updateRestaurantEntity(RestaurantEntity restaurantEntity) {
        return entityManager.merge(restaurantEntity);
    }

}
