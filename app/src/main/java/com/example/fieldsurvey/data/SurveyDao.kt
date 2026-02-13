package com.example.fieldsurvey.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SurveyDao {
    @Insert
    suspend fun insert(record: SurveyRecord): Long

    @Update
    suspend fun update(record: SurveyRecord)

    @Query("SELECT * FROM survey_records WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    suspend fun listByDate(start: Long, end: Long): List<SurveyRecord>

    @Query("SELECT * FROM survey_records ORDER BY createdAt DESC")
    suspend fun listAll(): List<SurveyRecord>

    @Query("SELECT * FROM survey_records WHERE mileageKm = :km AND mileageDecimal = :decimal LIMIT 1")
    suspend fun findByMileage(km: Int, decimal: Double): SurveyRecord?

    @Query("DELETE FROM survey_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

