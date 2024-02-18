package com.example.androiduni.message.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.Client
import com.example.androiduni.PicassoNotSafe
import com.example.androiduni.R
import com.example.androiduni.message.Attachment

class AttachmentAdapter(private val context: Context, private val attachments: MutableList<Attachment>) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_attach, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        Log.d("AttachmentAdapter", attachments[position].toString())
        holder.bind(attachments[position])
    }

    fun addAttachments(attachmentsList: List<Attachment>) {
        attachments.addAll(attachmentsList)
        Log.d("AttachmentAdapter", attachments.toString())
//        notifyItemRangeInserted(attachments.size - attachmentsList.size, attachments.size)
        notifyDataSetChanged()
    }

    inner class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivAttach)
        fun bind(attachment: Attachment) {
            Log.d("AttachmentAdapter", attachment.toString())
            PicassoNotSafe.get(context).load(Client.BASE_URL + attachment.link).into(imageView)
        }
    }
}