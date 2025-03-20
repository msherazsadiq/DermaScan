package com.sherazsadiq.dermascan.scan

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.ScanData

class ScanDetailsActivity : AppCompatActivity() {

    private lateinit var scan: ScanData

    private lateinit var scanImageView: ImageView
    private lateinit var firstDName: TextView
    private lateinit var firstDPercentage: TextView
    private lateinit var secondDName: TextView
    private lateinit var secondDPercentage: TextView
    private lateinit var scanDate: TextView
    private lateinit var scanTime: TextView
    private lateinit var scanBodyPart : TextView

    private lateinit var docDownload: FrameLayout

    private lateinit var pdfPath: String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scan = intent.getSerializableExtra("ScanDetails") as ScanData

        val backButton = findViewById<FrameLayout>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        firstDName = findViewById<TextView>(R.id.firstDiseaseName)
        firstDPercentage = findViewById<TextView>(R.id.firstDiseasePercentage)
        secondDName = findViewById<TextView>(R.id.secondDiseaseName)
        secondDPercentage = findViewById<TextView>(R.id.secondDiseasePercentage)
        scanImageView = findViewById<ImageView>(R.id.scannedImage)
        scanDate = findViewById<TextView>(R.id.scanDate)
        scanTime = findViewById<TextView>(R.id.scanTime)
        scanBodyPart = findViewById<TextView>(R.id.scanBodyPart)


        firstDName.text = scan.FirstDName
        firstDPercentage.text = scan.FirstDPercentage

        if (scan.SecondDPercentage == "0%"){
            findViewById<FrameLayout>(R.id.secondDiseaseResult).visibility = FrameLayout.GONE
        }else{
            secondDName.text = scan.SecondDName
            secondDPercentage.text = scan.SecondDPercentage

        }


        scanDate.text = scan.ScanDate
        scanTime.text = scan.ScanTime
        scanBodyPart.text = scan.ScanBodyPart

        // display image
        Glide.with(this)
            .load(scan.ImageURL)
            .into(scanImageView)


        docDownload = findViewById(R.id.docDownloadButton)
        docDownload.setOnClickListener {
            // Convert Image URL to Bitmap
            downloadImageAsBitmap(scan.ImageURL) { bitmap ->
                if (bitmap != null) {
                    // Generate PDF using the Bitmap

                    if(scan.SecondDPercentage == "0%"){
                        pdfPath = generateSkinScanPDF(
                            this,
                            bitmap, // Pass the downloaded bitmap
                            scan.FirstDName,
                            scan.FirstDPercentage,
                            "",
                            "",
                            scan.ScanBodyPart
                        ).toString()
                    } else {
                        pdfPath = generateSkinScanPDF(
                            this,
                            bitmap, // Pass the downloaded bitmap
                            scan.FirstDName,
                            scan.FirstDPercentage,
                            scan.SecondDName,
                            scan.SecondDPercentage,
                            scan.ScanBodyPart
                        ).toString()
                    }

                    if (pdfPath != null) {
                        Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show()

                    } else {
                        Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    private fun downloadImageAsBitmap(imageUrl: String, callback: (Bitmap?) -> Unit) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    callback(resource) // Return the bitmap
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    callback(null) // Return null if image loading fails
                }
            })
    }

}