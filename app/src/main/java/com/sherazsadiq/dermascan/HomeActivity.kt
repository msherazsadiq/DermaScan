package com.sherazsadiq.dermascan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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
import com.sherazsadiq.dermascan.chat.ChatActivity
import com.sherazsadiq.dermascan.chat.ChatHistoryActivity
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.loginsignup.SignInActivity
import com.sherazsadiq.dermascan.manageprofile.CircleCropTransformation
import com.sherazsadiq.dermascan.manageprofile.EditProfileDoctorActivity
import com.sherazsadiq.dermascan.manageprofile.EditProfileUserActivity
import com.sherazsadiq.dermascan.manageprofile.MapActivity
import com.sherazsadiq.dermascan.scan.ScanHistoryActivity
import com.sherazsadiq.dermascan.scan.ScanImageActivity


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
        headerView.findViewById<LinearLayout>(R.id.hospitalLocation).setOnClickListener {
            // Handle hospital location click
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

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
        headerView.findViewById<LinearLayout>(R.id.icon_scanHistory).setOnClickListener {
            val intent = Intent(this, ScanHistoryActivity::class.java)
            startActivity(intent)
        }
        headerView.findViewById<LinearLayout>(R.id.icon_settings).setOnClickListener {
            // Handle settings click
        }
        headerView.findViewById<LinearLayout>(R.id.icon_feedback).setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            intent.putExtra("userType", userType)  // Passing user type
            startActivity(intent)
        }
        headerView.findViewById<LinearLayout>(R.id.icon_logout).setOnClickListener {
            // Handle logout click
            FirebaseAuth.getInstance().signOut()

            // Clear SharedPreferences
            val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }


        // ----------------- Doctors RecyclerView -----------------
        recyclerViewDoctors = findViewById(R.id.recyclerViewDoctors)
        recyclerViewDoctors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val firebaseReadService = FirebaseReadService()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firebaseReadService.fetchApprovedAndCompleteProfileDoctors { doctors, error ->
            if (doctors != null) {
                val filteredDoctors = doctors.filter { it.UID != currentUserId }
                doctorAdapter = DoctorAdapter(filteredDoctors)
                recyclerViewDoctors.adapter = doctorAdapter
            } else {
                // Handle error
            }
        }

        val searchBtn = findViewById<FrameLayout>(R.id.searchButton)
        searchBtn.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }


        val scanButton = findViewById< FrameLayout>(R.id.gotoScanActivity)
        val scanHistoryButton = findViewById<FrameLayout>(R.id.gotoScanHistoryActivity)

        val chatButton = findViewById<FrameLayout>(R.id.gotoChatActivity)
        val chatHistoryButton = findViewById<FrameLayout>(R.id.gotoChatHistoryActivity)

        scanButton.setOnClickListener {
            startActivity(Intent(this, ScanImageActivity::class.java))
        }

        scanHistoryButton.setOnClickListener {
            startActivity(Intent(this, ScanHistoryActivity::class.java))
        }

        chatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("known", "0");
            startActivity(intent)
        }

        chatHistoryButton.setOnClickListener {
            val intent = Intent(this, ChatHistoryActivity::class.java)
            startActivity(intent)
        }




        // ----------------- Scan Button -----------------
        val scanBtn = findViewById<LinearLayout>(R.id.scanButton)
        scanBtn.setOnClickListener {
            startActivity(Intent(this, ScanImageActivity::class.java))
        }

        // ----------------- Chat Button -----------------
        val chatBtn = findViewById<LinearLayout>(R.id.chatButton)
        chatBtn.setOnClickListener {


            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("known", "0");
            startActivity(intent)

        }



        // ----------------- Dragging Gesture for Drawer -----------------
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
        scrollableLinearLayout.setOnTouchListener { _, event ->
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
        val hospitalLocation = headerView.findViewById<LinearLayout>(R.id.hospitalLocation)

        val profileViewButton = findViewById<ImageView>(R.id.ProfileViewButton)
        val profileViewButtonIcon = findViewById<ImageView>(R.id.ProfileViewButtonIcon)



        if (user is User) {
            hospitalLocation.visibility = View.GONE
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
            hospitalLocation.visibility = View.VISIBLE
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