package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.heer.foodRunner.R
import com.heer.foodRunner.adapter.OrderRestaurantRecyclerAdapter
import com.heer.foodRunner.model.Order
import com.heer.foodRunner.model.OrderFoodItems
import com.heer.foodRunner.util.ConnectionManager
import com.heer.foodRunner.util.FETCH_PREVIOUS_ORDERS
import org.json.JSONException

class OrderHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: OrderRestaurantRecyclerAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressLayout: RelativeLayout
    private lateinit var emptyHistoryLayout: ConstraintLayout
    private var orderList = arrayListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        init(view)
        progressLayout.visibility = View.VISIBLE
        sendRequestToServer(sharedPreferences.getString("userId", "No Id"))
        return view
    }

    private fun sendRequestToServer(userId: String?) {
        val queue = Volley.newRequestQueue(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                FETCH_PREVIOUS_ORDERS + "$userId",
                null,
                Response.Listener {
                    try {
                        progressLayout.visibility = View.INVISIBLE
                        val dataObj = it.getJSONObject("data")
                        if (dataObj.getBoolean("success")) {
                            val dataArray = dataObj.getJSONArray("data")
                            if (dataArray.length() == 0)
                                emptyHistoryLayout.visibility = View.VISIBLE
                            for (i in 0 until dataArray.length()) {
                                val restaurantObj = dataArray.getJSONObject(i)
                                val restaurantMenuArray = restaurantObj.getJSONArray("food_items")
                                val listOrderFoodItems = arrayListOf<OrderFoodItems>()
                                for (j in 0 until restaurantMenuArray.length()) {
                                    val foodObj = restaurantMenuArray.getJSONObject(j)
                                    val orderFoodItems = OrderFoodItems(
                                        foodObj.getString("food_item_id"),
                                        foodObj.getString("name"),
                                        foodObj.getString("cost")
                                    )
                                    listOrderFoodItems.add(orderFoodItems)
                                }
                                val order = Order(
                                    restaurantObj.getString("order_id"),
                                    restaurantObj.getString("restaurant_name"),
                                    restaurantObj.getString("total_cost"),
                                    restaurantObj.getString("order_placed_at"),
                                    listOrderFoodItems
                                )
                                orderList.add(order)
                            }
                            recyclerAdapter =
                                OrderRestaurantRecyclerAdapter(activity as Context, orderList)
                            recyclerView.adapter = recyclerAdapter
                            recyclerView.layoutManager = layoutManager
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

    private fun init(view: View) {
        recyclerView = view.findViewById(R.id.recyclerOrders)
        layoutManager = LinearLayoutManager(activity)
        sharedPreferences = context!!.getSharedPreferences(
            getString(R.string.shared_pref),
            Context.MODE_PRIVATE
        )
        progressLayout = view.findViewById(R.id.progressLayout)
        emptyHistoryLayout = view.findViewById(R.id.emptyHistoryLayout)
    }
}
