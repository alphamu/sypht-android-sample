package com.alimuzaffar.sypht.onedrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    private var mUserName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mUserName = arguments!!.getString(USER_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeView: View = inflater.inflate(R.layout.fragment_home, container, false)
        // If there is a username, replace the "Please sign in" with the username
        if (mUserName != null) {
            val userName = homeView.findViewById<TextView>(R.id.home_page_username)
            userName.text = mUserName
        }
        return homeView
    }

    companion object {
        private const val USER_NAME = "userName"
        fun createInstance(userName: String?): HomeFragment {
            val fragment = HomeFragment()
            // Add the provided username to the fragment's arguments
            val args = Bundle()
            args.putString(USER_NAME, userName)
            fragment.arguments = args
            return fragment
        }
    }
}