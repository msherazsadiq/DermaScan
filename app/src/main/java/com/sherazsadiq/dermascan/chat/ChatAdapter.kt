package com.sherazsadiq.dermascan.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sherazsadiq.dermascan.R
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        // Inflate the layout resource based on the view type
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = messages[position]
        // Set the text, trimming any extra whitespace
        holder.messageTextView.text = chatMessage.message.trim()
    }

    override fun getItemViewType(position: Int): Int {
        // Choose the layout resource based on the message content and sender
        val message = messages[position].message
        return when {
            message == "Error: Connection timeout. Please try again." ||
                    message == "Error: Unexpected response" -> R.layout.item_error_response
            messages[position].user -> R.layout.item_query
            else -> R.layout.item_response
        }
    }

    override fun getItemCount(): Int = messages.size

    // Adds a single message and notifies the adapter
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // Replaces the current list of messages with new ones and refreshes the UI
    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}