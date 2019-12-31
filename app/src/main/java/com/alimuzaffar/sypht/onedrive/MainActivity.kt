package com.alimuzaffar.sypht.onedrive

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.models.extensions.User
import com.microsoft.graph.requests.extensions.IDriveRecentCollectionPage
import com.microsoft.graph.requests.extensions.IMessageCollectionPage
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import com.sypht.SyphtClient
import com.sypht.auth.BasicCredentialProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File


class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mDrawer: DrawerLayout
    private lateinit var mNavigationView: NavigationView
    private lateinit var mHeaderView: View
    private var mIsSignedIn = false
    private var mUserName: String? = null
    private var mUserEmail: String? = null
    private lateinit var mAuthHelper: AuthenticationHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Prefs.init(this)
        // Set the toolbar
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        mDrawer = findViewById(R.id.drawer_layout)
        // Add the hamburger menu icon
        val toggle =
            ActionBarDrawerToggle(
                this, mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            )
        mDrawer.addDrawerListener(toggle)
        toggle.syncState()
        mNavigationView = findViewById(R.id.nav_view)
        // Set user name and email
        mHeaderView = mNavigationView.getHeaderView(0)
        setSignedInState(mIsSignedIn)
        // Listen for item select events on menu
        mNavigationView.setNavigationItemSelectedListener(this)
        // Load the home fragment by default on startup
        if (savedInstanceState == null) {
            openHomeFragment(mUserName);
        }
        // Get the authentication helper
        AuthenticationHelper.getInstance(applicationContext)?.run {
            mAuthHelper = this
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean { // Load the fragment that corresponds to the selected item
        when (menuItem.itemId) {
            R.id.nav_home -> openHomeFragment(mUserName)
            R.id.nav_calendar -> openCalendarFragment()
            R.id.nav_signin -> signIn()
            R.id.nav_signout -> signOut()
        }
        mDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun showProgressBar() {
        val container = findViewById<FrameLayout>(R.id.fragment_container)
        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        container.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        val container = findViewById<FrameLayout>(R.id.fragment_container)
        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        progressBar.visibility = View.GONE
        container.visibility = View.VISIBLE
    }

    // Update the menu and get the user's name and email
    private fun setSignedInState(isSignedIn: Boolean) {
        mIsSignedIn = isSignedIn
        val menu = mNavigationView.menu
        // Hide/show the Sign in, Calendar, and Sign Out buttons
        menu.findItem(R.id.nav_signin).isVisible = !isSignedIn
        menu.findItem(R.id.nav_calendar).isVisible = isSignedIn
        menu.findItem(R.id.nav_signout).isVisible = isSignedIn
        // Set the user name and email in the nav drawer
        val userName = mHeaderView.findViewById<TextView>(R.id.user_name)
        val userEmail = mHeaderView.findViewById<TextView>(R.id.user_email)
        if (isSignedIn) {
            userName.text = mUserName
            userEmail.text = mUserEmail
        } else {
            mUserName = null
            mUserEmail = null
            userName.text = "Please sign in"
            userEmail.text = ""
        }
    }

    // Load the "Home" fragment
    fun openHomeFragment(userName: String?) {
        val fragment = HomeFragment.createInstance(userName)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        mNavigationView.setCheckedItem(R.id.nav_home)
    }

    // Load the "Calendar" fragment
    private fun openCalendarFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CalendarFragment())
            .commit()
        mNavigationView.setCheckedItem(R.id.nav_calendar)
    }

    private fun signIn() {
        showProgressBar()
        // Attempt silent sign in first
        // if this fails, the callback will handle doing
        // interactive sign in
        doSilentSignIn()
    }

    private fun signOut() {
        mAuthHelper.signOut()
        setSignedInState(false)
        openHomeFragment(mUserName)
    }

    // Silently sign in - used if there is already a
// user account in the MSAL cache
    private fun doSilentSignIn() {
        mAuthHelper.acquireTokenSilently(getAuthCallback())
    }

    // Prompt the user to sign in
    private fun doInteractiveSignIn() {
        mAuthHelper.acquireTokenInteractively(this, getAuthCallback())
    }

    // Handles the authentication result
    fun getAuthCallback(): AuthenticationCallback? {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) { // Log the token for debug purposes
                val accessToken = authenticationResult.accessToken
                Log.d("AUTH", String.format("Access token: %s", accessToken))
                // Get Graph client and get user
                Prefs.instance.setAccessToken(accessToken);
                GraphHelper.instance?.getUser(getUserCallback())
                getRecentFiles()
            }

            override fun onError(exception: MsalException) { // Check the type of exception and handle appropriately
                if (exception is MsalUiRequiredException) {
                    Log.d("AUTH", "Interactive login required")
                    doInteractiveSignIn()
                } else if (exception is MsalClientException) {
                    if (exception.getErrorCode() === "no_current_account") {
                        Log.d("AUTH", "No current account, interactive login required")
                        doInteractiveSignIn()
                    } else { // Exception inside MSAL, more info inside MsalError.java
                        Log.e("AUTH", "Client error authenticating", exception)
                    }
                } else if (exception is MsalServiceException) { // Exception when communicating with the auth server, likely config issue
                    Log.e("AUTH", "Service error authenticating", exception)
                }
                hideProgressBar()
            }

            override fun onCancel() { // User canceled the authentication
                Log.d("AUTH", "Authentication canceled")
                hideProgressBar()
            }
        }
    }

    private fun getUserCallback(): ICallback<User> {
        return object : ICallback<User> {
            override fun success(user: User) {
                Log.d("AUTH", "User: " + user.displayName)
                mUserName = user.displayName
                mUserEmail = if (user.mail == null) user.userPrincipalName else user.mail
                runOnUiThread {
                    hideProgressBar()
                    setSignedInState(true)
                    openHomeFragment(mUserName)
                }
            }

            override fun failure(ex: ClientException?) {
                Log.e("AUTH", "Error getting /me", ex)
                mUserName = "ERROR"
                mUserEmail = "ERROR"
                runOnUiThread {
                    hideProgressBar()
                    setSignedInState(true)
                    openHomeFragment(mUserName)
                }
            }
        }
    }

    fun getEmailsWithAttachments() {
        GraphHelper.instance?.getEmailsWithAttachments(object: ICallback<IMessageCollectionPage> {
            override fun success(page: IMessageCollectionPage) {
                Log.d("MESSAGES", "Recent: " + page.rawObject.toString())
            }

            override fun failure(ex: ClientException?) {
                Log.e("MESSAGES", "Error getting /me/messages", ex)
            }
        })
    }

    fun getRecentFiles() {
        GraphHelper.instance?.getRecentFiles(getRecentFilesCallback())
    }

    private fun getRecentFilesCallback(): ICallback<IDriveRecentCollectionPage> {
        return object : ICallback<IDriveRecentCollectionPage> {
            override fun success(page: IDriveRecentCollectionPage) {
                Log.d("RECENT", "Recent: " + page.rawObject.toString())
                val itemId = page.currentPage[0].remoteItem.id
                val name = page.currentPage[0].remoteItem.name
                getFileById(itemId, name)
            }

            override fun failure(ex: ClientException?) {
                Log.e("RECENT", "Error getting /me/drive/recent", ex)
            }
        }
    }

    fun getFileById(fileId: String, fileName: String) {
        val file = File(getExternalFilesDir(null), fileName);
        val request =
            DownloadManager.Request(Uri.parse(GraphHelper.instance?.getFileStreamUrl(fileId)))
                .setTitle(fileName)// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setRequiresCharging(false)// Set if charging is required to begin the download
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        request.addRequestHeader("Authorization", "Bearer ${Prefs.instance.getAccessToken()}")
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                GlobalScope.launch {
                    sendToSypht(file)
                }
                unregisterReceiver(this)
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun sendToSypht(file: File) {
        val sypht = SyphtClient(
            BasicCredentialProvider(
                getString(R.string.sypht_client_id),
                getString(R.string.sypht_client_secret)
            )
        )

        val syphtFileId = sypht.upload(file.name, BufferedInputStream(file.inputStream()))
        val syphtResult = sypht.result(syphtFileId)
        Log.d("SYPHT", syphtResult)
    }

}