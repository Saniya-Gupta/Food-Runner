package com.heer.foodRunner.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.heer.foodRunner.R

class OrderSuccessFragment : Fragment() {

    private lateinit var btnOkay: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_success, container, false)
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN    // Hide Status Bar
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()    // Hide Action Bar
        btnOkay = view.findViewById(R.id.btnOkay)
        btnOkay.setOnClickListener {
            activity?.finish()
        }
        // If the user doesn't press Ok button, end the activity after 2.5sec
        Handler().postDelayed({
            activity?.finish()
        }, 2500)
        return view
    }


}