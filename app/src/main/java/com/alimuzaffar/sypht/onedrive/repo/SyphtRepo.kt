package com.alimuzaffar.sypht.onedrive.repo

import android.util.Base64
import android.util.Log
import com.alimuzaffar.sypht.onedrive.database.TheDatabase
import com.alimuzaffar.sypht.onedrive.entity.Attachment
import com.alimuzaffar.sypht.onedrive.entity.SyphtResult
import com.alimuzaffar.sypht.onedrive.util.allowedContentTypes
import com.sypht.SyphtClient
import com.sypht.auth.BasicCredentialProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream

class SyphtRepo(syphtClientId: String, syphtClientSecret: String) {
    private val sypht = SyphtClient(BasicCredentialProvider(syphtClientId, syphtClientSecret))
    private val attachmentDao = TheDatabase.instance.attachmentDao()
    private val dao = TheDatabase.instance.syphtDao()
    private val emailRepo = EmailRepo.get()

    fun getResult(attachmentId: String): SyphtResult? {
        return dao.getResultForAttachment(attachmentId)
    }

    fun getFinalisedResults(emailId: String): List<SyphtResult> {
        return dao.getFinalisedResultsForEmail(emailId)
    }

    fun upload(attachment: Attachment) {
        if (allowedContentTypes.contains(attachment.contentType)) {
            CoroutineScope(Dispatchers.IO).launch {
                val bytes = Base64.decode(attachment.contentBytes, Base64.NO_WRAP)
                val stream = ByteArrayInputStream(bytes)
                val syphtId = sendToSypht(attachment.name, stream)
                attachment.syphtId = syphtId
                attachment.uploaded = !syphtId.isNullOrEmpty()
                attachmentDao.updateAttachment(attachment)
                if (syphtId != null) {
                    val syphtResult = SyphtResult(syphtId, attachment.id, attachment.emailId)
                    dao.add(syphtResult)
                    getResultFromSypht(syphtId)?.let {
                        syphtResult.result = it
                        dao.update(syphtResult)
                    }
                    emailRepo.updateProcessing(attachment.emailId, attachment.received,true)
                }
            }
        } else {
            attachment.skip = true
            attachmentDao.updateAttachment(attachment)
            emailRepo.updateProcessing(attachment.emailId, attachment.received,true)
        }
    }

    private fun sendToSypht(name: String, inputStream: InputStream): String? {
        try {
            val syphtFileId =
                sypht.upload(name, inputStream, arrayOf("sypht.invoice"))
            return syphtFileId
        } catch (e: Exception) {
            Log.e("SyphtRepo", "ERROR UPLOADING FILE", e)
            return null
        }
    }

    private fun getResultFromSypht(syphtId: String): String? {
        try {
            return sypht.result(syphtId)
        } catch (e: java.lang.Exception) {
            Log.e("SyphtRepo", "ERROR GETTING RESULT for SyphyId: $syphtId", e)
            return null
        }
    }

    companion object {
        private lateinit var instance: SyphtRepo
        private val ANY = Any()
        fun get(): SyphtRepo {
            synchronized(ANY) {
                if (!::instance.isInitialized) {
                    throw ExceptionInInitializerError("You must call SyphtRepo.init()")
                }
                return instance
            }
        }

        fun init(syphtClientId: String, syphtClientSecret: String) {
            synchronized(ANY) {
                if (!::instance.isInitialized) {
                    instance = SyphtRepo(syphtClientId, syphtClientSecret)
                }
            }
        }
    }
}