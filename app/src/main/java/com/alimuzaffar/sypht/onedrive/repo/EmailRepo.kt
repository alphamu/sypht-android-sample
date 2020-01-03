package com.alimuzaffar.sypht.onedrive.repo

import android.util.Log
import androidx.lifecycle.LiveData
import com.alimuzaffar.sypht.onedrive.database.TheDatabase
import com.alimuzaffar.sypht.onedrive.entity.Email
import com.alimuzaffar.sypht.onedrive.util.GraphHelper
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.requests.extensions.IMessageCollectionPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class EmailRepo {
    private val displayDate = SimpleDateFormat.getDateInstance()
    private val dao = TheDatabase.instance.emailDao()
    private lateinit var attachmentRepo: AttachmentRepo

    fun getEmails(): LiveData<List<Email>> {
        fetchEmails()
        return dao.getAllEmails()
    }

    fun updateProcessing(emailId: String, received: String, finished: Boolean) {
        dao.updateProcessing(emailId, received, finished)
    }

    private fun fetchEmails() {
        CoroutineScope(Dispatchers.IO).launch {
            GraphHelper.instance?.getEmailsWithAttachments(object :
                ICallback<IMessageCollectionPage> {
                override fun success(page: IMessageCollectionPage) {
                    Log.d("MESSAGES", "Recent: " + page.rawObject.toString())

                    page.currentPage.forEach { message ->
                        val emailId = message.id
                        val subject = message.subject
                        val emailAddress = message.sender.emailAddress
                        val from =
                            if (emailAddress.name.isNotBlank()) emailAddress.name else emailAddress.address
                        val received = displayDate.format(message.receivedDateTime.time)
                        Log.d("EMAILID", "$from - $emailId")
                        setEmail(emailId, subject, from, received)
                    }
                }

                override fun failure(ex: ClientException?) {
                    Log.e("MESSAGES", "Error getting /me/messages", ex)
                }
            })
        }
    }

    private fun setEmail(id: String, subject: String, from: String, received: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val count = dao.contains(id)
            Log.d("SETEMAIL", "$count - $received - ${id.substring(id.length - 10)}")
            if (count == 0L) {
                dao.addEmail(Email(id, subject, from, received))
                if (!::attachmentRepo.isInitialized) {
                    attachmentRepo = AttachmentRepo.get()
                }
                attachmentRepo.fetchAttachments(id, received)
            }
        }
    }

    companion object {
        private lateinit var instance: EmailRepo

        fun get(): EmailRepo {
            if (!::instance.isInitialized) {
                instance = EmailRepo()
            }

            return instance
        }
    }
}