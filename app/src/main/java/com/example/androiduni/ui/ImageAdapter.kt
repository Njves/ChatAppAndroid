package com.example.androiduni.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.Client
import com.example.androiduni.ImageDetailActivity
import com.example.androiduni.PicassoNotSafe
import com.example.androiduni.R
import com.example.androiduni.message.Attachment
import com.google.gson.Gson


class ImageAdapter(private val context: Context, private val imagesLinks: List<Attachment>) : RecyclerView.Adapter<ImageAdapter.AttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_attach, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imagesLinks.size
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(imagesLinks[position])
    }

    inner class AttachmentViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivAttach)
        fun bind(attachment: Attachment) {
            PicassoNotSafe(context).get().load(Client.BASE_URL + attachment.link).tag(context).error(R.drawable.ic_error).fit().placeholder(R.drawable.placeholder).into(imageView)
            itemView.setOnClickListener {
                if(imagesLinks.isEmpty()) {
                    return@setOnClickListener
                }
                val gson = Gson()
                val intent = Intent(context, ImageDetailActivity::class.java)
                intent.putExtra("images",gson.toJson(imagesLinks))
                context.startActivity(intent)
            }
        }
    }
}