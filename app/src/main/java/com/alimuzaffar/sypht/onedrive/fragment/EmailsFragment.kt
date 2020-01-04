package com.alimuzaffar.sypht.onedrive.fragment

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alimuzaffar.sypht.onedrive.R
import com.alimuzaffar.sypht.onedrive.adapter.EmailsAdapter
import com.alimuzaffar.sypht.onedrive.entity.Email
import com.alimuzaffar.sypht.onedrive.repo.AttachmentRepo
import com.alimuzaffar.sypht.onedrive.repo.EmailRepo
import com.alimuzaffar.sypht.onedrive.repo.SyphtRepo
import com.alimuzaffar.sypht.onedrive.util.allowedContentTypes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class EmailsFragment : Fragment(),
    EmailsAdapter.OnItemTap {

    val mapOfFields = mapOf<String, String?>(
        "invoice.tax" to null,
        "invoice.gst" to null,
        "invoice.total" to null,
        "invoice.amountDue" to null,
        "invoice.dueDate" to null
    )

    val displayFields = mapOfFields.keys

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        recyclerView = inflater.inflate(R.layout.fragment_emails, container, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.setHasFixedSize(true)

        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = EmailsAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter
        EmailRepo.get().getEmails().observe(this, Observer {
            adapter.myDataset = it
            adapter.notifyDataSetChanged()
        })
    }


    override fun onItemTap(email: Email) {
        if (!email.finished) {
            toastOnUiThread("Attachments are still being processed.", Toast.LENGTH_SHORT)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val attachments = AttachmentRepo.get().getAttachments(email.id)
            if (attachments.isNotEmpty()) {
                val emailId = attachments[0].emailId
                val validAttachments =
                    attachments.filter { allowedContentTypes.contains(it.contentType) }
                if (validAttachments.isEmpty()) {
                    toastOnUiThread("No PDF or image attachments.", Toast.LENGTH_SHORT)
                    return@launch
                }

                val validAndUploaded = validAttachments.filter { it.uploaded }
                if (validAndUploaded.isEmpty() || validAndUploaded.size != validAttachments.size) {
                    toastOnUiThread(
                        "Attachments are still uploading to sypht.",
                        Toast.LENGTH_SHORT
                    )
                    return@launch
                }

                val results = SyphtRepo.get().getFinalisedResults(emailId)
                if (results.isEmpty()) {
                    toastOnUiThread("Sypht is processing results.", Toast.LENGTH_SHORT)
                    return@launch
                }
                val display = mutableListOf<LinkedHashMap<String, String?>>()
                results.forEach {
                    display.add(generateDisplayString(it.result!!))
                }
                if (display.any { l -> l.values.any{ v -> !v.isNullOrBlank() && v != "\"null\"" } }) {
                    // toastOnUiThread(display, Toast.LENGTH_LONG)
                    (context as Activity).runOnUiThread {
                        MaterialAlertDialogBuilder(context)
                            .setTitle("Invoice details")
                            .setMessage(JSONArray(display.toString()).toString(3))
                            .show()
                    }
                } else {
                    toastOnUiThread("No invoice found.", Toast.LENGTH_SHORT)
                    return@launch
                }
            } else {
                // Since we save to Room when there is an attachment that can
                // be uploaded to Sypht, if we are here, that means
                // the email didn't have an image or PDF attachment.
                toastOnUiThread("No PDF or image attachment found.", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun generateDisplayString(resultString: String): LinkedHashMap<String, String?> {
        val display = LinkedHashMap(mapOfFields)
        JSONObject(resultString).takeIf { result ->
            result.getString("status") == "FINALISED"
        }?.let { it ->
            val fields = it.getJSONObject("results").getJSONArray("fields")
            for (x in 0 until fields.length()) {
                val field = fields.getJSONObject(x)
                field.getString("name").takeIf { name ->
                    displayFields.contains(name)
                }?.let { fieldName ->
                    var v = field.optString("value", "")
                    // Hacky way to figure out if the field is a dollar value
                    if (v.contains(".") && v.substring(v.indexOf(".")+1).length == 2) {
                        v = "$ $v"
                    }
                    display[fieldName] = "\"$v\""
                }
            }
            Log.d("SYPHT", display.toString())
        }
        return display
    }

    private fun toastOnUiThread(display: String, length: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, display, length).show()
        }
    }

    private fun toastOnUiThread(display: List<LinkedHashMap<String, String?>>, length: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, display.toString(), length).show()
        }
    }

    companion object {
        fun createInstance(): EmailsFragment {
            return EmailsFragment()
        }
    }
}