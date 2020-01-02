package com.alimuzaffar.sypht.onedrive.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.alimuzaffar.sypht.onedrive.R
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.SignOutCallback
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException

// Singleton class - the app only needs a single instance
// of PublicClientApplication
class AuthenticationHelper private constructor(ctx: Context) {
    private lateinit var mPCA: ISingleAccountPublicClientApplication
    private val mScopes =
        arrayOf("User.Read", "Calendars.Read", "Files.Read", "Mail.Read")

    fun acquireTokenInteractively(
        activity: Activity?,
        callback: AuthenticationCallback?
    ) {
        mPCA.signIn(activity!!, "", mScopes, callback!!)
    }

    fun acquireTokenSilently(callback: AuthenticationCallback?) { // Get the authority from MSAL config
        val authority =
            mPCA.configuration.defaultAuthority.authorityURL.toString()
        mPCA.acquireTokenSilentAsync(mScopes, authority, callback!!)
    }

    fun signOut() {
        mPCA.signOut(object : SignOutCallback {
            override fun onSignOut() {
                Log.d("AUTHHELPER", "Signed out")
            }

            override fun onError(exception: MsalException) {
                Log.d("AUTHHELPER", "MSAL error signing out", exception)
            }
        })
    }

    companion object {
        private var INSTANCE: AuthenticationHelper? = null
        @Synchronized
        fun getInstance(ctx: Context): AuthenticationHelper? {
            if (INSTANCE == null) {
                INSTANCE =
                    AuthenticationHelper(ctx)
            }
            return INSTANCE
        }

        // Version called from fragments. Does not create an
        // instance if one doesn't exist
        @get:Synchronized
        val instance: AuthenticationHelper?
            get() {
                checkNotNull(INSTANCE) { "AuthenticationHelper has not been initialized from MainActivity" }
                return INSTANCE
            }
    }

    init {
        PublicClientApplication.createSingleAccountPublicClientApplication(ctx,
            R.raw.msal_config,
            object : ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mPCA = application
                }

                override fun onError(exception: MsalException) {
                    Log.e("AUTHHELPER", "Error creating MSAL application", exception)
                }
            })
    }
}