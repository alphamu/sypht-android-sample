package com.alimuzaffar.sypht.onedrive.util

import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.http.IHttpRequest
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.microsoft.graph.models.extensions.User
import com.microsoft.graph.options.HeaderOption
import com.microsoft.graph.options.QueryOption
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.microsoft.graph.requests.extensions.IAttachmentCollectionPage
import com.microsoft.graph.requests.extensions.IDriveRecentCollectionPage
import com.microsoft.graph.requests.extensions.IMessageCollectionPage
import java.io.InputStream

// Singleton class - the app only needs a single instance
// of the Graph client
class GraphHelper private constructor() : IAuthenticationProvider {
    private val mClient: IGraphServiceClient = GraphServiceClient.builder()
        .authenticationProvider(this).buildClient()

    // Part of the Graph IAuthenticationProvider interface
// This method is called before sending the HTTP request
    override fun authenticateRequest(request: IHttpRequest) { // Add the access token in the Authorization header
        if (Prefs.instance.getAccessToken().isNotEmpty()) {
            request.addHeader("Authorization", "Bearer ${Prefs.instance.getAccessToken()}")
        }
    }

    fun getUser(
        callback: ICallback<User>
    ) {
        // GET /me (logged in user)
        mClient.me().buildRequest()[callback]
    }

    fun getRecentFiles(
        callback: ICallback<IDriveRecentCollectionPage>
    ) {
        // GET /me/drive/recent (logged in user)
        mClient.me().drive().recent()
            .buildRequest()[callback]
    }

    fun getFileStream(fileId: String, callback: ICallback<InputStream>) {
        // GET /me/drive/items/:id/content (logged in user)
        mClient.me().drive().items().byId(fileId).content().buildRequest(
            listOf(
                HeaderOption(
                    "Accept",
                    "application/x-www-form-urlencoded"
                )
            )
        )[callback]
    }

    fun getFileStreamUrl(fileId: String): String {
        // GET /me/drive/items/:id/content (logged in user)
        return mClient.me().drive().items().byId(fileId).content().requestUrl
    }

    fun getEmailsWithAttachments(callback: ICallback<IMessageCollectionPage>) {
        // https://graph.microsoft.com/v1.0/me/messages?$filter=hasAttachments+eq+true&$select=sender,subject,hasAttachments
        return mClient.me().messages().buildRequest(
            listOf(
//                QueryOption(""\$filter", "hasAttachments eq true"),
//                QueryOption("\$select", "subject,sender,hasAttachments"),
                QueryOption("\$orderby", "receivedDateTime desc")
            )
        )[callback]
    }

    fun getAttachmentsForEmail(emailId: String, callback: ICallback<IAttachmentCollectionPage>) {
        return mClient.me().messages().byId(emailId).attachments().buildRequest()[callback]
    }

    companion object {
        private var INSTANCE: GraphHelper? = null
        @get:Synchronized
        val instance: GraphHelper?
            get() {
                if (INSTANCE == null) {
                    INSTANCE =
                        GraphHelper()
                }
                return INSTANCE
            }
    }

}