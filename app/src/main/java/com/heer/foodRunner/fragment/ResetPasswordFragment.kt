package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.heer.foodRunner.R
import com.heer.foodRunner.activity.LoginActivity
import com.heer.foodRunner.util.ConnectionManager
import com.heer.foodRunner.util.RESET_PASSWORD
import org.json.JSONException
import org.json.JSONObject

class ResetPasswordFragment : Fragment() {

    private lateinit var etOtp: EditText
    private lateinit var etPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnSubmit: Button
    private lateinit var progressLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)
        init(view)
        (activity as LoginActivity).supportActionBar?.title = "Reset Password"
        btnSubmit.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            // Validate Details
            if (etPass.text.length > 5) {
                if (etPass.text.toString()
                        .compareTo(etConfirmPass.text.toString()) == 0
                ) {
                    val num = arguments?.getString("num")
                    println("Number : $num")
                    val jsonObj = JSONObject(
                        """{
                    |"mobile_number": "$num",
                    |"password": "${etPass.text}",
                    |"otp": "${etOtp.text}"
                    |}
                """.trimMargin()
                    )
                    sendOtpToServer(jsonObj)
                } else {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(
                        activity as Context,
                        "Passwords do not match.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                progressLayout.visibility = View.GONE
                Toast.makeText(
                    activity as Context,
                    "Password must be of at least 6 characters.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return view
    }

    private fun sendOtpToServer(jsonObj: JSONObject) {
        val queue = Volley.newRequestQueue(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, RESET_PASSWORD, jsonObj,
                    Response.Listener {
                        try {
                            progressLayout.visibility = View.GONE
                            val resObj = it.getJSONObject("data")
                            val resSuccess = resObj.getBoolean("success")
                            if (resSuccess) {
                                Toast.makeText(
                                    activity as Context,
                                    "Password changed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                this.activity?.supportFragmentManager?.beginTransaction()
                                    ?.replace(R.id.frameLayout, LoginFragment())?.commit()
                            } else {
                                Toast.makeText(
                                    activity as Context,
                                    "Invalid OTP",
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

    private fun init(view: View) {
        etOtp = view.findViewById(R.id.etOtp)
        etPass = view.findViewById(R.id.etPass)
        etConfirmPass = view.findViewById(R.id.etConfirmPass)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        progressLayout = view.findViewById(R.id.progressLayout)
    }
}