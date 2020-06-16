package com.heer.foodRunner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RestaurantDao {

    @Insert
    fun insertRestaurant(restaurantEntity: RestaurantEntity)

    @Delete
    fun removeRestaurant(restaurantEntity: RestaurantEntity)

    @Query("Select * from Restaurants")
    fun getAllRestaurants(): List<RestaurantEntity>

    @Query("Select * from Restaurants where id = :bookId")
    fun getRestaurantById(bookId: String): RestaurantEntity

}