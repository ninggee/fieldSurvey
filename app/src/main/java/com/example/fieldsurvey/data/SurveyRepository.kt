package com.example.fieldsurvey.data

class SurveyRepository(private val dao: SurveyDao) {
    suspend fun insert(record: SurveyRecord): Long = dao.insert(record)

    suspend fun listByDate(start: Long, end: Long): List<SurveyRecord> = dao.listByDate(start, end)

    suspend fun listAll(): List<SurveyRecord> = dao.listAll()
}

