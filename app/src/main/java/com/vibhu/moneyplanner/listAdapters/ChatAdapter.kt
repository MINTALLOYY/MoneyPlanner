package com.vibhu.moneyplanner.listAdapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.models.ChatMessage

class ChatAdapter(
    private val context: Context,
    private var messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 0) {
            R.layout.item_user_chat
        } else {
            R.layout.item_bot_chat
        }
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.message
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 0 else 1
    }

    fun getMessages(): List<ChatMessage> {
        return messages
    }

    fun updateMessages(newMessages: List<ChatMessage>){
        messages = newMessages
        Log.d("ChatAdapter", "Updating messages: ${messages}}")
        notifyDataSetChanged()
    }
}