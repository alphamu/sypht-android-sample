package com.alimuzaffar.sypht.onedrive

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.models.extensions.Message
import com.microsoft.graph.requests.extensions.IAttachmentCollectionPage
import com.microsoft.graph.requests.extensions.IMessageCollectionPage
import com.sypht.SyphtClient
import com.sypht.auth.BasicCredentialProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.InputStream

class EmailsFragment : Fragment(), EmailsAdapter.OnItemTap {
    val allowedContentTypes =
        arrayOf("application/pdf", "application/png", "application/jpeg", "application/jpg")
    val displayFields = arrayOf(
        "invoice.tax",
        "invoice.gst",
        "invoice.total",
        "invoice.amountDue",
        "invoice.dueDate"
    )
    val mapIds = mutableMapOf<String, String>()

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        recyclerView = inflater.inflate(R.layout.fragment_emails, container, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.setHasFixedSize(true)

        return recyclerView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getSyphtClient(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getEmailsWithAttachments()
    }

    fun getEmailsWithAttachments() {
        GraphHelper.instance?.getEmailsWithAttachments(object : ICallback<IMessageCollectionPage> {
            override fun success(page: IMessageCollectionPage) {
                Log.d("MESSAGES", "Recent: " + page.rawObject.toString())
                activity?.runOnUiThread {
                    recyclerView.adapter = EmailsAdapter(page.currentPage, this@EmailsFragment)
                }
                page.currentPage.forEach { message ->
                    val emailId = message.id
                    GraphHelper.instance?.getAttachmentsForEmail(
                        emailId,
                        object : ICallback<IAttachmentCollectionPage> {
                            override fun success(attach: IAttachmentCollectionPage) {
                                attach.currentPage.forEach {
                                    val contentType = it.contentType
                                    if (allowedContentTypes.contains(contentType)) {
                                        val attachmentName = it.name
                                        val contentBytes = it.rawObject["contentBytes"].asString
                                        val bytes = Base64.decode(contentBytes, Base64.NO_WRAP)
                                        val stream = ByteArrayInputStream(bytes)
                                        GlobalScope.launch {
                                            val syphtId = sendToSypht(attachmentName, stream)
                                            mapIds[emailId] = syphtId
                                            //No need to close the stream, the SDk does this for us.
                                        }
                                    }
                                }
                            }

                            override fun failure(ex: ClientException?) {
                                Log.e("ATTACHMENT", "Error getting /me/messages/{}/attachments", ex)
                            }
                        })
                }

            }

            override fun failure(ex: ClientException?) {
                Log.e("MESSAGES", "Error getting /me/messages", ex)
            }
        })
    }

    fun sendToSypht(name: String, inputStream: InputStream): String {
        val syphtFileId =
            sypht.upload(name, inputStream, arrayOf("sypht.invoice"))
        return syphtFileId
    }

    override fun onItemTap(message: Message) {
        val sid = mapIds[message.id]
        if (sid != null) {
            (activity as MainActivity).showProgressBar()
            GlobalScope.launch {
                var display = ""
                JSONObject(sypht.result(sid)).takeIf { result ->
                    result.getString("status") == "FINALISED"
                }?.let { it ->
                    val fields = it.getJSONObject("results").getJSONArray("fields")
                    for (x in 0 until fields.length()) {
                        val field = fields.getJSONObject(x)
                        field.getString("name").takeIf { name ->
                            displayFields.contains(name)
                        }?.let {
                            display += "$it ${field.getString("value")}\n"
                        }
                    }
                    Log.d("SYPHT", display)
                    activity?.runOnUiThread {
                        (activity as MainActivity).hideProgressBar()
                        Toast.makeText(context, display, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Attachment still uploading, or not is a PDF or Image.", Toast.LENGTH_LONG).show()
        }

    }

    companion object {
        fun createInstance(): EmailsFragment {
            return EmailsFragment()
        }

        private lateinit var sypht: SyphtClient
        fun getSyphtClient(context: Context): SyphtClient {
            if (!::sypht.isInitialized)
                sypht = SyphtClient(
                    BasicCredentialProvider(
                        context.getString(R.string.sypht_client_id),
                        context.getString(R.string.sypht_client_secret)
                    )
                )
            return sypht
        }
    }
}