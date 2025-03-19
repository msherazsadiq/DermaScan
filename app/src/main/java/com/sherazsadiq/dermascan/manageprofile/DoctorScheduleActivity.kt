package com.sherazsadiq.dermascan.manageprofile

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Schedule
import com.sherazsadiq.dermascan.setStatusBarColor

class DoctorScheduleActivity : AppCompatActivity() {

    // Create a list of schedules from Monday to Sunday
    private val schedules = mutableListOf(
        Schedule("Monday", "", ""),
        Schedule("Tuesday", "", ""),
        Schedule("Wednesday", "", ""),
        Schedule("Thursday", "", ""),
        Schedule("Friday", "", ""),
        Schedule("Saturday", "", ""),
        Schedule("Sunday", "", "")
    )


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_schedule)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<LinearLayout>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // -------------- Monday --------------
        val mondayStartTimeButton: TextView = findViewById(R.id.mondayStartTimeButton)
        val mondayEndTimeButton: TextView = findViewById(R.id.mondayEndTimeButton)

        mondayStartTimeButton.setOnClickListener {
            showTimePickerDialog("Monday", true, mondayStartTimeButton)
        }

        mondayEndTimeButton.setOnClickListener {
            showTimePickerDialog("Monday", false, mondayEndTimeButton)
        }

        // -------------- Tuesday --------------
        val tuesdayStartTimeButton: TextView = findViewById(R.id.tuesdayStartTimeButton)
        val tuesdayEndTimeButton: TextView = findViewById(R.id.tuesdayEndTimeButton)

        tuesdayStartTimeButton.setOnClickListener {
            showTimePickerDialog("Tuesday", true, tuesdayStartTimeButton)
        }

        tuesdayEndTimeButton.setOnClickListener {
            showTimePickerDialog("Tuesday", false, tuesdayEndTimeButton)
        }

        // -------------- Wednesday --------------
        val wednesdayStartTimeButton: TextView = findViewById(R.id.wednesdayStartTimeButton)
        val wednesdayEndTimeButton: TextView = findViewById(R.id.wednesdayEndTimeButton)

        wednesdayStartTimeButton.setOnClickListener {
            showTimePickerDialog("Wednesday", true, wednesdayStartTimeButton)
        }

        wednesdayEndTimeButton.setOnClickListener {
            showTimePickerDialog("Wednesday", false, wednesdayEndTimeButton)
        }


        // -------------- Thursday --------------




    }

    private fun showTimePickerDialog(day: String, isStartTime: Boolean, button: TextView) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.time_picker_dialog, null)
        dialog.setContentView(view)

        val timePicker: TimePicker = view.findViewById(R.id.timePicker)
        timePicker.setIs24HourView(false) // 12-hour format
        val confirmButton: TextView = view.findViewById(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val amPm = if (hour >= 12) "PM" else "AM"
            val hourIn12Format = if (hour % 12 == 0) 12 else hour % 12
            val time = String.format("%02d:%02d %s", hourIn12Format, minute, amPm)

            button.text = time  // Update UI

            // Update the schedule in the list
            schedules.find { it.day == day }?.let {
                if (isStartTime) {
                    it.startTime = time
                } else {
                    it.endTime = time
                }
            }

            dialog.dismiss()
        }

        dialog.show()
    }

}