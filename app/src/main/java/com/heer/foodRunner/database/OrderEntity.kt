package com.heer.foodRunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Orders")
data class OrderEntity(
    @ColumnInfo(name = "id") @PrimaryKey val orderItemId: String,
    @ColumnInfo(name = "name") val orderItemName: String,
    @ColumnInfo(name = "cost_for_one") val orderItemCost: String
)