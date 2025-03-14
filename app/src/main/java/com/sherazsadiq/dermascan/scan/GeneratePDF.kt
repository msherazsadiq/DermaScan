package com.sherazsadiq.dermascan.scan

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.sherazsadiq.dermascan.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateSkinScanPDF(
    context: Context,
    userImage: Bitmap,
    firstDisease: String,
    firstConfidence: String,
    secondDisease: String,
    secondConfidence: String,
    selectedBodyPart: String,
    location: String?
): String? {
    val pdfDocument = PdfDocument()
    val paint = Paint()
    val titlePaint = Paint()
    titlePaint.textSize = 20f
    titlePaint.isFakeBoldText = true

    val pageInfo = PdfDocument.PageInfo.Builder(600, 900, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    var yPos = 50f

    // **1️⃣ App Logo**
    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_dark)
    val scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, false)
    canvas.drawBitmap(scaledLogo, 250f, yPos, paint)
    yPos += 120f

    // **2️⃣ App Name**
    canvas.drawText("DermaScan AI", 230f, yPos, titlePaint)
    yPos += 40f

    // **3️⃣ App Slogan**
    paint.textSize = 16f
    canvas.drawText("Your AI-Powered Skin Health Assistant", 130f, yPos, paint)
    yPos += 50f

    // **4️⃣ User's Scanned Image**
    val scaledUserImage = Bitmap.createScaledBitmap(userImage, 200, 200, false)
    canvas.drawBitmap(scaledUserImage, 200f, yPos, paint)
    yPos += 220f

    // **5️⃣ Disease Prediction Results**
    titlePaint.textSize = 18f
    canvas.drawText("Diagnosis Results", 50f, yPos, titlePaint)
    yPos += 40f

    paint.textSize = 16f
    canvas.drawText("1. $firstDisease ($firstConfidence)", 50f, yPos, paint)
    yPos += 30f
    canvas.drawText("2. $secondDisease ($secondConfidence)", 50f, yPos, paint)
    yPos += 50f

    // **6️⃣ Scan Details**
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDateTime = dateFormat.format(Date())

    canvas.drawText("📅 Date & Time: $currentDateTime", 50f, yPos, paint)
    yPos += 30f
    canvas.drawText("🩹 Scanned Body Part: $selectedBodyPart", 50f, yPos, paint)
    yPos += 30f

    if (location != null) {
        canvas.drawText("📍 Location: $location", 50f, yPos, paint)
        yPos += 40f
    }

    // **7️⃣ Disclaimer**
    titlePaint.textSize = 16f
    canvas.drawText("⚠ Disclaimer", 50f, yPos, titlePaint)
    yPos += 30f
    paint.textSize = 14f
    canvas.drawText("This is an AI-generated result. Please consult a dermatologist for professional diagnosis.", 50f, yPos, paint)
    yPos += 30f
    canvas.drawText("Follow proper skincare practices and do not rely solely on this report.", 50f, yPos, paint)

    // Finish the page
    pdfDocument.finishPage(page)

    // **8️⃣ Save PDF File to Downloads**
    val fileName = "SkinScanReport_${System.currentTimeMillis()}.pdf"
    var pdfFilePath: String?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Save to Downloads using MediaStore (Android 10+)
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(it).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfFilePath = uri.toString()
        } ?: run {
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            pdfFilePath = null
        }
    } else {
        // Save to Downloads folder (Android 9 and below)
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        try {
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfFilePath = file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            pdfFilePath = null
        }
    }

    pdfDocument.close()

    return pdfFilePath
}



