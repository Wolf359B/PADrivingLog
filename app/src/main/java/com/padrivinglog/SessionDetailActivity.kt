package com.padrivinglog

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SessionDetailActivity : AppCompatActivity() {

    private lateinit var db: SessionDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        db = SessionDatabase(this)

        val sessionId = intent.getLongExtra("session_id", -1L)
        if (sessionId == -1L) { finish(); return }

        val session = db.getSession(sessionId)
        if (session == null) { finish(); return }

        findViewById<TextView>(R.id.tvDetailDate).text      = session.date
        findViewById<TextView>(R.id.tvDetailStart).text     = "Start: ${session.startTime}"
        findViewById<TextView>(R.id.tvDetailEnd).text       = "End:   ${session.endTime}"
        findViewById<TextView>(R.id.tvDetailDuration).text  = formatMins(session.totalMinutes)
        findViewById<TextView>(R.id.tvDetailNight).text     = formatMins(session.nightMinutes)
        findViewById<TextView>(R.id.tvDetailWeather).text   = formatMins(session.inclemWeatherMinutes)

        findViewById<Button>(R.id.btnDeleteSession).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Session")
                .setMessage("Are you sure you want to delete this session? This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteSession(sessionId)
                    Toast.makeText(this, "Session deleted.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Session Details"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun formatMins(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
