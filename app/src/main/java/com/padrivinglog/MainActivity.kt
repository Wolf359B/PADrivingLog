package com.padrivinglog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var db: SessionDatabase
    private lateinit var sessionAdapter: SessionAdapter

    private lateinit var tvTotalHours: TextView
    private lateinit var tvNightHours: TextView
    private lateinit var tvWeatherHours: TextView
    private lateinit var pbTotal: ProgressBar
    private lateinit var pbNight: ProgressBar
    private lateinit var pbWeather: ProgressBar
    private lateinit var tvTotalLabel: TextView
    private lateinit var tvNightLabel: TextView
    private lateinit var tvWeatherLabel: TextView
    private lateinit var rvSessions: RecyclerView
    private lateinit var fabNewSession: ExtendedFloatingActionButton
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = SessionDatabase(this)

        tvTotalHours  = findViewById(R.id.tvTotalHours)
        tvNightHours  = findViewById(R.id.tvNightHours)
        tvWeatherHours= findViewById(R.id.tvWeatherHours)
        pbTotal       = findViewById(R.id.pbTotal)
        pbNight       = findViewById(R.id.pbNight)
        pbWeather     = findViewById(R.id.pbWeather)
        tvTotalLabel  = findViewById(R.id.tvTotalLabel)
        tvNightLabel  = findViewById(R.id.tvNightLabel)
        tvWeatherLabel= findViewById(R.id.tvWeatherLabel)
        rvSessions    = findViewById(R.id.rvSessions)
        fabNewSession = findViewById(R.id.fabNewSession)
        tvEmptyState  = findViewById(R.id.tvEmptyState)

        sessionAdapter = SessionAdapter(emptyList()) { session ->
            val intent = Intent(this, SessionDetailActivity::class.java)
            intent.putExtra("session_id", session.id)
            startActivity(intent)
        }
        rvSessions.layoutManager = LinearLayoutManager(this)
        rvSessions.adapter = sessionAdapter

        fabNewSession.setOnClickListener {
            startActivity(Intent(this, ActiveSessionActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun refreshDashboard() {
        val sessions = db.getAllSessions()
        val totals   = db.getTotals()

        val totalMins   = totals.totalMinutes
        val nightMins   = totals.nightMinutes
        val weatherMins = totals.inclemWeatherMinutes

        val totalHrs   = totalMins   / 60.0
        val nightHrs   = nightMins   / 60.0
        val weatherHrs = weatherMins / 60.0

        tvTotalHours.text   = "%.1f / 65 hrs".format(totalHrs)
        tvNightHours.text   = "%.1f / 10 hrs".format(nightHrs)
        tvWeatherHours.text = "%.1f / 5 hrs".format(weatherHrs)

        tvTotalLabel.text   = "Total Practice  (65 hrs required)"
        tvNightLabel.text   = "Night Driving  (10 hrs required)"
        tvWeatherLabel.text = "Inclement Weather  (5 hrs required)"

        pbTotal.max      = 65 * 60
        pbNight.max      = 10 * 60
        pbWeather.max    = 5  * 60

        pbTotal.progress   = minOf(totalMins,   65 * 60)
        pbNight.progress   = minOf(nightMins,   10 * 60)
        pbWeather.progress = minOf(weatherMins, 5  * 60)

        if (sessions.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvSessions.visibility   = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvSessions.visibility   = View.VISIBLE
            sessionAdapter.updateSessions(sessions)
        }
    }
}
