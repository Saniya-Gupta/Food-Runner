package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
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
import com.heer.foodRunner.util.FORGOT_PASSWORD
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class ForgotPasswordFragment : Fragment() {

    private lateinit var etNum: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnNext: Button
    private lateinit var progressLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)
        init(view)
        (activity as LoginActivity).supportActionBar?.title = "Forgot Password"
        btnNext.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            // Validate details
            if (etNum.text.isNotEmpty() && etEmail.text.isNotEmpty()) {
                if (Pattern.matches(
                        "[789][0-9]{9}",
                        etNum.text.toString()
                    ) && Patterns.EMAIL_ADDRESS.matcher(etEmail.text).matches()
                ) {
                    val jsonObject = JSONObject(
                        """{
                    |"mobile_number": "${etNum.text}",
                    |"email":"${etEmail.text}"
                    | }
                """.trimMargin()
                    )
                    sendDetailsToServer(jsonObject)
                } else {
                    Toast.makeText(activity, "Invalid details", Toast.LENGTH_SHORT).show()
                    progressLayout.visibility = View.GONE
                }
            } else {
                Toast.makeText(activity, "None of the fields can be empty", Toast.LENGTH_SHORT)
                    .show()
                progressLayout.visibility = View.GONE
            }
        }
        return view
    }

    private fun sendDetailsToServer(jsonObj: JSONObject) {
        val queue = Volley.newRequestQueue(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(
                    Method.POST, FORGOT_PASSWORD, jsonObj,
                    Response.Listener {
                        try {
                            progressLayout.visibility = View.GONE
                            val resObj = it.getJSONObject("data")
                            val resSuccess = resObj.getBoolean("success")
                            if (resSuccess) {
                                if (resObj.getBoolean("first_try")) {
                                    Toast.makeText(
                                        activity as Context,
                                        "OTP Sent",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else
                                    Toast.makeText(
                                        activity as Context,
                                        "OTP has been sent",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                val otpFragment = ResetPasswordFragment()
                                val bundle = Bundle()
                                bundle.putString("num", jsonObj.getString("mobile_number"))
                                otpFragment.arguments = bundle
                                activity?.supportFragmentManager?.beginTransaction()?.replace(
                                    R.id.frameLayout,
                                    otpFragment
                                )?.commit()
                            } else {
                                Toast.makeText(
                                    activity as Context,
                                    "Verify your Number and Email id",
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
        etNum = view.findViewById(R.id.etNum)
        etEmail = view.findViewById(R.id.etEmail)
        btnNext = view.findViewById(R.id.btnNext)
        progressLayout = view.findViewById(R.id.progressLayout)
    }
}