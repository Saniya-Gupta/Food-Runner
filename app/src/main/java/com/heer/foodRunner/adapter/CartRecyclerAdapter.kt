package com.heer.foodRunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heer.foodRunner.R
import com.heer.foodRunner.database.OrderEntity

class CartRecyclerAdapter(
    val context: Context,
    private val orderList: List<OrderEntity>
) :
    RecyclerView.Adapter<CartRecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemCost: TextView = view.findViewById(R.id.txtItemCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_cart, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val orderItem = orderList[position]
        holder.txtItemName.text = orderItem.orderItemName
        val money = "Rs. ${orderItem.orderItemCost}"
        holder.txtItemCost.text = money
    }
}
