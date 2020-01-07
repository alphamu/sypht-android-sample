package com.alimuzaffar.sypht.onedrive.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.alimuzaffar.sypht.onedrive.repo.AttachmentRepo

// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
private const val ACTION_FETCH_ATTACHMENTS = "com.alimuzaffar.sypht.onedrive.service.action.FETCH_ATTACHMENTS"
private const val ACTION_UPLOAD_TO_SYPHT = "com.alimuzaffar.sypht.onedrive.service.action.UPLOAD_TO_SYPHT"

private const val EXTRA_EMAIL_ID = "com.alimuzaffar.sypht.onedrive.service.extra.EMAIL_ID"
private const val EXTRA_EMAIL_RECEIVED = "com.alimuzaffar.sypht.onedrive.service.extra.EMAIL_RECEIVED"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class ProcessAttachments : IntentService("ProcessAttachments") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_FETCH_ATTACHMENTS -> {
                val param1 = intent.getStringExtra(EXTRA_EMAIL_ID)
                val param2 = intent.getStringExtra(EXTRA_EMAIL_RECEIVED)
                handleFetchAttachments(param1, param2)
            }
            ACTION_UPLOAD_TO_SYPHT -> {
                val param1 = intent.getStringExtra(EXTRA_EMAIL_ID)
                val param2 = intent.getStringExtra(EXTRA_EMAIL_RECEIVED)
                handleActionBaz(param1, param2)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleFetchAttachments(emailId: String, received: String) {
        AttachmentRepo.get().fetchAttachmentsSync(emailId, received)
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        TODO("Handle action Baz")
    }

    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startFetchAttachments(context: Context, param1: String, param2: String) {
            val intent = Intent(context, ProcessAttachments::class.java).apply {
                action = ACTION_FETCH_ATTACHMENTS
                putExtra(EXTRA_EMAIL_ID, param1)
                putExtra(EXTRA_EMAIL_RECEIVED, param2)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startUploadToSypht(context: Context, param1: String, param2: String) {
            val intent = Intent(context, ProcessAttachments::class.java).apply {
                action = ACTION_UPLOAD_TO_SYPHT
                putExtra(EXTRA_EMAIL_ID, param1)
                putExtra(EXTRA_EMAIL_RECEIVED, param2)
            }
            context.startService(intent)
        }
    }
}
