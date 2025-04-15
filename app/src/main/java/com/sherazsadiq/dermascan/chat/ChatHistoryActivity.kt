package com.sherazsadiq.dermascan.chat

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.setStatusBarColor
import java.text.SimpleDateFormat
import java.util.*

class ChatHistoryActivity : AppCompatActivity() {

    private var currentUserData: Any? = null
    private var userType: String? = null

    private lateinit var adapter: ChatHistoryAdapter
    private val firebaseReadService = FirebaseReadService()
    private lateinit var chatHistoryRecyclerView: RecyclerView

    // Data class representing a conversation thread and its latest chat message
    data class ChatThread(
        val threadKey: String,
        val lastMessage: ChatData
    )

    // Timestamp format that matches how your chat messages store their timestamp (for example: "2025-03-21 05:20 AM")
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(this)
        setContentView(R.layout.activity_chat_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button finishes the activity
        val backButton = findViewById<FrameLayout>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        chatHistoryRecyclerView = findViewById(R.id.chatHistoryRecyclerView)
        chatHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatHistoryAdapter(emptyList())
        chatHistoryRecyclerView.adapter = adapter

        fetchUserData()
    }

    private fun fetchUserData() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            firebaseReadService.fetchCurrentUser(uid) { user, error ->
                if (user != null) {
                    currentUserData = user
                    // Assume user is a Patient; adjust if needed.
                    userType = if (user is User) "Patients" else "Doctors"

                    val userUID = when (val userData = currentUserData) {
                        is User -> userData.UID
                        is Doctor -> userData.UID
                        else -> null
                    }

                    if (userUID != null && userType != null) {
                        fetchChatHistory(userUID, userType!!)
                    } else {
                        Log.e("ChatHistoryActivity", "User UID or userType is null")
                    }
                } else {
                    Log.e("ChatHistoryActivity", "Error fetching user: $error")
                }
            }
        } else {
            Log.e("ChatHistoryActivity", "No current user")
        }
    }

    private fun fetchChatHistory(uid: String, userType: String) {
        val chatRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(userType)
            .child(uid)
            .child("Chats")

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatThreadList = mutableListOf<ChatThread>()
                // Each child under "Chats" represents a conversation thread
                for (threadSnapshot in snapshot.children) {
                    val threadKey = threadSnapshot.key ?: continue
                    var latestMessage: ChatData? = null
                    // Iterate through messages in this conversation to find the most recent one
                    for (messageSnapshot in threadSnapshot.children) {
                        val msg = messageSnapshot.getValue(ChatData::class.java)
                        if (msg != null) {
                            try {
                                val msgTime = timestampFormat.parse(msg.timestamp)?.time ?: 0L
                                val currentLatestTime = latestMessage?.let {
                                    timestampFormat.parse(it.timestamp)?.time ?: 0L
                                } ?: 0L
                                if (latestMessage == null || msgTime > currentLatestTime) {
                                    latestMessage = msg
                                }
                            } catch (e: Exception) {
                                Log.e("ChatHistoryActivity", "Error parsing timestamp: ${msg.timestamp}", e)
                            }
                        }
                    }
                    if (latestMessage != null) {
                        chatThreadList.add(ChatThread(threadKey, latestMessage))
                    }
                }
                // Sort threads so that the conversation with the most recent message is first
                chatThreadList.sortByDescending { thread ->
                    try {
                        timestampFormat.parse(thread.lastMessage.timestamp)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
                adapter.updateData(chatThreadList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatHistoryActivity", "Error fetching chats: ${error.message}")
            }
        })
    }
}