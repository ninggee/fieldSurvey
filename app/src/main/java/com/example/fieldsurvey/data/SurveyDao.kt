package com.example.fieldsurvey.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SurveyDao {
    @Insert
    suspend fun insert(record: SurveyRecord): Long

    @Query("SELECT * FROM survey_records WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    suspend fun listByDate(start: Long, end: Long): List<SurveyRecord>

    @Query("SELECT * FROM survey_records ORDER BY createdAt DESC")
    suspend fun listAll(): List<SurveyRecord>
}

