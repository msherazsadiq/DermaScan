package com.sherazsadiq.dermascan.chat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.setStatusBarColor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChatActivity : AppCompatActivity() {

    private var currentUserData: Any? = null
    private var userType: String? = null

    private var isMessagesUploaded = false

    private lateinit var query: EditText
    private lateinit var sendButton: FrameLayout
    private lateinit var chatMessagesRecycler: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var apiUrl: String? = null // Store API URL here

    private lateinit var known: String
    private lateinit var scannedResultText: String

    private lateinit var backButton: LinearLayout
    private lateinit var timestamp: String

    // Mutable list to store chat context with a fixed size of 30
    private var contextList = mutableListOf<ChatMessage>()
    private var allChatList = mutableListOf<ChatMessage>()

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
            uploadAllMessagesToDatabase()
            onBackPressed()
        }

        fetchUserData()

        query = findViewById(R.id.queryEditText)
        sendButton = findViewById(R.id.sendPrompt)
        chatMessagesRecycler = findViewById(R.id.chatMessages)

        chatAdapter = ChatAdapter(mutableListOf())
        chatMessagesRecycler.layoutManager = LinearLayoutManager(this)
        chatMessagesRecycler.adapter = chatAdapter


        // Initialize the context list with the hardcoded message
        known = intent.getStringExtra("known") ?: ""
        val baseContext = when (known) {
            "0" -> """
                Below is a conversation between a patient and a medical assistant named Dermascan. Dermascan's role is to gather detailed information about the patient's symptoms and medical history solely by asking clarifying questions. Do not provide any direct analysis, advice, or treatment recommendations until sufficient context has been gathered. If during the conversation Dermascan becomes moderately confident that a skin disease might be present, simply mention that possibility and ask targeted follow-up questions to confirm the suspicion. Please begin by welcoming the patient and asking an open-ended clarifying question about their symptoms.    """.trimIndent().replace("\n", " ")
            "1" -> """
                Below is a conversation between a patient and a medical assistant named Dermascan. Dermascan's role is to gather detailed information about the patient's symptoms and medical history solely by asking clarifying questions. Do not provide any direct analysis, advice, or treatment recommendations until sufficient context has been gathered. If during the conversation Dermascan becomes moderately confident that a skin disease might be present, simply mention that possibility and ask targeted follow-up questions to confirm the suspicion. Please begin by welcoming the patient and asking an open-ended clarifying question about their symptoms.    """.trimIndent().replace("\n", " ")
            else -> ""
        }



        // Add the hardcoded message as the first item in the context list
        contextList.add(ChatMessage(baseContext, true))
        if (known == "0"){

            val welcomeMsg = "Hi there! I am so glad you are here today! Can you tell me what brought you in?"
            chatAdapter.addMessage(ChatMessage(welcomeMsg, false))
            addBotMessage(welcomeMsg)
            timestamp = getCurrentTimestamp()
            allChatList.add(ChatMessage(welcomeMsg, false, timestamp))

            geturl { fetchedUrl ->
                if (fetchedUrl != null) {
                    apiUrl = fetchedUrl

                    Log.e("ChatActivity", "API URL: $fetchedUrl")


                        //addUserMessage(scannedResultText)
                        //sendMessageToApi(fetchedUrl)


                    //Toast.makeText(this, "from home", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to fetch URL", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (known == "1") {
            geturl { fetchedUrl ->
                if (fetchedUrl != null) {
                    apiUrl = fetchedUrl
                    scannedResultText = intent.getStringExtra("scanned_result") ?: ""

                    Log.e("ChatActivity", "Scanned Result: $scannedResultText")
                    Log.e("ChatActivity", "API URL: $fetchedUrl")

                    if (scannedResultText.isNotEmpty()) {
                        addUserMessage(scannedResultText)
                        sendMessageToApi(fetchedUrl)
                    }

                    //Toast.makeText(this, "from scan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to fetch URL", Toast.LENGTH_SHORT).show()
                }
            }
        }

            sendButton.setOnClickListener {
            val userQuery = query.text.toString().trim()

            geturl { fetchedUrl ->
                if (fetchedUrl != null) {
                    apiUrl = fetchedUrl
                    if (userQuery.isNotEmpty() && apiUrl != null) {

                        // Add user message to the context list and adapter
                        addUserMessage(userQuery)
                        chatAdapter.addMessage(ChatMessage(userQuery, true))
                        // Send the message to the API
                        sendMessageToApi(apiUrl!!)
                        timestamp = getCurrentTimestamp()
                        allChatList.add(ChatMessage(userQuery, true, timestamp))

                        query.text.clear()
                    }
                }
            }
        }

    }

    private fun addUserMessage(message: String) {
        // Add user message to the context list and adapter
        val userMessage = ChatMessage(message, true)
        if (contextList.size >= 30) {
            // Remove the second item (index 1) if the list is full
            contextList.removeAt(1)
        }
        contextList.add(userMessage)
    }

    private fun addBotMessage(message: String) {
        // Add bot message to the context list and adapter
        val botMessage = ChatMessage(message, false)
        if (contextList.size >= 30) {
            // Remove the second item (index 1) if the list is full
            contextList.removeAt(1)
        }
        contextList.add(botMessage)
    }

    private fun flattenContextList(): String {
        return buildString {
            for ((index, message) in contextList.withIndex()) {
                if (index == 0) {
                    append(message.message)
                } else {
                    append(if (message.isUser) "user: " else "bot: ")
                    append("\n")
                    append(message.message)
                }
                append("\n")
            }
        }.trim()
    }

    private fun geturl(callback: (String?) -> Unit) {
        val firebaseService = FirebaseReadService()
        firebaseService.fetchApiUrl { url, error ->
            if (url != null) {
                val fullUrl = "$url/query"
                Log.e("ChatActivity", "API URL: $fullUrl")
                callback(fullUrl)
            } else {
                println("Error fetching API URL: $error")
                callback(null)
            }
        }
    }

    private fun sendMessageToApi(apiUrl: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        // Flatten the context list into the required string format
        val flattenedContext = flattenContextList()

        val json = JSONObject().apply {
            put("prompt", flattenedContext)
        }

        // chatAdapter.addMessage(ChatMessage(flattenedContext, true))


        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatActivity", "API Request Failed", e)
                runOnUiThread {
                    addBotMessage("Error: Connection timeout. Please try again.")
                    timestamp = getCurrentTimestamp()
                    allChatList.add(ChatMessage("Error: Connection timeout. Please try again.", false, timestamp))
                    chatAdapter.addMessage(ChatMessage("Error: Connection timeout. Please try again.", false))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string() ?: "{}"

                    if (!response.isSuccessful) {
                        Log.e("ChatActivity", "Unexpected API response: $responseBody")
                        runOnUiThread {
                            addBotMessage("Error: Unexpected response")
                            allChatList.add(ChatMessage("Error: Unexpected response", false))
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
                        addBotMessage(botReply)
                        chatAdapter.addMessage(ChatMessage(botReply, false))
                        timestamp = getCurrentTimestamp()
                        allChatList.add(ChatMessage(botReply, false, timestamp))
                    }
                }
            }
        })
    }

    fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        uploadAllMessagesToDatabase()  // Upload all messages to database
    }

    override fun onPause() {
        super.onPause()
        uploadAllMessagesToDatabase()  // Upload all messages to database
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadAllMessagesToDatabase()  // Upload all messages to database
    }

    private fun uploadAllMessagesToDatabase() {

        if (isMessagesUploaded) {
            return
        }

        val firebaseReadService = FirebaseReadService()
        val firebaseWriteService = FirebaseWriteService()

        val uid = firebaseReadService.getCurrentUserUid()
        if (uid == null) {
            Toast.makeText(this, "Failed to get user id", Toast.LENGTH_SHORT).show()
            return
        } else {
            val timestamp = getCurrentTimestamp()

            if((userType == "Patients" || userType == "Doctors") && userType != null && allChatList.isNotEmpty()){


                firebaseWriteService.uploadChatMessages(
                    timestamp,
                    uid,
                    userType!!,
                    allChatList
                ) { success ->
                    if (success) {
                        Log.d("ChatActivity", "All messages uploaded successfully")
                    } else {
                        Log.e("ChatActivity", "Error uploading messages")
                    }
                }
            }
            else{
                Log.e("ChatActivity", "User type not recognized")
            }
        }

        isMessagesUploaded = true
    }


    // ----------------- Fetch User Data -----------------
    private fun fetchUserData() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val firebaseReadService = FirebaseReadService()

            firebaseReadService.fetchCurrentUser(uid) { user, error ->
                if (user != null) {
                    currentUserData = user
                    userType = if (user is User) "Patients" else "Doctors"

                } else {
                    if (error != null) {
                        Log.e("ScanResultsActivity", "Error fetching user: $error")
                    } else {
                        Log.e("ScanResultsActivity", "Unknown error occurred while fetching user.")
                    }
                }
            }
        } else {
            Log.e("ScanResultsActivity", "No current user")
        }
    }
}