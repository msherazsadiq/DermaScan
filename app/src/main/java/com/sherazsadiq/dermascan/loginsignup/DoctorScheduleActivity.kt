package com.sherazsadiq.dermascan.loginsignup

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.setStatusBarColor

class DoctorScheduleActivity : AppCompatActivity() {
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

        val mondayStartTimeButton: TextView = findViewById(R.id.mondayStartTimeButton)
        val mondayEndTimeButton: TextView = findViewById(R.id.mondayEndTimeButton)

        mondayStartTimeButton.setOnClickListener { showTimePickerDialog(mondayStartTimeButton) }
        mondayEndTimeButton.setOnClickListener { showTimePickerDialog(mondayEndTimeButton) }
    }

    private fun showTimePickerDialog(button: TextView) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.time_picker_dialog, null)
        dialog.setContentView(view)

        val timePicker: TimePicker = view.findViewById(R.id.timePicker)
        timePicker.setIs24HourView(false) // Set the TimePicker to 12-hour format
        val confirmButton: TextView = view.findViewById(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val amPm = if (hour >= 12) "PM" else "AM"
            val hourIn12Format = if (hour % 12 == 0) 12 else hour % 12
            val time = String.format("%02d:%02d %s", hourIn12Format, minute, amPm)
            button.text = time
            dialog.dismiss()
        }

        dialog.show()
    }
}