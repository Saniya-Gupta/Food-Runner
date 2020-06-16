package com.heer.foodRunner.model

data class Order(
    val orderId: String,
    val restaurant_name: String,
    val totalCost: String,
    val dateAndTime: String,
    val foodItems: List<OrderFoodItems>
)