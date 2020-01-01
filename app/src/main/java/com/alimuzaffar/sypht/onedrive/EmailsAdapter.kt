package com.alimuzaffar.sypht.onedrive;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.graph.models.extensions.Message

class EmailsAdapter(private val myDataset: List<Message>, private val onTap: OnItemTap) :
        RecyclerView.Adapter<EmailsAdapter.EmailsViewHolder>(), View.OnClickListener {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class EmailsViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): EmailsAdapter.EmailsViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.email_view, parent, false) as TextView
        return EmailsViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: EmailsViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = myDataset[position].subject
        holder.textView.tag = myDataset[position]
        holder.textView.setOnClickListener(this)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    interface OnItemTap {
        fun onItemTap(message: Message)
    }

    override fun onClick(v: View?) {
        onTap.onItemTap(v?.tag as Message)
    }
}