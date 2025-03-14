package com.sherazsadiq.dermascan.firebase

import java.io.Serializable

class ScanData(
    var ScanDate: String = "",
    var ScanTime: String = "",
    var ImageURL: String = "",
    var FirstDName: String = "",
    var FirstDPercentage: String = "",
    var SecondDName: String = "",
    var SecondDPercentage: String = "",
    var ScanBodyPart: String = ""

) : Serializable


