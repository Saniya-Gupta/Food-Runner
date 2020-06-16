package com.heer.foodRunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Restaurants")
data class RestaurantEntity(
    @ColumnInfo(name = "id") @PrimaryKey val restaurant_id: Int,
    @ColumnInfo(name = "name") val restaurantName: String,
    @ColumnInfo(name = "rating") val restaurantRating: String,
    @ColumnInfo(name = "cost_for_one") val restaurantCostPerPerson: String,
    @ColumnInfo(name = "img") val restaurantImgUrl: String
)