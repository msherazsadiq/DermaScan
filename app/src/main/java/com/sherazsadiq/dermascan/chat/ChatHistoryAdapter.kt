package com.sherazsadiq.dermascan.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.chat.ChatActivity
import java.text.SimpleDateFormat
import java.util.*

class ChatHistoryAdapter(private var threads: List<ChatHistoryActivity.ChatThread>) :
    RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>() {

    // Parser for stored timestamp (e.g., "2025-03-21 05:20 AM")
    private val parser = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
    // Format for the date portion (e.g., "21 March 2025")
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    // Format for the time portion (e.g., "05:20 AM")
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    class ChatHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatDateTextView: TextView = itemView.findViewById(R.id.chatDate)
        val chatTimeTextView: TextView = itemView.findViewById(R.id.chatTime)
        val gotoChatDetails: View = itemView.findViewById(R.id.gotoChatDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_history, parent, false)
        return ChatHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHistoryViewHolder, position: Int) {
        val chatThread = threads[position]
        try {
            val date = parser.parse(chatThread.lastMessage.timestamp)
            if (date != null) {
                holder.chatDateTextView.text = dateFormat.format(date)
                holder.chatTimeTextView.text = timeFormat.format(date)
            } else {
                holder.chatDateTextView.text = chatThread.lastMessage.timestamp
                holder.chatTimeTextView.text = ""
            }
        } catch (e: Exception) {
            holder.chatDateTextView.text = chatThread.lastMessage.timestamp
            holder.chatTimeTextView.text = ""
        }

        // Set click listener to start ChatActivity with known = "3" and pass the thread key
        holder.gotoChatDetails.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("known", "3")
            intent.putExtra("threadKey", chatThread.threadKey)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = threads.size

    fun updateData(newThreads: List<ChatHistoryActivity.ChatThread>) {
        threads = newThreads
        notifyDataSetChanged()
    }
}