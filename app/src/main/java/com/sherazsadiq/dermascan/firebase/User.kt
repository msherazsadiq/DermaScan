package com.sherazsadiq.dermascan.firebase

import java.io.Serializable
import java.sql.Time
import java.util.Date

class User(
    val UserType: String = "Patient",
    var Email: String = "",
    var UID: String = "",
    var Name: String = "",
    var DOB: String = "",
    var Gender: String = "",
    var Address: String = "",
    var Phone: String = "",
    var ProfilePic: String = "",
    var CreatedAt: String = ""
) : Serializable
