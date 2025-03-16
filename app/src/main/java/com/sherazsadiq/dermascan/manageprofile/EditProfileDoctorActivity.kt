package com.sherazsadiq.dermascan.manageprofile

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseManager
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.setStatusBarColor
import java.util.Calendar

class EditProfileDoctorActivity : AppCompatActivity() {
    private lateinit var currentUser: Doctor
    private var selectedImageUri: Uri? = null
    private val firebaseManager = FirebaseManager()
    private val firebaseWriteService = FirebaseWriteService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setStatusBarColor(this)

        setContentView(R.layout.activity_edit_profile_doctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUser = intent.getSerializableExtra("currentUser") as Doctor

        val dateOfBirthTextView = findViewById<TextView>(R.id.dateOfBirthTextView)
        val genderSpinner = findViewById<Spinner>(R.id.genderSpinner)
        val addressEditText = findViewById<EditText>(R.id.addressEditText)
        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val experienceEditText = findViewById<EditText>(R.id.ExperienceEditText)
        val specializationEditText = findViewById<EditText>(R.id.specializationEditText)






        dateOfBirthTextView.setOnClickListener {
            showDatePickerDialog()
        }

        val genderOptions = arrayOf("Select", "Male", "Female", "Prefer not to say")
        val adapter = ArrayAdapter(this, R.layout.gender_spinner_selected_item, genderOptions)
        adapter.setDropDownViewResource(R.layout.gender_spinner_item)
        genderSpinner.adapter = adapter

        updateUI(currentUser)


        val editProfileButton = findViewById<FrameLayout>(R.id.EditProfileImage)
        editProfileButton.setOnClickListener {
            openGallery()
        }

        val cancelBtn = findViewById<TextView>(R.id.cancelButton)
        cancelBtn.setOnClickListener {
            finish()
        }

        val saveBtn = findViewById<TextView>(R.id.saveButton)
        saveBtn.setOnClickListener {
            val dob = dateOfBirthTextView.text.toString()
            val gender = genderSpinner.selectedItem.toString()
            val address = addressEditText.text.toString()
            val phoneNo = phoneEditText.text.toString()
            val experience = experienceEditText.text.toString()
            val specialization = specializationEditText.text.toString()


            // Check if required fields are filled
            if (dob == "Select Date of Birth" || gender == "Select" || address.isEmpty() || phoneNo.isEmpty() || experience.isEmpty() || specialization.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show progress dialog for saving user data
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Saving data...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            // Update user data first
            updateUserData(dob, gender, address, phoneNo, experience, specialization) { userDataUpdated ->
                progressDialog.dismiss()  // Dismiss the progress dialog after saving data

                if (userDataUpdated) {
                    // Show a success message
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()  // Finish the activity after saving data
                } else {
                    // Show an error message
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            selectedImageUri?.let { imageUri ->
                // Show progress dialog when starting the image upload
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Uploading image...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Upload image and dismiss progress dialog when done
                uploadImage(imageUri) { imageUploaded ->
                    progressDialog.dismiss()  // Dismiss progress dialog after upload is completed

                    if (imageUploaded) {
                        Glide.with(this)
                            .load(imageUri)
                            .transform(CircleCropTransformation())
                            .into(findViewById(R.id.profile_image))

                        // Hide profile image icon after successful upload
                        findViewById<ImageView>(R.id.profile_image_icon).visibility = View.INVISIBLE
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun updateUserData(dob: String, gender: String, address: String, phoneNo: String, experience: String, specialization: String, callback: (Boolean) -> Unit) {
        val userData = mapOf(
            "dob" to dob,
            "gender" to gender,
            "address" to address,
            "phone" to phoneNo,
            "experience" to experience,
            "specialization" to specialization,
            "isProfileComplete" to true
        )
        firebaseWriteService.updateDoctorData(currentUser.UID, userData) { success ->
            callback(success)  // Pass success/failure to the callback
        }
    }

    private fun uploadImage(imageUri: Uri, callback: (Boolean) -> Unit) {
        firebaseManager.uploadImageToStorage(imageUri, currentUser.UID, "Doctors") { uri ->
            if (uri != null) {
                currentUser.ProfilePic = uri
                callback(true)  // Image upload successful
            } else {
                callback(false)  // Image upload failed
            }
        }
    }




    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, R.style.CustomDatePickerDialog, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            findViewById<TextView>(R.id.dateOfBirthTextView).text = selectedDate
        }, year, month, day)

        datePickerDialog.setOnShowListener {
            try {
                val datePicker = datePickerDialog.datePicker
                val ll = datePicker.getChildAt(0) as LinearLayout
                val ll2 = ll.getChildAt(0) as LinearLayout
                val header = ll2.getChildAt(0) as TextView
                header.setTextColor(resources.getColor(R.color.DarkBlue))
                header.setBackgroundColor(resources.getColor(R.color.HalfWhite))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        datePickerDialog.show()
    }


    // ------------------- Update UI -------------------
    private fun updateUI(user: Doctor) {
        val profileName = findViewById<TextView>(R.id.profile_name)
        val profileEmail = findViewById<TextView>(R.id.profile_email)
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val profileImageIcon = findViewById<ImageView>(R.id.profile_image_icon)
        val dateOfBirthTextView = findViewById<TextView>(R.id.dateOfBirthTextView)
        val genderSpinner = findViewById<Spinner>(R.id.genderSpinner)
        val addressEditText = findViewById<EditText>(R.id.addressEditText)
        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val experienceEditText = findViewById<EditText>(R.id.ExperienceEditText)
        val specializationEditText = findViewById<EditText>(R.id.specializationEditText)

        // Update Profile Name and Email
        profileName.text = user.Name
        profileEmail.text = user.Email

        // Update Profile Image
        if (!user.ProfilePic.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.ProfilePic)
                .transform(CircleCropTransformation())
                .into(profileImage)
            profileImageIcon.visibility = View.INVISIBLE
        } else {
            // Handle case where there is no profile picture
            profileImageIcon.visibility = View.VISIBLE
        }

        // Update Date of Birth
        if (!user.DOB.isNullOrEmpty()) {
            dateOfBirthTextView.text = user.DOB
        }


        // Gender Spinner Update
        val genderOptions = arrayOf("Select", "Male", "Female", "Prefer not to say")
        val adapter = ArrayAdapter(this, R.layout.gender_spinner_selected_item, genderOptions)
        adapter.setDropDownViewResource(R.layout.gender_spinner_item)
        genderSpinner.adapter = adapter

        // Set the gender selection based on user.Gender
        when (user.Gender) {
            "Male" -> genderSpinner.setSelection(1)
            "Female" -> genderSpinner.setSelection(2)
            "Prefer not to say" -> genderSpinner.setSelection(3)
            else -> genderSpinner.setSelection(0)  // Default to "Select" if gender is null or doesn't match
        }



        // Update Address and Phone Number
        if (!user.Address.isNullOrEmpty()) {
            addressEditText.setText(user.Address)
        }
        if (!user.Phone.isNullOrEmpty()) {
            phoneEditText.setText(user.Phone)
        }
        if (!user.Experience.isNullOrEmpty()) {
            experienceEditText.setText(user.Experience)
        }
        if (!user.Specialization.isNullOrEmpty()) {
            specializationEditText.setText(user.Specialization)
        }
    }
}