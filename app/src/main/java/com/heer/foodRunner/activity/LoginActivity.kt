package com.heer.foodRunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import com.heer.foodRunner.R
import com.heer.foodRunner.fragment.LoginFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var frameLayout: FrameLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()
        setUpToolbar()
        openHomeOrLoginActivity()
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            !is LoginFragment -> openLoginFragment()
            else -> super.onBackPressed()
        }
    }

    private fun openHomeOrLoginActivity() {
        // Check if user is logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        } else
            openLoginFragment()
    }

    private fun openLoginFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, LoginFragment())
            .commit()
    }

    private fun setUpToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun init() {
        frameLayout = findViewById(R.id.frameLayout)
        sharedPreferences = getSharedPreferences(
            getString(R.string.shared_pref),
            Context.MODE_PRIVATE
        )
    }
}
