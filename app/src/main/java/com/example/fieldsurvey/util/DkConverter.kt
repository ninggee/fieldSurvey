package com.example.fieldsurvey.util

import kotlin.math.floor

fun toDk(mileageMeters: Double): String {
    val total = if (mileageMeters < 0) 0.0 else mileageMeters
    val km = floor(total / 1000.0).toInt()
    val m = floor(total % 1000.0).toInt()
    val metersPart = m.toString().padStart(3, '0')
    return "DK${km}+${metersPart}"
}

