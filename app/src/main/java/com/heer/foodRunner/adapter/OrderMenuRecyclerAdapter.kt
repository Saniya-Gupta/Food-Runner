package com.heer.foodRunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heer.foodRunner.R
import com.heer.foodRunner.model.OrderFoodItems

class OrderMenuRecyclerAdapter(
    val context: Context,
    private val orderMenuList: List<OrderFoodItems>
) :
    RecyclerView.Adapter<OrderMenuRecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemCost: TextView = view.findViewById(R.id.txtItemCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_order_menu, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderMenuList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val orderMenuItem = orderMenuList[position]
        holder.txtItemName.text = orderMenuItem.name
        val cost = "Rs. ${orderMenuItem.cost}"
        holder.txtItemCost.text = cost
    }

}

