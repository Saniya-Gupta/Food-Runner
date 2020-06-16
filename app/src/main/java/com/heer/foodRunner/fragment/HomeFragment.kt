package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.heer.foodRunner.R
import com.heer.foodRunner.adapter.RestaurantRecyclerAdapter
import com.heer.foodRunner.model.Restaurant
import com.heer.foodRunner.util.ConnectionManager
import com.heer.foodRunner.util.FETCH_RESTAURANTS
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    private lateinit var recyclerHome: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: RestaurantRecyclerAdapter
    private lateinit var progressLayout: RelativeLayout
    private var restaurantInfoList: ArrayList<Restaurant> = arrayListOf()

    private var ratingComparator = Comparator<Restaurant> { restaurant1, restaurant2 ->
        if (restaurant1.restaurantRating.compareTo(restaurant2.restaurantRating, true) == 0) {
            // Sort the Restaurant names alphabetically
            restaurant1.restaurantName.compareTo(restaurant2.restaurantName, true)
        } else
            restaurant1.restaurantRating.compareTo(restaurant2.restaurantRating, true)
        // Else sort them according to ratings
    }

    private var costComparator = Comparator<Restaurant> { obj1, obj2 ->
        if (obj1.restaurantCostPerPerson.compareTo(
                obj2.restaurantCostPerPerson,
                ignoreCase = true
            ) == 0
        )
            obj1.restaurantName.compareTo(obj2.restaurantName, true)
        else
            obj1.restaurantCostPerPerson.compareTo(obj2.restaurantCostPerPerson, ignoreCase = true)
    }

    private var nameComparator = Comparator<Restaurant> { obj1, obj2 ->
        obj1.restaurantName.compareTo(obj2.restaurantName, ignoreCase = true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        init(view)
        setUpToolbar()
        progressLayout.visibility = View.VISIBLE
        sendRequestToServer()
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId != R.id.search) {
            item.isCheckable = true
            item.isChecked = true
        }

        when (item.itemId) {
            R.id.action_sort_ratings -> {
                Collections.sort(restaurantInfoList, ratingComparator)
                restaurantInfoList.reverse() // Descending Order
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.action_sort_cost -> {
                Collections.sort(restaurantInfoList, costComparator)
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.action_sort_cost_name -> {
                Collections.sort(restaurantInfoList, nameComparator)
                recyclerAdapter.notifyDataSetChanged()
            }
            R.id.search -> {
                /*// Search feature using filter in Adapter
                val searchView: SearchView = item.actionView as SearchView
                item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                recyclerAdapter.filter.filter(newText)
                                recyclerAdapter.notifyDataSetChanged()
                                return true
                            }
                        })
                        return true
                    }
                    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                        recyclerAdapter.filter.filter("")
                        recyclerAdapter.notifyDataSetChanged()
                        return true
                    }
                })*/
                Toast.makeText(activity as Context, "Clicked on Search", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_restaurant, menu)
    }

    private fun sendRequestToServer() {
        val queue = Volley.newRequestQueue(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                FETCH_RESTAURANTS,
                null,
                Response.Listener {
                    try {
                        val dataObj = it.getJSONObject("data")
                        val success = dataObj.getBoolean("success")
                        if (success) {
                            progressLayout.visibility = View.GONE
                            val dataArray = dataObj.getJSONArray("data")
                            for (i in 0 until dataArray.length()) {
                                val jsonObj = dataArray.getJSONObject(i)
                                val restaurantObj = Restaurant(
                                    jsonObj.getString("id"),
                                    jsonObj.getString("name"),
                                    jsonObj.getString("rating"),
                                    jsonObj.getString("cost_for_one"),
                                    jsonObj.getString("image_url")
                                )
                                restaurantInfoList.add(restaurantObj)
                            }
                            recyclerAdapter =
                                RestaurantRecyclerAdapter(activity as Context, restaurantInfoList)
                            recyclerHome.adapter = recyclerAdapter
                            recyclerHome.layoutManager = layoutManager
                        } else
                            Toast.makeText(
                                activity as Context,
                                "Some error occurred!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error occurred!!!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(
                        activity as Context,
                        "Slow internet connection!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["token"] = "526f51e288db30 "
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("No Internet Connection Found")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Open settings") { _, _ ->
                // Implicit Intent
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                activity?.finish()
            }
            dialog.setNegativeButton("Exit App") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
    }

    private fun setUpToolbar() {
        // To inform the compiler that this toolbar has menu items: sort. Only required in fragment.
        setHasOptionsMenu(true)
    }

    private fun init(view: View) {
        recyclerHome = view.findViewById(R.id.recycler)
        layoutManager = LinearLayoutManager(activity)
        progressLayout = view.findViewById(R.id.progressLayout)

    }
}

