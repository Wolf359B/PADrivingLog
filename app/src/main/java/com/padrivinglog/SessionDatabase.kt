package com.padrivinglog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// ── Data Models ──────────────────────────────────────────────────────────────

data class DrivingSession(
    val id: Long = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val totalMinutes: Int,
    val nightMinutes: Int,
    val inclemWeatherMinutes: Int
)

data class SessionTotals(
    val totalMinutes: Int,
    val nightMinutes: Int,
    val inclemWeatherMinutes: Int
)

// ── Database Helper ───────────────────────────────────────────────────────────

class SessionDatabase(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME    = "driving_log.db"
        const val DB_VERSION = 1

        const val TABLE   = "sessions"
        const val COL_ID      = "id"
        const val COL_DATE    = "date"
        const val COL_START   = "start_time"
        const val COL_END     = "end_time"
        const val COL_TOTAL   = "total_minutes"
        const val COL_NIGHT   = "night_minutes"
        const val COL_WEATHER = "weather_minutes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE (
                $COL_ID      INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DATE    TEXT NOT NULL,
                $COL_START   TEXT NOT NULL,
                $COL_END     TEXT NOT NULL,
                $COL_TOTAL   INTEGER NOT NULL,
                $COL_NIGHT   INTEGER NOT NULL,
                $COL_WEATHER INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    fun insertSession(session: DrivingSession): Long {
        val cv = ContentValues().apply {
            put(COL_DATE,    session.date)
            put(COL_START,   session.startTime)
            put(COL_END,     session.endTime)
            put(COL_TOTAL,   session.totalMinutes)
            put(COL_NIGHT,   session.nightMinutes)
            put(COL_WEATHER, session.inclemWeatherMinutes)
        }
        return writableDatabase.insert(TABLE, null, cv)
    }

    fun getAllSessions(): List<DrivingSession> {
        val list = mutableListOf<DrivingSession>()
        val cursor = readableDatabase.query(
            TABLE, null, null, null, null, null, "$COL_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.toSession())
            }
        }
        return list
    }

    fun getSession(id: Long): DrivingSession? {
        val cursor = readableDatabase.query(
            TABLE, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null
        )
        return cursor.use { if (it.moveToFirst()) it.toSession() else null }
    }

    fun deleteSession(id: Long) {
        writableDatabase.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun getTotals(): SessionTotals {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM($COL_TOTAL), SUM($COL_NIGHT), SUM($COL_WEATHER) FROM $TABLE", null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                SessionTotals(
                    totalMinutes         = it.getInt(0),
                    nightMinutes         = it.getInt(1),
                    inclemWeatherMinutes = it.getInt(2)
                )
            } else {
                SessionTotals(0, 0, 0)
            }
        }
    }

    private fun android.database.Cursor.toSession() = DrivingSession(
        id                   = getLong(getColumnIndexOrThrow(COL_ID)),
        date                 = getString(getColumnIndexOrThrow(COL_DATE)),
        startTime            = getString(getColumnIndexOrThrow(COL_START)),
        endTime              = getString(getColumnIndexOrThrow(COL_END)),
        totalMinutes         = getInt(getColumnIndexOrThrow(COL_TOTAL)),
        nightMinutes         = getInt(getColumnIndexOrThrow(COL_NIGHT)),
        inclemWeatherMinutes = getInt(getColumnIndexOrThrow(COL_WEATHER))
    )
}
