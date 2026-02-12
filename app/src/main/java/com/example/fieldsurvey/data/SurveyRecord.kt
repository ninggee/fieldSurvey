package com.example.fieldsurvey.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survey_records")
data class SurveyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lineType: String,
    val mileageRaw: Double,
    val mileageDk: String,
    val depthM: Double,
    val photoPath: String,
    val createdAt: Long
)

