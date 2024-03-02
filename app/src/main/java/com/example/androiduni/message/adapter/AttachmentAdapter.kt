package com.example.androiduni.message.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.AttachmentViewModel
import com.example.androiduni.Client
import com.example.androiduni.PicassoNotSafe
import com.example.androiduni.R
import com.example.androiduni.message.Attachment
import com.squareup.picasso.Picasso

class AttachmentAdapter(private val context: Context, var attachments: MutableList<Attachment>, private val attachmentViewModel: AttachmentViewModel) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_attach_with_badge, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        Log.d("AttachmentAdapter", attachments[position].toString())
        holder.bind(attachments[position])
    }

    fun clearAttachments() {
        attachments.clear()
    }

    inner class AttachmentViewHolder(itemView: View, ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivAttach)
        private val iconRemove: ImageView = itemView.findViewById(R.id.removeIcon)
        fun bind(attachment: Attachment) {
            iconRemove.setOnClickListener {
                attachmentViewModel.removeAttachment(attachment)
            }
            PicassoNotSafe(context).get().load(Client.BASE_URL + attachment.link).tag(context).fit().placeholder(R.drawable.placeholder).into(imageView)
//
        }
    }



}