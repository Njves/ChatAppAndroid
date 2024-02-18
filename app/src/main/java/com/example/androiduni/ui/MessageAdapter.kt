package com.example.androiduni.ui

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.androiduni.Client
import com.example.androiduni.PicassoNotSafe
import com.example.androiduni.R
import com.example.androiduni.UserProvider
import com.example.androiduni.message.Message
import com.example.androiduni.select.MessageDetails
import com.example.androiduni.select.ViewHolderWithDetails
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class MessageAdapter(private val context: Context, private var messagesList: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    lateinit var tracker: SelectionTracker<Message>
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

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) = Unit

    override fun onBindViewHolder(holder: MessageAdapter.MessageViewHolder, position: Int, payloads: List<Any>) {
        holder.setActivatedState(tracker.isSelected(messagesList[position]))
        if (SelectionTracker.SELECTION_CHANGED_MARKER !in payloads) {
            holder.bind(messagesList[position])
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    fun setData(list: MutableList<Message>) {
        this.messagesList = list
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ViewHolderWithDetails<Message> {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val grid: GridView = itemView.findViewById(R.id.gridImages)
        val tvDate: TextView = itemView.findViewById(R.id.tvTime)
        fun bind(message: Message) {

            tvSender.text = message.user.username
            tvMessage.text = message.text
            Picasso.get().isLoggingEnabled = true
            Log.d("MessageAdapter", message.attachments.toString())
            grid.adapter = ImageAdapter(context, message.attachments)
        }
        fun setActivatedState(isActivated: Boolean) {
            itemView.isActivated = isActivated
        }
        override fun getItemDetail(): ItemDetailsLookup.ItemDetails<Message> = MessageDetails(absoluteAdapterPosition, messagesList.getOrNull(absoluteAdapterPosition))
    }

}
