package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.heer.foodRunner.R
import com.heer.foodRunner.adapter.MenuRecyclerAdapter
import com.heer.foodRunner.database.OrderEntity
import com.heer.foodRunner.database.RestaurantDatabase
import com.heer.foodRunner.model.MenuItems
import com.heer.foodRunner.util.ConnectionManager
import com.heer.foodRunner.util.FETCH_RESTAURANT_MENU
import org.json.JSONException
import org.json.JSONObject

class MenuFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: MenuRecyclerAdapter
    private val orderList = arrayListOf<OrderEntity>()
    private lateinit var btnProceedToCart: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        init(view)
        setUpToolbar()
        progressLayout.visibility = View.VISIBLE
        sendRequestToServer(sharedPreferences.getString("restaurantId", "0")) // Get Menu
        if (EmptyPreviousOrder(activity?.applicationContext!!).execute().get())
            btnProceedToCart.visibility = View.GONE
        btnProceedToCart.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            proceedToCartPage() //  Place Order
        }
        return view
    }

    private fun proceedToCartPage() {
        val activity: AppCompatActivity = context as AppCompatActivity
        activity.supportFragmentManager.beginTransaction().replace(R.id.frameLayout, CartFragment())
            .addToBackStack("MenuFragment").commit()
        progressLayout.visibility = View.GONE
    }

    private fun sendRequestToServer(restaurantId: String?) {
        val queue = Volley.newRequestQueue(context)
        if (context?.let { ConnectionManager().checkConnectivity(it) } == true) {
            val stringRequest = object : StringRequest( // Using String Request
                Method.GET,
                FETCH_RESTAURANT_MENU + "$restaurantId",
                Response.Listener {
                    try {
                        progressLayout.visibility = View.INVISIBLE
                        val resObj = JSONObject(it)
                        val dataObj = resObj.getJSONObject("data")
                        val success = dataObj.getBoolean("success")
                        if (success) {
                            val arrayFoodItems = dataObj.getJSONArray("data")
                            val menuList = arrayListOf<MenuItems>()
                            for (i in 0 until arrayFoodItems.length()) {
                                val menuObj = arrayFoodItems.getJSONObject(i)
                                val menuItem = MenuItems(
                                    menuObj.getString("id"),
                                    menuObj.getString("name"),
                                    menuObj.getString("cost_for_one"),
                                    menuObj.getString("restaurant_id")
                                )
                                menuList.add(menuItem)
                            }

                            /*
                            This code is used to maintain same instance when user presses back button.
                            Also Empty Orders in RestaurantRecyclerAdapter on holder click. Line commented for now.

                            orderList.addAll(CartFragment.GetOrders(activity!!.applicationContext)
                                    .execute().get())
                            if(orderList.isNotEmpty()) {
                                println("Here made visible 1")
                                btnProceedToCart.visibility = View.VISIBLE
                            }
                            else
                                btnProceedToCart.visibility = View.GONE
                            */

                            recyclerAdapter =
                                MenuRecyclerAdapter(
                                    activity as Context,
                                    menuList,
                                    object : MenuRecyclerAdapter.OnItemClickListener {
                                        override fun addItemClick(menuItems: MenuItems) {

                                            orderList.add(
                                                OrderEntity(
                                                    menuItems.foodItemsId,
                                                    menuItems.foodItemsName,
                                                    menuItems.foodItemsCostPerPerson
                                                )
                                            )
                                            if (orderList.size > 0)
                                                btnProceedToCart.visibility = View.VISIBLE
                                        }

                                        override fun removeItemClick(menuItems: MenuItems) {

                                            orderList.remove(
                                                OrderEntity(
                                                    menuItems.foodItemsId,
                                                    menuItems.foodItemsName,
                                                    menuItems.foodItemsCostPerPerson
                                                )
                                            )
                                            if (orderList.isEmpty())
                                                btnProceedToCart.visibility = View.GONE
                                        }
                                    }
                                )
                            recyclerView.adapter = recyclerAdapter
                            recyclerView.layoutManager = layoutManager
                        } else {
                            makeText(
                                activity as Context,
                                "Some error occurred!!!",
                                LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        makeText(
                            activity as Context,
                            "Some unexpected error occurred!!!",
                            LENGTH_SHORT
                        )
                            .show()
                    }
                }
                ,
                Response.ErrorListener {
                    makeText(
                        activity as Context,
                        "Slow internet connection!!!",
                        LENGTH_SHORT
                    ).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["token"] = "526f51e288db30 "
                    return headers
                }
            }
            queue.add(stringRequest)
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
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Menu"
        (activity as AppCompatActivity?)!!.supportActionBar!!.title =
            sharedPreferences.getString("restaurantName", "No Name")
    }

    private fun init(view: View) {
        btnProceedToCart = view.findViewById(R.id.btnProceedToCart)
        recyclerView = view.findViewById(R.id.recycler)
        layoutManager = LinearLayoutManager(activity)
        sharedPreferences = activity!!.getSharedPreferences(
            getString(R.string.shared_pref), Context.MODE_PRIVATE
        )
        progressLayout = view.findViewById(R.id.progressLayout)
    }

    class EmptyPreviousOrder(
        val context: Context
    ) :
        AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "item-db")
                .fallbackToDestructiveMigration().build()
            db.orderDao().cancelOrder()
            db.close()
            return true
        }
    }
}


