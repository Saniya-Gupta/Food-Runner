package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.heer.foodRunner.R
import com.heer.foodRunner.adapter.CartRecyclerAdapter
import com.heer.foodRunner.database.OrderEntity
import com.heer.foodRunner.database.RestaurantDatabase
import com.heer.foodRunner.model.CartJsonArray
import com.heer.foodRunner.model.CartJsonObject
import com.heer.foodRunner.util.ConnectionManager
import com.heer.foodRunner.util.PLACE_ORDER
import org.json.JSONException
import org.json.JSONObject

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: CartRecyclerAdapter
    private lateinit var btnPlaceOrder: Button
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var progressLayout: RelativeLayout
    private lateinit var txtRestaurantName: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        init(view)
        setUpToolbar()
        defaultLayout()
        val orderEntity = GetOrders(activity as AppCompatActivity).execute().get()
        val totalCost = sendDataToAdapter(orderEntity)
        val btnText = "Place Order: Rs. $totalCost"
        btnPlaceOrder.text = btnText // Set Total Cost
        btnPlaceOrder.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            sendOrderRequestToServer(getJsonObj(orderEntity, totalCost))
        }
        return view
    }

    private fun getJsonObj(orderEntity: List<OrderEntity>, totalCost: Int): JSONObject {
        val jsonArray = arrayListOf<CartJsonArray>()
        for (i in orderEntity) {
            jsonArray.add(CartJsonArray(i.orderItemId))
        } // Creating complex Json Object
        val jsonObj = CartJsonObject(
            sharedPreferences.getString("userId", "No ID"),
            sharedPreferences.getString("restaurantId", "0"),
            totalCost.toString(),
            Gson().toJsonTree(jsonArray)
        )
        return JSONObject(Gson().toJson(jsonObj))
    }

    private fun sendOrderRequestToServer(jsonObj: JSONObject) {
        val queue = Volley.newRequestQueue(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(
                    Method.POST, PLACE_ORDER, jsonObj,
                    Response.Listener {
                        try {
                            progressLayout.visibility = View.GONE
                            val resObj = it.getJSONObject("data")
                            val resSuccess = resObj.getBoolean("success")
                            if (resSuccess) {
                                val activity = activity as AppCompatActivity
                                activity.supportFragmentManager.beginTransaction()
                                    .replace(R.id.frameLayout, OrderSuccessFragment()).commit()
                            } else {
                                Toast.makeText(
                                    activity as Context,
                                    resObj.getString("Some error occurred!!!"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error occurred!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            activity as Context,
                            "Slow internet connection!!!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "526f51e288db30"
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

    private fun sendDataToAdapter(orderEntity: List<OrderEntity>): Int {
        var totalCost = 0
        for (i in orderEntity) {
            totalCost += Integer.parseInt(i.orderItemCost)
        }
        recyclerAdapter = CartRecyclerAdapter(activity as Context, orderEntity)
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = layoutManager
        return totalCost
    }

    private fun defaultLayout() {
        progressLayout.visibility = View.GONE
        val txtText = "Ordering From: ${sharedPreferences.getString("restaurantName", "No name")}"
        txtRestaurantName.text = txtText // Set Restaurant Name
    }

    private fun setUpToolbar() {
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "My Cart"
    }

    private fun init(view: View) {
        recyclerView = view.findViewById(R.id.recycler)
        layoutManager = LinearLayoutManager(activity)
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder)
        txtRestaurantName = view.findViewById(R.id.txtRestaurantName)
        sharedPreferences = context!!.getSharedPreferences(
            context?.getString(R.string.shared_pref),
            Context.MODE_PRIVATE
        )
        progressLayout = view.findViewById(R.id.progressLayout)
    }

    class GetOrders(val context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "item-db")
                .fallbackToDestructiveMigration().build()
            val orderList = db.orderDao().fetchOrder()
            db.close()
            return orderList
        }
    }
}