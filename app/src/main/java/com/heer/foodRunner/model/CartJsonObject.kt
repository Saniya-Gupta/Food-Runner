package com.heer.foodRunner.model

import com.google.gson.JsonElement

data class CartJsonObject(
    val user_id: String?,
    val restaurant_id: String?,
    val total_cost: String,
    val food: JsonElement
)
