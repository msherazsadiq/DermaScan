package com.sherazsadiq.dermascan.firebase

import java.io.Serializable

class AppFeedback (
    var starRating: Float = 0.0f,
    var feedbackText: String = "",
    var userType: String = "",
    var userUID: String = ""

) : Serializable