package com.sherazsadiq.dermascan.introslider

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.sherazsadiq.dermascan.R
import android.os.Handler
import android.os.Looper
import com.sherazsadiq.dermascan.loginsignup.RegisterOptionsActivity
import com.sherazsadiq.dermascan.setStatusBarColor


class IntroSliderActivity : AppCompatActivity() {
    private lateinit var swipeUnlock: SeekBar
    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro_slider)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = IntroSliderAdapter(this)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
                resetAutoScroll()
            }
        })

        swipeUnlock = findViewById(R.id.swipe_unlock)
        swipeUnlock.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 10) {
                    seekBar?.progress = 10
                } else if (progress > 85) {
                    seekBar?.progress = 80
                } else if (progress >= 80) {
                    // Vibrate the device
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(100) // Vibrate for 100 milliseconds

                    val intent = Intent(this@IntroSliderActivity, RegisterOptionsActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: handle start of swipe
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Reset the SeekBar to initial position
                seekBar?.progress = 10
            }
        })

        startAutoScroll()
    }

    private fun startAutoScroll() {
        handler.postDelayed({
            currentPage = (currentPage + 1) % 3
            viewPager.setCurrentItem(currentPage, true)
            startAutoScroll()
        }, getDelayForCurrentPage())
    }

    private fun resetAutoScroll() {
        handler.removeCallbacksAndMessages(null)
        startAutoScroll()
    }

    private fun getDelayForCurrentPage(): Long {
        return when (currentPage) {
            0 -> 3000L // 3 seconds for fragment 1
            else -> 6000L // 6 seconds for fragments 2 and 3
        }
    }

    override fun onResume() {
        super.onResume()
        swipeUnlock.progress = 10
        resetAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

}