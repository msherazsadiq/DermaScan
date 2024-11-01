package com.sherazsadiq.dermascan.firebase

import java.io.Serializable
import java.sql.Time
import java.util.Date

class Doctor(
    var UserType: String = "Doctor",
    var Email: String = "",
    var UID: String = "",
    var Name: String = "",
    var Specialization: String = "",
    var Phone: String = "",
    var Address: String = "",
    var Gender: String = "",
    var Rating: Double = 0.0,
    var Experience: String = "",
    var ProfilePic: String = "",
    var MedicalLicense: String = "",
    var DegreeCertificate: String = "",
    var GovernmentID: String = "",
    var Approved: Boolean = false,
    var CreatedAt: String = "",

    var huzaifa: String = ""
) : Serializable