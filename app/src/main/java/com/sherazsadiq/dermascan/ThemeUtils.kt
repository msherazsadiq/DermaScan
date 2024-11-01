package com.sherazsadiq.dermascan

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

fun setStatusBarColor(activity: Activity) {
    // Set the status bar background color to DarkBlue
    activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.DarkBlue)

    // Set the navigation bar background color to DarkBlue
    activity.window.navigationBarColor = ContextCompat.getColor(activity, R.color.DarkBlue)

    // Ensure the status bar icons are white
    activity.window.decorView.systemUiVisibility = activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
}