package com.sherazsadiq.dermascan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DisplayModelActivity : AppCompatActivity() {

    private lateinit var faceButton: FrameLayout
    private lateinit var shoulderButton: FrameLayout
    private lateinit var chestButton: FrameLayout
    private lateinit var armButton: FrameLayout
    private lateinit var handButton: FrameLayout
    private lateinit var thighButton: FrameLayout
    private lateinit var kneeButton: FrameLayout
    private lateinit var legButton: FrameLayout
    private lateinit var footButton: FrameLayout

    private lateinit var faceSelected: ImageView
    private lateinit var shoulderSelected: ImageView
    private lateinit var chestSelected: ImageView
    private lateinit var armSelected: ImageView
    private lateinit var handSelected: ImageView
    private lateinit var thighSelected: ImageView
    private lateinit var kneeSelected: ImageView
    private lateinit var legSelected: ImageView
    private lateinit var footSelected: ImageView

    private lateinit var continueBtn: TextView
    private lateinit var otherButton: TextView

    private var selectedBodyPart: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display_model)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        faceButton = findViewById(R.id.face)
        shoulderButton = findViewById(R.id.shoulder)
        chestButton = findViewById(R.id.chest)
        armButton = findViewById(R.id.arm)
        handButton = findViewById(R.id.hand)
        thighButton = findViewById(R.id.thigh)
        kneeButton = findViewById(R.id.knee)
        legButton = findViewById(R.id.leg)
        footButton = findViewById(R.id.foot)

        faceSelected = findViewById(R.id.faceSelected)
        shoulderSelected = findViewById(R.id.shoulderSelected)
        chestSelected = findViewById(R.id.chestSelected)
        armSelected = findViewById(R.id.armSelected)
        handSelected = findViewById(R.id.handSelected)
        thighSelected = findViewById(R.id.thighSelected)
        kneeSelected = findViewById(R.id.kneeSelected)
        legSelected = findViewById(R.id.legSelected)
        footSelected = findViewById(R.id.footSelected)

        continueBtn = findViewById(R.id.continueButton)
        otherButton = findViewById(R.id.otherButton)

        // Setup button clicks
        val buttonMap = mapOf(
            faceButton to faceSelected,
            shoulderButton to shoulderSelected,
            chestButton to chestSelected,
            armButton to armSelected,
            handButton to handSelected,
            thighButton to thighSelected,
            kneeButton to kneeSelected,
            legButton to legSelected,
            footButton to footSelected
        )

        buttonMap.forEach { (button, selectedView) ->
            setupButton(button, selectedView)
        }

        // Continue button action
        continueBtn.setOnClickListener {
            selectedBodyPart?.let { bodyPart ->
                val intent = Intent(this, ScanImageActivity::class.java)
                intent.putExtra("bodyPart", bodyPart)
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Please select a body part first!", Toast.LENGTH_SHORT).show()
            }
        }

        // Other button action
        otherButton.setOnClickListener {
            showOtherBodyPartDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        selectedBodyPart = null
        listOf(
            faceSelected, shoulderSelected, chestSelected, armSelected, handSelected,
            thighSelected, kneeSelected, legSelected, footSelected
        ).forEach { it.visibility = ImageView.INVISIBLE }
    }

    private fun setupButton(button: FrameLayout, selectedView: ImageView) {
        button.setOnClickListener {
            vibrate()
            val bodyPart = resources.getResourceEntryName(button.id).capitalize()
            Toast.makeText(this, "$bodyPart selected", Toast.LENGTH_SHORT).show()
            toggleSelection(selectedView)
            selectedBodyPart = bodyPart
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(100)
            }
        }
    }

    private fun toggleSelection(visibleView: ImageView) {
        // Make all other views invisible
        listOf(
            faceSelected, shoulderSelected, chestSelected, armSelected, handSelected,
            thighSelected, kneeSelected, legSelected, footSelected
        ).forEach { it.visibility = ImageView.INVISIBLE }

        // Make the selected view visible
        visibleView.visibility = ImageView.VISIBLE
    }

    @SuppressLint("MissingInflatedId")
    private fun showOtherBodyPartDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_bodypart, null)
        val input = dialogView.findViewById<EditText>(R.id.bodyPartInput)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.continueButton).setOnClickListener {
            val bodyPart = input.text.toString().trim()
            if (bodyPart.isNotEmpty()) {
                selectedBodyPart = bodyPart
                val intent = Intent(this, ScanImageActivity::class.java)
                intent.putExtra("bodyPart", bodyPart)
                startActivity(intent)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a body part", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<TextView>(R.id.cancelButton).setOnClickListener {
            selectedBodyPart = null
            dialog.cancel()
        }

        dialog.show()
    }
}