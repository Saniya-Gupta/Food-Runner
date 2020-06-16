package com.heer.foodRunner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrderDao {

    @Insert
    fun insertItemOrder(orderEntity: OrderEntity)

    @Delete
    fun cancelItemOrder(orderEntity: OrderEntity)

    @Query("Select id from Orders where id= :itemId")
    fun getOrderItemById(itemId: String): Int

    @Query("Select * from Orders")
    fun fetchOrder(): List<OrderEntity>

    @Query("Delete from Orders")
    fun cancelOrder()
}