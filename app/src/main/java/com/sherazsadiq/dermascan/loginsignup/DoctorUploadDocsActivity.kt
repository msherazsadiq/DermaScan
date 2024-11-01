package com.sherazsadiq.dermascan.loginsignup

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.setStatusBarColor
import android.provider.DocumentsContract
import com.google.android.gms.tasks.Tasks
import com.sherazsadiq.dermascan.firebase.FirebaseManager
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import java.text.SimpleDateFormat
import java.util.Date


class DoctorUploadDocsActivity : AppCompatActivity() {
    private val REQUEST_CODE_PICK_DOCUMENT = 1
    private var selectedDocumentType: String? = null
    private lateinit var doctor: Doctor
    private val firebaseWriteService = FirebaseWriteService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_upload_docs)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        doctor = intent.getSerializableExtra("doctor") as? Doctor ?: run {
            Toast.makeText(this, "Doctor data is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val date : Date = Date()
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        val currentDate = sdf.format(date)
        doctor.CreatedAt = currentDate


        setupDocumentUpload(R.id.medicalLicenseLinearLayout, "MedicalLicense")
        setupDocumentUpload(R.id.degreeCertificateLinearLayout, "DegreeCertificate")
        setupDocumentUpload(R.id.uploadGovernmentIdLinearLayout, "GovernmentID")

        findViewById<TextView>(R.id.signUpButton).setOnClickListener {
            if (doctor.MedicalLicense.isEmpty() || doctor.DegreeCertificate.isEmpty() || doctor.GovernmentID.isEmpty()) {
                Toast.makeText(this, "Please upload all required documents", Toast.LENGTH_SHORT).show()
            } else {
                // Show progress dialog
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Uploading documents and saving data...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                val firebaseManager = FirebaseManager()
                val uploadTasks = listOf(
                    firebaseManager.uploadFile(doctor.MedicalLicense, "MedicalLicense", doctor.UID),
                    firebaseManager.uploadFile(doctor.DegreeCertificate, "DegreeCertificate", doctor.UID),
                    firebaseManager.uploadFile(doctor.GovernmentID, "GovernmentID", doctor.UID)
                )

                // Wait for all uploads to complete
                Tasks.whenAllComplete(uploadTasks).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Store the doctor object in the database
                        firebaseWriteService.saveDoctorData(doctor) { saveSuccess ->
                            progressDialog.dismiss()
                            if (saveSuccess) {
                                // Proceed to the next activity

                                val intent = Intent(this, RegisterOptionsActivity::class.java)
                                startActivity(intent)
                                finish()

                            } else {
                                Toast.makeText(this, "Failed to save doctor data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to upload documents", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupDocumentUpload(layoutId: Int, documentType: String) {
        findViewById<LinearLayout>(layoutId).setOnClickListener {
            selectedDocumentType = documentType
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, REQUEST_CODE_PICK_DOCUMENT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_DOCUMENT && resultCode == RESULT_OK) {
            data?.data?.also { uri ->
                when (selectedDocumentType) {
                    "MedicalLicense" -> updateDocumentUI(R.id.uploadMedicalLicenseImageView, R.id.uploadedMedicalLicenseImageView, R.id.uploadMedicalLicenseTextView, uri, "MedicalLicense")
                    "DegreeCertificate" -> updateDocumentUI(R.id.uploadDegreeCertificateImageView, R.id.uploadedDegreeCertificateImageView, R.id.uploadDegreeCertificateTextView, uri, "DegreeCertificate")
                    "GovernmentID" -> updateDocumentUI(R.id.uploadGovernmentIdImageView, R.id.uploadedGovernmentIdImageView, R.id.uploadGovernmentIdTextView, uri, "GovernmentID")
                }
            }
        }
    }

    private fun updateDocumentUI(imageViewId_upload: Int, imageViewId_uploaded: Int, textViewId: Int, uri: Uri, documentType: String) {
        val imageView_upload = findViewById<ImageView>(imageViewId_upload)
        val imageView_uploaded = findViewById<ImageView>(imageViewId_uploaded)
        val textView = findViewById<TextView>(textViewId)

        // set imageview visibility to invisible
        imageView_upload.visibility = ImageView.INVISIBLE

        // set imageview visibility to visible
        imageView_uploaded.visibility = ImageView.VISIBLE

        // Extract the file name from the URI
        val fileName = getFileName(uri)
        textView.text = fileName

        // Update the doctor object with the URI
        when (documentType) {
            "MedicalLicense" -> doctor.MedicalLicense = uri.toString()
            "DegreeCertificate" -> doctor.DegreeCertificate = uri.toString()
            "GovernmentID" -> doctor.GovernmentID = uri.toString()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }
}