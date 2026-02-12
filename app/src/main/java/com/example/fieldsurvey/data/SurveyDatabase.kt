package com.example.fieldsurvey.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SurveyRecord::class], version = 3)
abstract class SurveyDatabase : RoomDatabase() {
    abstract fun surveyDao(): SurveyDao

    companion object {
        @Volatile
        private var INSTANCE: SurveyDatabase? = null

        fun getInstance(context: Context): SurveyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SurveyDatabase::class.java,
                    "survey.db"
                )
                    .fallbackToDestructiveMigration() // 数据库结构改变时允许重建
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

