package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.heer.foodRunner.R
import com.heer.foodRunner.activity.LoginActivity
import com.heer.foodRunner.activity.MainActivity
import com.heer.foodRunner.util.ConnectionManager
import com.heer.foodRunner.util.LOGIN
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class LoginFragment : Fragment() {

    private lateinit var etNum: EditText
    private lateinit var etPass: EditText
    private lateinit var txtForgotPass: TextView
    private lateinit var txtRegister: TextView
    private lateinit var btnLogin: Button
    private lateinit var progressLayout: RelativeLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        init(view)
        (activity as LoginActivity).supportActionBar?.title = "Login"
        btnLogin.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            if (Pattern.matches(
                    "[789][0-9]{9}",
                    etNum.text.toString()
                )
            ) {
                if (etPass.text.length > 5) {
                    val jsonObject = JSONObject(
                        """{
                    |"mobile_number": "${etNum.text}",
                    |"password":"${etPass.text}"
                    | }
                """.trimMargin()
                    )
                    sendLoginDetailsToServer(jsonObject)
                } else {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(activity, "Invalid password", Toast.LENGTH_SHORT).show()
                }
            } else {
                progressLayout.visibility = View.GONE
                Toast.makeText(activity, "Invalid Mobile Number", Toast.LENGTH_SHORT).show()
            }
        }
        txtRegister.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.frameLayout,
                RegisterFragment()
            )?.commit()
        }
        txtForgotPass.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.frameLayout,
                ForgotPasswordFragment()
            )?.commit()
        }
        return view
    }

    private fun sendLoginDetailsToServer(jsonObj: JSONObject) {
        val queue = Volley.newRequestQueue(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(
                    Method.POST, LOGIN, jsonObj,
                    Response.Listener {
                        try {
                            val resObj = it.getJSONObject("data")
                            val resSuccess = resObj.getBoolean("success")
                            if (resSuccess) {
                                sharedPreferences = activity!!.getSharedPreferences(
                                    getString(R.string.shared_pref),
                                    Context.MODE_PRIVATE
                                )
                                val resProfileObj = resObj.getJSONObject("data")
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isLoggedIn", true).apply()
                                editor.putString("userId", resProfileObj.getString("user_id"))
                                    .apply()
                                editor.putString("name", resProfileObj.getString("name")).apply()
                                editor.putString("num", resProfileObj.getString("mobile_number"))
                                    .apply()
                                editor.putString("email", resProfileObj.getString("email")).apply()
                                editor.putString("add", resProfileObj.getString("address")).apply()
                                startActivity(Intent(this.activity, MainActivity::class.java))
                                this.activity?.finish()
                            } else {
                                progressLayout.visibility = View.GONE
                                Toast.makeText(
                                    activity as Context,
                                    "Invalid login details",
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
        etPass = view.findViewById(R.id.etPass)
        txtForgotPass = view.findViewById(R.id.txtForgotPass)
        txtRegister = view.findViewById(R.id.txtRegister)
        btnLogin = view.findViewById(R.id.btnLogin)
        progressLayout = view.findViewById(R.id.progressLayout)

        sharedPreferences = activity!!.getSharedPreferences(
            getString(R.string.shared_pref),
            Context.MODE_PRIVATE
        )
    }
}
