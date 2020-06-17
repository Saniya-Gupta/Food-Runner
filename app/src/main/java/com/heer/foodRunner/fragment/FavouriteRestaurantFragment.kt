package com.heer.foodRunner.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.heer.foodRunner.R
import com.heer.foodRunner.adapter.RestaurantRecyclerAdapter
import com.heer.foodRunner.model.Restaurant
import com.heer.foodRunner.database.RestaurantDatabase
import com.heer.foodRunner.database.RestaurantEntity

class FavouriteRestaurantFragment : Fragment() {

    private lateinit var recyclerFav: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: RestaurantRecyclerAdapter
    private lateinit var progressLayout: RelativeLayout
    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite_restaurant, container, false)
        init(view)
        setContent()
        return view
    }

    private fun setContent() {
        val favRestaurantList = DbAsyncTask(activity as Context).execute().get()
        val restaurantList = arrayListOf<Restaurant>()
        if (favRestaurantList.isNotEmpty()) {
            progressLayout.visibility = View.INVISIBLE
            for (i in favRestaurantList) {
                restaurantList.add(
                    Restaurant(
                        i.restaurant_id.toString(),
                        i.restaurantName,
                        i.restaurantRating,
                        i.restaurantCostPerPerson,
                        i.restaurantImgUrl
                    )
                )
            }
        } else {
            progressLayout.visibility = View.INVISIBLE
            constraintLayout.visibility = View.VISIBLE
        }
        recyclerAdapter = RestaurantRecyclerAdapter(activity as Context, restaurantList)
        recyclerFav.adapter = recyclerAdapter
        recyclerFav.layoutManager = layoutManager
    }

    private fun init(view: View) {
        recyclerFav = view.findViewById(R.id.recycler)
        layoutManager = LinearLayoutManager(activity)
        progressLayout = view.findViewById(R.id.progressLayout)
        constraintLayout = view.findViewById(R.id.emptyFavLayout)
        progressLayout.visibility = View.VISIBLE
        constraintLayout.visibility = View.GONE
    }

    class DbAsyncTask(val context: Context) : AsyncTask<Void, Void, List<RestaurantEntity>>() {
        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db")
                .fallbackToDestructiveMigration().build()
            return db.restaurantDao().getAllRestaurants()
        }
    }

    override fun onResume() {
        setContent()
        super.onResume()
    }
}