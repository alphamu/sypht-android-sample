package com.alimuzaffar.sypht.onedrive.repo

import android.util.Log
import com.alimuzaffar.sypht.onedrive.database.TheDatabase
import com.alimuzaffar.sypht.onedrive.entity.Attachment
import com.alimuzaffar.sypht.onedrive.util.GraphHelper
import com.alimuzaffar.sypht.onedrive.util.allowedContentTypes
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.requests.extensions.IAttachmentCollectionPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AttachmentRepo {
    private val dao = TheDatabase.instance.attachmentDao()
    private val syphtRepo = SyphtRepo.get()
    private val emailRepo = EmailRepo.get()

    fun getAttachments(emailId: String): List<Attachment> {
        return dao.getAllAttachments(emailId)
    }

    fun fetchAttachmentsSync(emailId: String, received: String) {
        runBlocking {
            fetchAttachments(emailId, received)
        }
    }

    suspend fun fetchAttachments(emailId: String, received: String) = withContext(Dispatchers.IO) {
        GraphHelper.instance?.getAttachmentsForEmail(
            emailId,
            object : ICallback<IAttachmentCollectionPage> {
                override fun success(attach: IAttachmentCollectionPage) {
                    attach.currentPage.forEach {
                        val contentType = it.contentType
                        Log.d("GOTATTACHMENT", "${emailId.substring(emailId.length - 10)} - $contentType")
                        val attachmentName = it.name
                        val contentBytes = it.rawObject["contentBytes"].asString
                        val attachment = Attachment(
                            it.id,
                            emailId,
                            null,
                            attachmentName,
                            contentType,
                            contentBytes,
                            uploaded = false,
                            skip = false,
                            received = received
                        )
                        if (allowedContentTypes.contains(contentType)) {
                            emailRepo.updateProcessing(emailId, received, false)
                            dao.addAttachment(attachment)
                            SyphtRepo.get().uploadSync(attachment)
                        } else {
                            emailRepo.updateProcessing(emailId, received, true)
                        }
                    }
                }

                override fun failure(ex: ClientException?) {
                    Log.e("ATTACHMENT", "Error getting /me/messages/{}/attachments", ex)
                }
            })

    }


    companion object {
        private lateinit var instance: AttachmentRepo

        fun get(): AttachmentRepo {
            if (!::instance.isInitialized) {
                instance = AttachmentRepo()
            }
            return instance
        }
    }
}