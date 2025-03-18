package com.sherazsadiq.dermascan.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.setStatusBarColor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ChatActivity : AppCompatActivity() {

    private lateinit var query: EditText
    private lateinit var sendButton: FrameLayout
    private lateinit var chatMessagesRecycler: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var apiUrl: String? = null // Store API URL here

    private lateinit var backButton: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        query = findViewById(R.id.queryEditText)
        sendButton = findViewById(R.id.sendPrompt)
        chatMessagesRecycler = findViewById(R.id.chatMessages)

        chatAdapter = ChatAdapter(mutableListOf())
        chatMessagesRecycler.layoutManager = LinearLayoutManager(this)
        chatMessagesRecycler.adapter = chatAdapter

        val firebaseService = FirebaseReadService()
        // Fetch API URL
        firebaseService.fetchApiUrl { url, error ->
            if (url != null) {
                apiUrl = url
                apiUrl += "/query"


            } else {
                // Handle error (e.g., show a Toast message)
                println("Error fetching API URL: $error")
            }
        }

        sendButton.setOnClickListener {
            val userQuery = query.text.toString().trim()
            if (userQuery.isNotEmpty() && apiUrl != null) {

                chatAdapter.addMessage(ChatMessage(userQuery, true))
                sendMessageToApi(userQuery, apiUrl!!)

                query.text.clear()

            }
        }
    }

    private fun sendMessageToApi(prompt: String, apiUrl: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Increase connection timeout
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Increase read timeout
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Increase write timeout
            .build()

        val json = JSONObject().apply {
            put("prompt", prompt)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatActivity", "API Request Failed", e)
                runOnUiThread {
                    chatAdapter.addMessage(ChatMessage("Error: Connection timeout. Please try again.", false))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string() ?: "{}"

                    if (!response.isSuccessful) {
                        Log.e("ChatActivity", "Unexpected API response: $responseBody")
                        runOnUiThread {
                            chatAdapter.addMessage(ChatMessage("Error: Unexpected response", false))
                        }
                        return
                    }

                    val botReply = try {
                        JSONObject(responseBody).optString("response", "No response from API")
                    } catch (e: Exception) {
                        Log.e("ChatActivity", "JSON Parsing Error", e)
                        "Error parsing response"
                    }

                    runOnUiThread {
                        chatAdapter.addMessage(ChatMessage(botReply, false))
                    }
                }
            }
        })
    }


}