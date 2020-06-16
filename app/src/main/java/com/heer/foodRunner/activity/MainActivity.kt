package com.heer.foodRunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.heer.foodRunner.R
import com.heer.foodRunner.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fl: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        setUpToolbar()
        setUpActionBarDrawerToggle()
        setUpNavigationBar()
        openHomeFragment()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Open drawer when hamburger item is clicked
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            !is HomeFragment -> openHomeFragment()
            else -> super.onBackPressed()
        }
    }

    private fun openHomeFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, HomeFragment()).commit()
        navigationView.setCheckedItem(R.id.action_home)
        supportActionBar?.title = "All Restaurants"
    }

    private fun setUpNavigationBar() {

        // Inflate the header layout programmatically
        val headerView = navigationView.inflateHeaderView(R.layout.drawer_header)
        val txtDrawerHead: TextView = headerView.findViewById(R.id.txtDrawerHead)
        val txtDrawerText: TextView = headerView.findViewById(R.id.txtDrawerText)

        // Set username and mobile number
        txtDrawerHead.text = getSharedPreferences(
            getString(R.string.shared_pref),
            Context.MODE_PRIVATE
        ).getString("name", "No name")
        txtDrawerText.text = getSharedPreferences(
            getString(R.string.shared_pref),
            Context.MODE_PRIVATE
        ).getString("num", "No num")

        val previousCheckedItem: MenuItem? = null

        navigationView.setNavigationItemSelectedListener {

            if (previousCheckedItem != null)
                previousCheckedItem.isChecked = false

            // Do not check action_logOut
            if (it.itemId == R.id.action_logOut)
                it.isCheckable = false
            else {
                it.isCheckable = true
                it.isChecked = true
            }

            /*  The closing of navigation drawer is delayed to make the transition smooth.
                Using a thread to close drawers
                We delay it by 0.1 second   */
            val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 100)

            val fragmentTransaction = supportFragmentManager.beginTransaction()

            when (it.itemId) {

                R.id.action_home -> {
                    openHomeFragment()
                    drawerLayout.closeDrawers()
                }

                R.id.action_profile -> {
                    fragmentTransaction.replace(R.id.frameLayout, ProfileFragment()).commit()
                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()
                }

                R.id.action_favRes -> {
                    fragmentTransaction.replace(R.id.frameLayout, FavouriteRestaurantFragment())
                        .commit()
                    supportActionBar?.title = "Favourite Restaurants"
                    drawerLayout.closeDrawers()
                }

                R.id.action_orderHistory -> {
                    fragmentTransaction.replace(R.id.frameLayout, OrderHistoryFragment()).commit()
                    supportActionBar?.title = "My Order History"
                    drawerLayout.closeDrawers()
                }

                R.id.action_faqs -> {
                    fragmentTransaction.replace(R.id.frameLayout, FaqFragment()).commit()
                    supportActionBar?.title = "Frequently Asked Questions"
                    drawerLayout.closeDrawers()
                }

                R.id.action_logOut -> {
                    val sharedPreferences: SharedPreferences = getSharedPreferences(
                        getString(R.string.shared_pref),
                        Context.MODE_PRIVATE
                    )
                    drawerLayout.closeDrawers()

                    /*Creating a confirmation dialog*/
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want exit?")
                        .setPositiveButton("Yes") { _, _ ->
                            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                        .setNegativeButton("No", null)
                        .create()
                        .show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun setUpActionBarDrawerToggle() {
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        // Disable swipe gesture to open the navigation drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setUpToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Display home button
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun init() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        fl = findViewById(R.id.frameLayout)
    }
}