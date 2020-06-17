package com.heer.foodRunner.adapter

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.heer.foodRunner.R
import com.heer.foodRunner.database.OrderEntity
import com.heer.foodRunner.database.RestaurantDatabase
import com.heer.foodRunner.model.MenuItems

class MenuRecyclerAdapter(
    val context: Context,
    private val itemList: List<MenuItems>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<MenuRecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItemIndex: TextView = view.findViewById(R.id.txtItemIndex)
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemCost: TextView = view.findViewById(R.id.txtItemCost)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_menu, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnItemClickListener {
        fun addItemClick(menuItems: MenuItems)
        fun removeItemClick(menuItems: MenuItems)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val foodItem = itemList[position]
        holder.txtItemIndex.text = (position + 1).toString()
        holder.txtItemName.text = foodItem.foodItemsName
        val money = "Rs. ${foodItem.foodItemsCostPerPerson}"
        holder.txtItemCost.text = money

        holder.btnAddToCart.setOnClickListener {
            val orderEntity = OrderEntity(
                foodItem.foodItemsId,
                foodItem.foodItemsName,
                foodItem.foodItemsCostPerPerson
            )
            if (OrderDb(context.applicationContext, orderEntity, 3).execute().get()) {
                if (OrderDb(context.applicationContext, orderEntity, 1).execute().get()) { // Insert
                    holder.btnAddToCart.setBackgroundResource(R.color.colorGreen)
                    holder.btnAddToCart.text = context.getString(R.string.remove_from_cart)
                    listener.addItemClick(foodItem)
                }
            } else {
                if (OrderDb(context.applicationContext, orderEntity, 2).execute().get()) { // Remove
                    holder.btnAddToCart.setBackgroundResource(R.color.colorPrimary)
                    holder.btnAddToCart.text = context.getString(R.string.add_to_cart)
                    listener.removeItemClick(foodItem)
                }
            }
        }
    }

    class OrderDb(
        val context: Context,
        private val orderEntity: OrderEntity,
        private val mode: Int = 0
    ) :
        AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "item-db")
                .fallbackToDestructiveMigration().build()
            when (mode) {
                1 -> {
                    db.orderDao().insertItemOrder(orderEntity)
                    db.close()
                    return true
                }
                2 -> {
                    db.orderDao().cancelItemOrder(orderEntity)
                    db.close()
                    return true
                }
                3 -> {
                    val a = db.orderDao().getOrderItemById(orderEntity.orderItemId)
                    db.close()
                    return a == 0
                }
            }
            return false
        }
    }

}
