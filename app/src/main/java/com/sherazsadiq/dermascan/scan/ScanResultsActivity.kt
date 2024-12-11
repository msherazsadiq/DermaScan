package com.sherazsadiq.dermascan.scan

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.setStatusBarColor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

data class ApiResponse(
    val probabilities: Map<String, Double>
)

class ScanResultsActivity : AppCompatActivity() {

    private lateinit var scannedImage: ImageView
    private lateinit var firstDName: TextView
    private lateinit var firstDPercentage: TextView
    private lateinit var secondDName: TextView
    private lateinit var secondDPercentage: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setStatusBarColor(this)

        setContentView(R.layout.activity_scan_results)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scannedImage = findViewById(R.id.scannedImage)
        firstDName = findViewById(R.id.firstDiseaseName)
        firstDPercentage = findViewById(R.id.firstDiseasePercentage)
        secondDName = findViewById(R.id.secondDiseaseName)
        secondDPercentage = findViewById(R.id.secondDiseasePercentage)

        val imageUriString = intent.getStringExtra("imageUri")

        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            Glide.with(this)
                .load(imageUri)
                .into(scannedImage)

            uploadImage(imageUri)
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex =
                    it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            } /////////////////////////////////////////////////////////////////////////////////////// v
        }
        return path
    }

    private fun uploadImage(imageUri: Uri) {
        val url = "http://13.60.98.133:5000/predict"
        val client = OkHttpClient()

        val imagePath = getPathFromUri(imageUri)
        if (imagePath == null) {
            // Log error if file path cannot be resolved
            Log.e("UploadImage", "Unable to resolve file path from URI: $imageUri")
            return
        }

        Log.d("UploadImage", "Resolved file path: $imagePath")

        val file = File(imagePath)

        // Determine the MIME type of the file dynamically
        val mimeType = contentResolver.getType(imageUri) ?: "application/octet-stream"
        Log.d("UploadImage", "Determined MIME type: $mimeType")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",  // Field name expected by the backend
                file.name,  // File name
                file.asRequestBody(mimeType.toMediaTypeOrNull())  // File content with dynamically determined MIME type
            )
            .build()

        Log.d("UploadImage", "Request body built successfully")

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        Log.d("UploadImage", "Request created: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadImage", "Request failed: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("UploadImage", "Response received: ${response.code}")

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("UploadImage", "Response body: $responseBody")

                    if (responseBody != null) {
                        try {
                            val apiResponse = Gson().fromJson(responseBody, ApiResponse::class.java)
                            val sortedProbabilities = apiResponse.probabilities.entries.sortedByDescending { it.value }
                            runOnUiThread {
                                if (sortedProbabilities.isNotEmpty()) {
                                    Log.d("UploadImage", "First disease: ${sortedProbabilities[0].key} - ${sortedProbabilities[0].value}")
                                    firstDName.text = sortedProbabilities[0].key
                                    firstDPercentage.text = "${(sortedProbabilities[0].value * 100).toInt()}%"
                                }
                                if (sortedProbabilities.size > 1) {
                                    Log.d("UploadImage", "Second disease: ${sortedProbabilities[1].key} - ${sortedProbabilities[1].value}")
                                    secondDName.text = sortedProbabilities[1].key
                                    secondDPercentage.text = "${(sortedProbabilities[1].value * 100).toInt()}%"
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("UploadImage", "Error parsing response JSON: ${e.message}", e)
                        }
                    }
                } else {
                    Log.e("UploadImage", "Unsuccessful response: ${response.code} - ${response.message}")
                    runOnUiThread {
                        // Handle unsuccessful response
                    }
                }
            }
        })
    }


}
