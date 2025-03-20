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
    var DOB: String = "",
    var Phone: String = "",
    var Address: String = "",
    var Gender: String = "",
    var Experience: String = "",
    var ProfilePic: String = "",
    var MedicalLicense: String = "",
    var DegreeCertificate: String = "",
    var GovernmentID: String = "",
    var Approved: Boolean = false,
    var CreatedAt: String = "",
    var ProfileComplete: Boolean = false

) : Serializable

class DocLocation(
    var LocName: String = "",
    var LocAddress: String = "",
    var LocPhone: String = "",
    var LocWebsite: String = "",
    var LocURL: String = "",
    var LocComplete: Boolean = false
) : Serializable


// class Schedule(val day: String, var startTime: String, var endTime: String)
