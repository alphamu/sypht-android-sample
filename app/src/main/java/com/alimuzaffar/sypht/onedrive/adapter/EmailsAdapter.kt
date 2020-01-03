package com.alimuzaffar.sypht.onedrive.adapter;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alimuzaffar.sypht.onedrive.R
import com.alimuzaffar.sypht.onedrive.entity.Email

class EmailsAdapter(var myDataset: List<Email>, private val onTap: OnItemTap) :
        RecyclerView.Adapter<EmailsAdapter.EmailsViewHolder>(), View.OnClickListener {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class EmailsViewHolder(val layout: ViewGroup) : RecyclerView.ViewHolder(layout) {
        val subject = layout.findViewById<TextView>(R.id.subject)
        val from = layout.findViewById<TextView>(R.id.from)
        val received = layout.findViewById<TextView>(R.id.received)
        val processing = layout.findViewById<TextView>(R.id.processing)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): EmailsViewHolder {
        // create a new view
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.email_view, parent, false) as ViewGroup
        return EmailsViewHolder(
            layout
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: EmailsViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val data = myDataset[position]
        holder.subject.text = data.subject

        holder.from.text = data.from
        holder.received.text = data.received
        holder.processing.visibility = if (data.finished) View.INVISIBLE else View.VISIBLE

        holder.layout.tag = data
        holder.layout.setOnClickListener(this)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    interface OnItemTap {
        fun onItemTap(email: Email)
    }

    override fun onClick(v: View?) {
        onTap.onItemTap(v?.tag as Email)
    }
}