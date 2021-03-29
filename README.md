**FoodOrderingApp**

In this project, we have developed from scratch REST API endpoints of various functionalities required for the web app FoodOrderingApp.  
In order to observe the functionality of the endpoints, we have used the Swagger user interface and store the data in the PostgreSQL database. Also, the project has been implemented using Java Persistence API (JPA).

Technologies used: Spring Boot

List of API endpoints developed are as below:

# FoodOrderingApp-Backend

## address-controller : Address Controller
POST /address saveAddress

GET /address/customer getAllAddresses

DELETE /address/{address_id} deleteAddress

GET /states getAllStates

## category-controller : Category Controller
GET /category getAllCategories

GET /category/{category_id} getCategoryById

## customer-controller : Customer Controller
PUT /customer updateCustomerDetails

POST /customer/login login

POST /customer/logout signOut

PUT /customer/password updateCustomerPassword

POST /customer/signup signup

## item-controller : Item Controller
GET /item/restaurant/{restaurant_id} getTopFiveItemsByPopularity

## order-controller : Order Controller
GET /order getPastOrderOfUser

POST /order saveOrder

GET /order/coupon/{coupon_name} getCouponByCouponName

## payment-controller : Payment Controller
GET /payment getAllPaymentMethods

## restaurant-controller : Restaurant Controller
GET /api/restaurant/{restaurant_id} getRestaurantByRestaurantID

PUT /api/restaurant/{restaurant_id} updateRestaurantDetails

GET /restaurant getAllRestaurants

GET /restaurant/category/{category_id} restaurantByCategory

GET /restaurant/name/{restaurant_name} restaurantsByName
