package com.heer.foodRunner.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.heer.foodRunner.R
import com.heer.foodRunner.activity.OrderActivity
import com.heer.foodRunner.model.Restaurant
import com.heer.foodRunner.database.RestaurantDatabase
import com.heer.foodRunner.database.RestaurantEntity
import com.squareup.picasso.Picasso

class RestaurantRecyclerAdapter(val context: Context, private var itemList: List<Restaurant>) :
    RecyclerView.Adapter<RestaurantRecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtRestaurantCostPerPerson: TextView =
            view.findViewById(R.id.txtRestaurantCostPerPerson)
        val txtRestaurantRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val imgRestaurant: ImageView = view.findViewById(R.id.imgRestaurant)
        val cvRestaurant: CardView = view.findViewById(R.id.cvRestaurant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_restaurant, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val restaurant = itemList[position]
        holder.txtRestaurantName.text = restaurant.restaurantName
        val money = "Rs. " + restaurant.restaurantCostPerPerson + " / person"
        holder.txtRestaurantCostPerPerson.text = money
        holder.txtRestaurantRating.text = restaurant.restaurantRating
        Picasso.get().load(restaurant.restaurantImgUrl).error(R.drawable.app_logo_black)
            .into(holder.imgRestaurant)

        val listOfFavRestaurantIds = GetAllRestaurants(context.applicationContext).execute().get()

        if (listOfFavRestaurantIds.isNotEmpty() && listOfFavRestaurantIds.contains(restaurant.restaurantId)) {
            holder.txtRestaurantRating.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(context, R.drawable.ic_action_red_fav_checked),
                null,
                null
            )
        } else {
            holder.txtRestaurantRating.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(context, R.drawable.ic_action_red_fav), null, null
            )
        }

        holder.txtRestaurantRating.setOnClickListener {
            val restaurantEntity = RestaurantEntity(
                restaurant.restaurantId.toInt(),
                restaurant.restaurantName,
                restaurant.restaurantRating,
                restaurant.restaurantCostPerPerson,
                restaurant.restaurantImgUrl
            )
            if (!RestaurantDb(context.applicationContext, restaurantEntity, 3).execute().get()) {
                val addToFav =
                    RestaurantDb(context.applicationContext, restaurantEntity, 1).execute().get()
                if (addToFav) {
                    holder.txtRestaurantRating.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        ContextCompat.getDrawable(
                            context.applicationContext,
                            R.drawable.ic_action_red_fav_checked
                        ),
                        null,
                        null
                    )
                }
            } else {
                val removeFromFav =
                    RestaurantDb(context.applicationContext, restaurantEntity, 2).execute().get()
                if (removeFromFav) {
                    holder.txtRestaurantRating.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        ContextCompat.getDrawable(
                            context.applicationContext,
                            R.drawable.ic_action_red_fav
                        ),
                        null,
                        null
                    )
                }
            }
        }

        holder.cvRestaurant.setOnClickListener {
            val sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.shared_pref),
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putString("restaurantId", restaurant.restaurantId).apply()
            sharedPreferences.edit().putString("restaurantName", restaurant.restaurantName).apply()
            val activity: AppCompatActivity = context as AppCompatActivity
            //EmptyPreviousOrder(context.applicationContext).execute().get()
            activity.startActivity(Intent(activity as Context, OrderActivity::class.java))
        }
    }

    class RestaurantDb(
        val context: Context,
        private val restaurantEntity: RestaurantEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db")
                .fallbackToDestructiveMigration().build()
            when (mode) {
                1 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                2 -> {
                    db.restaurantDao().removeRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    val favBookId: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurant_id.toString())
                    db.close()
                    return favBookId != null
                }
            }
            return false
        }
    }

    class GetAllRestaurants(val context: Context) : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg params: Void?): List<String> {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db")
                .fallbackToDestructiveMigration().build()
            val list = db.restaurantDao().getAllRestaurants()
            val listIds = arrayListOf<String>()
            for (i in list)
                listIds.add(i.restaurant_id.toString())
            return listIds
        }
    }
}
