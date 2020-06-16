package com.heer.foodRunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heer.foodRunner.R
import com.heer.foodRunner.model.Order

class OrderRestaurantRecyclerAdapter(
    val context: Context, private val orderList: ArrayList<Order>
) :
    RecyclerView.Adapter<OrderRestaurantRecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtOrderDate: TextView = view.findViewById(R.id.txtOrderDate)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerMenuItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_order_restaurant, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val order = orderList[position]
        holder.txtRestaurantName.text = order.restaurant_name
        holder.txtOrderDate.text = order.dateAndTime.subSequence(0, 8)
        holder.recyclerView.adapter = OrderMenuRecyclerAdapter(context, order.foodItems)
        holder.recyclerView.layoutManager = LinearLayoutManager(context)
    }
}