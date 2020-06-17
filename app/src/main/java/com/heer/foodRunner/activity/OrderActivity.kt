package com.heer.foodRunner.activity

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.heer.foodRunner.R
import com.heer.foodRunner.fragment.CartFragment
import com.heer.foodRunner.fragment.MenuFragment
import com.heer.foodRunner.fragment.OrderSuccessFragment

class OrderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        setUpToolbar()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, MenuFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Add functionality to action bar back button
        onBackPressed()
        return true
    }

    private fun setUpToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Display home button
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            is CartFragment -> {
                /* Creating Confirmation dialog */
                AlertDialog.Builder(this).setTitle("Exit Cart")
                    .setMessage("Order items will be discarded.\nDo you still want to continue?")
                    .setPositiveButton("No") { _, _ -> }
                    .setNegativeButton("Yes") { _, _ ->
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, MenuFragment()).commit()
                    }
                    .create().show()

            }
            is OrderSuccessFragment -> finish()
            else -> super.onBackPressed()
        }
    }
}