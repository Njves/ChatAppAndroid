package com.example.androiduni.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.Client
import com.example.androiduni.PicassoNotSafe
import com.example.androiduni.R
import com.example.androiduni.UserProvider
import com.example.androiduni.message.Message
import com.squareup.picasso.Picasso

class MessageAdapter(private val context: Context, private val messagesList: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageAdapter.MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        var view = inflater.inflate(R.layout.item_message, parent, false)
        if(viewType == 1)
            view = inflater.inflate(R.layout.item_message_sender, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        if(messagesList[position].user.id == UserProvider.user?.id)
            return 1
        return 0
    }

    override fun onBindViewHolder(holder: MessageAdapter.MessageViewHolder, position: Int) {
        holder.bind(messagesList[position])
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(message: Message) {
            val tvSender = itemView.findViewById<TextView>(R.id.tvSender)
            val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
            val ivAttach = itemView.findViewById<ImageView>(R.id.ivAttach)
            val tvDate = itemView.findViewById<TextView>(R.id.tvTime)
            tvSender.text = message.user.username
            tvMessage.text = message.text
            Picasso.get().isLoggingEnabled = true
            Log.d("MessageAdapter", message.attachments.toString())
            if(message.attachments.isNotEmpty()) {
                ivAttach.visibility = VISIBLE
                PicassoNotSafe.get(context).load(Client.BASE_URL + message.attachments[0].link).into(ivAttach)
            }
//            tvDate.text = message.date.toString()
        }
    }
}
