package com.heer.foodRunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.heer.foodRunner.activity.MainActivity
import com.heer.foodRunner.util.ConnectionManager
import com.heer.foodRunner.util.REGISTER
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class RegisterFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etNum: EditText
    private lateinit var etAdd: EditText
    private lateinit var etPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressLayout: RelativeLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        init(view)
        (activity as LoginActivity).supportActionBar?.title = "Register"
        btnRegister.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            if (validateRegistrationDetails()) {
                val jsonObj = JSONObject(
                    """{
                    |"name" : "${etName.text}",
                    |"mobile_number" : "${etNum.text}",
                    |"password" : "${etPass.text}",
                    |"address" : "${etAdd.text}",
                    |"email" : "${etEmail.text}"
                    |}""".trimMargin()
                )
                sendRegisterRequestToServer(jsonObj)
            }
        }
        return view
    }

    private fun sendRegisterRequestToServer(jsonObj: JSONObject) {
        val queue = Volley.newRequestQueue(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, REGISTER, jsonObj,
                    Response.Listener {
                        try {
                            progressLayout.visibility = View.GONE
                            val resObj = it.getJSONObject("data")
                            println("JsonObject: $resObj")
                            val resSuccess = resObj.getBoolean("success")
                            if (resSuccess) {
                                Toast.makeText(
                                    activity as Context,
                                    "Registration Successful",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                Toast.makeText(
                                    activity as Context,
                                    resObj.getString("errorMessage"),
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

    private fun validateRegistrationDetails(): Boolean {
        if (etName.text.isNotEmpty()) {
            if (etEmail.text.isNotEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(etEmail.text).matches()) {
                    if (etNum.text.isNotEmpty()) {
                        if (Pattern.matches("[789][0-9]{9}", etNum.text)) {
                            if (etAdd.text.isNotEmpty()) {
                                if (etPass.text.isNotEmpty()) {
                                    if (etPass.text.length > 5) {
                                        if (etPass.text.toString()
                                                .compareTo(etConfirmPass.text.toString()) == 0
                                        ) {
                                            return true
                                        } else {
                                            Toast.makeText(
                                                activity as Context,
                                                "Passwords do not match",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            activity as Context,
                                            "Length of Password must be at least 6",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        activity as Context,
                                        "Password cannot be empty",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    activity as Context,
                                    "Address cannot be empty",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Invalid Mobile Number",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            activity as Context,
                            "Mobile Number cannot be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        activity as Context,
                        "Invalid Email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    activity as Context,
                    "Email cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(activity as Context, "Name cannot be empty", Toast.LENGTH_SHORT)
                .show()
        }
        progressLayout.visibility = View.GONE
        return false
    }

    private fun init(view: View) {
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etNum = view.findViewById(R.id.etNum)
        etAdd = view.findViewById(R.id.etAdd)
        etPass = view.findViewById(R.id.etPass)
        etConfirmPass = view.findViewById(R.id.etConfirmPass)
        btnRegister = view.findViewById(R.id.btnRegister)
        progressLayout = view.findViewById(R.id.progressLayout)
    }
}
