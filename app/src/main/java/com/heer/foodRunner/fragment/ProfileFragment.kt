package com.heer.foodRunner.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.heer.foodRunner.R

class ProfileFragment : Fragment() {

    private lateinit var txtName: TextView
    private lateinit var txtNum: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAdd: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        init(view)
        setContent()
        return view
    }

    private fun setContent() {
        val email = "Email: " + sharedPreferences.getString("email", "No Email")
        val add = "Address: " + sharedPreferences.getString("add", "No Add")
        txtName.text = sharedPreferences.getString("name", "No Name")
        txtNum.text = sharedPreferences.getString("num", "No Number")
        txtEmail.text = email
        txtAdd.text = add
    }

    private fun init(view: View) {
        txtName = view.findViewById(R.id.txtName)
        txtNum = view.findViewById(R.id.txtNum)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtAdd = view.findViewById(R.id.txtAdd)
        sharedPreferences =
            activity!!.getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
    }
}