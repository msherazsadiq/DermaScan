package com.sherazsadiq.dermascan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.manageprofile.CircleCropTransformation
import com.sherazsadiq.dermascan.manageprofile.EditProfileDoctorActivity
import com.sherazsadiq.dermascan.manageprofile.EditProfileUserActivity

class HomeActivity : AppCompatActivity() {
    private var originalHeight = 0
    private var maxHeight = 0
    private var currentHeight = 0 // Tracks the current height during dragging
    private var initialY = 0f

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var recyclerViewDoctors: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter

    private var currentUserData: Any? = null
    private var userType: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // ----------------- Navigation Drawer -----------------
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val profileViewButton = findViewById<ImageView>(R.id.ProfileViewButton)
        val profileViewButtonIcon = findViewById<ImageView>(R.id.ProfileViewButtonIcon)
        profileViewButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        val headerView = navigationView.getHeaderView(0)


        // Fetch initial user data
        fetchUserData()

        // ----------------- Navigation Drawer Menu -----------------
            headerView.findViewById<LinearLayout>(R.id.icon_profile).setOnClickListener {
            // Handle profile click
            when (userType) {
                "Patient" -> {
                    val intent = Intent(this, EditProfileUserActivity::class.java)
                    intent.putExtra("currentUser", currentUserData as User)
                    startActivity(intent)
                }
                "Doctor" -> {
                    val intent = Intent(this, EditProfileDoctorActivity::class.java)
                    intent.putExtra("currentUser", currentUserData as Doctor)
                    startActivity(intent)
                }
            }
        }
        headerView.findViewById<LinearLayout>(R.id.icon_settings).setOnClickListener {
            // Handle settings click
        }
        headerView.findViewById<LinearLayout>(R.id.icon_logout).setOnClickListener {
            // Handle logout click
        }


        // ----------------- Doctors RecyclerView -----------------
        recyclerViewDoctors = findViewById(R.id.recyclerViewDoctors)
        recyclerViewDoctors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val firebaseReadService = FirebaseReadService()
        firebaseReadService.fetchAllDoctors { doctors, error ->
            if (doctors != null) {
                doctorAdapter = DoctorAdapter(doctors)
                recyclerViewDoctors.adapter = doctorAdapter
            } else {
                // Handle error
            }
        }


        // ----------------- Scan Button -----------------
        val scanBtn = findViewById<LinearLayout>(R.id.scanButton)
        scanBtn.setOnClickListener {
            startActivity(Intent(this, ScanImageActivity::class.java))
        }



        // ----------------- Dragging Gesture for Drawer -----------------
        val scrollableline = findViewById<LinearLayout>(R.id.scrollableline)
        val scrollableLinearLayout = findViewById<LinearLayout>(R.id.scrollableLinearLayout)

        // Initialize dimensions
        scrollableLinearLayout.post {
            originalHeight = scrollableLinearLayout.height
            maxHeight = originalHeight + TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 150f, resources.displayMetrics
            ).toInt()
            currentHeight = originalHeight
        }

        // Touch listener for drag gestures
        scrollableline.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = (initialY - event.rawY).toInt()
                    adjustHeight(scrollableLinearLayout, deltaY)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Snap to nearest boundary (originalHeight or maxHeight) for better UX
                    snapHeight(scrollableLinearLayout)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        fetchUserData()
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
                    userType = if (user is User) "Patient" else "Doctor"
                    updateUI(user)
                } else {
                    if (error != null) {
                        Log.e("HomeActivity", "Error fetching user: $error")
                    } else {
                        Log.e("HomeActivity", "Unknown error occurred while fetching user.")
                    }
                }
            }
        } else {
            Log.e("HomeActivity", "No current user")
        }
    }

    // ----------------- Update UI -----------------

    private fun updateUI(user: Any) {
        val headerView = navigationView.getHeaderView(0)
        val navProfileImage = headerView.findViewById<ImageView>(R.id.profile_image)
        val navProfileName = headerView.findViewById<TextView>(R.id.profile_name)
        val navProfileEmail = headerView.findViewById<TextView>(R.id.profile_email)
        val navProfileImageIcon = headerView.findViewById<ImageView>(R.id.ProfileImageIcon)

        val profileViewButton = findViewById<ImageView>(R.id.ProfileViewButton)
        val profileViewButtonIcon = findViewById<ImageView>(R.id.ProfileViewButtonIcon)

        if (user is User) {
            updateProfileUI(
                name = user.Name,
                email = user.Email,
                profilePic = user.ProfilePic,
                navProfileImage = navProfileImage,
                navProfileName = navProfileName,
                navProfileEmail = navProfileEmail,
                navProfileImageIcon = navProfileImageIcon,
                profileViewButton = profileViewButton,
                profileViewButtonIcon = profileViewButtonIcon
            )
        } else if (user is Doctor) {
            updateProfileUI(
                name = user.Name,
                email = user.Email,
                profilePic = user.ProfilePic,
                navProfileImage = navProfileImage,
                navProfileName = navProfileName,
                navProfileEmail = navProfileEmail,
                navProfileImageIcon = navProfileImageIcon,
                profileViewButton = profileViewButton,
                profileViewButtonIcon = profileViewButtonIcon
            )
        }
    }

    /**
     * Update profile section UI
     */
    private fun updateProfileUI(
        name: String,
        email: String,
        profilePic: String?,
        navProfileImage: ImageView,
        navProfileName: TextView,
        navProfileEmail: TextView,
        navProfileImageIcon: ImageView,
        profileViewButton: ImageView,
        profileViewButtonIcon: ImageView
    ) {
        navProfileName.text = name
        navProfileEmail.text = email

        if (!profilePic.isNullOrEmpty()) {
            Glide.with(this)
                .load(profilePic)
                .transform(CircleCropTransformation())
                .into(navProfileImage)
            Glide.with(this)
                .load(profilePic)
                .transform(CircleCropTransformation())
                .into(profileViewButton)

            navProfileImageIcon.visibility = View.INVISIBLE
            profileViewButtonIcon.visibility = View.INVISIBLE
        } else {
            // Set visibility for default image icons
            navProfileImageIcon.visibility = View.VISIBLE
            profileViewButtonIcon.visibility = View.VISIBLE
        }
    }


    // ----------------- Dragging Gesture Helper Functions -----------------
    // Adjusts the height of the layout dynamically
    private fun adjustHeight(layout: LinearLayout, deltaY: Int) {
        val newHeight = (currentHeight + deltaY).coerceIn(originalHeight, maxHeight)
        if (newHeight != currentHeight) {
            val layoutParams = layout.layoutParams
            layoutParams.height = newHeight
            layout.layoutParams = layoutParams
            currentHeight = newHeight
        }
    }


    // ----------------- Dragging Gesture Helper Functions -----------------
    // Snaps the layout height to the nearest boundary (originalHeight or maxHeight)
    private fun snapHeight(layout: LinearLayout) {
        val snapTarget = if (currentHeight - originalHeight < (maxHeight - originalHeight) / 2) {
            originalHeight
        } else {
            maxHeight
        }
        val layoutParams = layout.layoutParams
        layoutParams.height = snapTarget
        layout.layoutParams = layoutParams
        currentHeight = snapTarget
    }
}