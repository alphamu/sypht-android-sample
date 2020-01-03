package com.alimuzaffar.sypht.onedrive.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class EmailsFragment : Fragment(),
    EmailsAdapter.OnItemTap {
    val displayFields = arrayOf(
        "invoice.tax",
        "invoice.gst",
        "invoice.total",
        "invoice.amountDue",
        "invoice.dueDate"
    )

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
        EmailRepo.get().getEmails().observe(this, Observer {
            adapter.myDataset = it
            adapter.notifyDataSetChanged()
        })
    }


    override fun onItemTap(email: Email) {
        if (email.processing) {
            Toast.makeText(context, "Attachments are still being processed.", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val attachments = AttachmentRepo.get().getAttachments(email.id)
            if (attachments.isNotEmpty()) {
                val emailId = attachments[0].emailId
                val validAttachments = attachments.filter { allowedContentTypes.contains(it.contentType) }
                if (validAttachments.isEmpty()) {
                    Toast.makeText(context, "No PDF or image attachments.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val validAndUploaded = validAttachments.filter { it.uploaded }
                if (validAndUploaded.isEmpty() || validAndUploaded.size != validAttachments.size) {
                    Toast.makeText(context, "Attachments are still uploading to sypht.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val results = SyphtRepo.get().getFinalisedResults(emailId)
                if (results.isEmpty()) {
                    Toast.makeText(context, "Sypht is processing results.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                var display = ""
                results.forEach {
                    if (display.isNotEmpty()) {
                        display += "\n\n"
                    }
                    display += generateDisplayString(it.result!!)
                }

            } else {
                // Since we only request emails with extensions
                // If there is nothing here, we probably haven't download the attachments yet.
                Toast.makeText(context, "Attachments are still downloading for this email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateDisplayString(resultString: String): String {
        var display = ""
        JSONObject(resultString).takeIf { result ->
            result.getString("status") == "FINALISED"
        }?.let { it ->
            val fields = it.getJSONObject("results").getJSONArray("fields")
            for (x in 0 until fields.length()) {
                val field = fields.getJSONObject(x)
                field.getString("name").takeIf { name ->
                    displayFields.contains(name)
                }?.let { fieldName ->
                    display += "$fieldName ${field.getString("value")}\n"
                }
            }
            Log.d("SYPHT", display)
        }
        return display
    }

    companion object {
        fun createInstance(): EmailsFragment {
            return EmailsFragment()
        }
    }
}