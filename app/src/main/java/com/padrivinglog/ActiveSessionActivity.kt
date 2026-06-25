package com.padrivinglog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.*

class ActiveSessionActivity : AppCompatActivity() {

    private lateinit var db: SessionDatabase

    private lateinit var tvDate: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvElapsed: TextView
    private lateinit var tvNightTime: TextView
    private lateinit var tvWeatherTime: TextView
    private lateinit var switchNight: SwitchMaterial
    private lateinit var switchWeather: SwitchMaterial
    private lateinit var btnStop: Button

    private val handler = Handler(Looper.getMainLooper())
    private var sessionStartMs: Long = 0L
    private var nightStartMs: Long   = 0L
    private var weatherStartMs: Long = 0L
    private var accNightMs: Long     = 0L
    private var accWeatherMs: Long   = 0L

    private val dateFormat  = SimpleDateFormat("MMMM d, yyyy", Locale.US)
    private val timeFormat  = SimpleDateFormat("h:mm a", Locale.US)

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTimerDisplay()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_session)

        db = SessionDatabase(this)

        tvDate       = findViewById(R.id.tvDate)
        tvStartTime  = findViewById(R.id.tvStartTime)
        tvElapsed    = findViewById(R.id.tvElapsed)
        tvNightTime  = findViewById(R.id.tvNightTime)
        tvWeatherTime= findViewById(R.id.tvWeatherTime)
        switchNight  = findViewById(R.id.switchNight)
        switchWeather= findViewById(R.id.switchWeather)
        btnStop      = findViewById(R.id.btnStop)

        sessionStartMs = System.currentTimeMillis()
        val now = Date(sessionStartMs)
        tvDate.text      = dateFormat.format(now)
        tvStartTime.text = "Started: ${timeFormat.format(now)}"

        switchNight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nightStartMs = System.currentTimeMillis()
            } else {
                if (nightStartMs > 0) {
                    accNightMs += System.currentTimeMillis() - nightStartMs
                    nightStartMs = 0L
                }
            }
        }

        switchWeather.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                weatherStartMs = System.currentTimeMillis()
            } else {
                if (weatherStartMs > 0) {
                    accWeatherMs += System.currentTimeMillis() - weatherStartMs
                    weatherStartMs = 0L
                }
            }
        }

        btnStop.setOnClickListener {
            confirmStop()
        }

        handler.post(timerRunnable)
    }

    private fun updateTimerDisplay() {
        val now        = System.currentTimeMillis()
        val totalMs    = now - sessionStartMs
        val curNightMs = if (nightStartMs > 0) accNightMs + (now - nightStartMs) else accNightMs
        val curWeathMs = if (weatherStartMs > 0) accWeatherMs + (now - weatherStartMs) else accWeatherMs

        tvElapsed.text    = formatDuration(totalMs)
        tvNightTime.text  = formatDuration(curNightMs)
        tvWeatherTime.text= formatDuration(curWeathMs)
    }

    private fun confirmStop() {
        AlertDialog.Builder(this)
            .setTitle("End Session?")
            .setMessage("This will save and end your current driving session.")
            .setPositiveButton("End Session") { _, _ -> saveSession() }
            .setNegativeButton("Keep Driving", null)
            .show()
    }

    private fun saveSession() {
        handler.removeCallbacks(timerRunnable)

        val now        = System.currentTimeMillis()
        val totalMs    = now - sessionStartMs

        // Capture any still-running sub-timers
        val finalNightMs = if (nightStartMs > 0) accNightMs + (now - nightStartMs) else accNightMs
        val finalWeathMs = if (weatherStartMs > 0) accWeatherMs + (now - weatherStartMs) else accWeatherMs

        val totalMins   = (totalMs   / 1000 / 60).toInt()
        val nightMins   = (finalNightMs / 1000 / 60).toInt()
        val weatherMins = (finalWeathMs / 1000 / 60).toInt()

        if (totalMins < 1) {
            Toast.makeText(this, "Session too short to log (under 1 minute).", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val session = DrivingSession(
            date             = dateFormat.format(Date(sessionStartMs)),
            startTime        = timeFormat.format(Date(sessionStartMs)),
            endTime          = timeFormat.format(Date(now)),
            totalMinutes     = totalMins,
            nightMinutes     = nightMins,
            inclemWeatherMinutes = weatherMins
        )

        db.insertSession(session)
        Toast.makeText(this, "Session saved! ${formatDurationMins(totalMins)} logged.", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun formatDuration(ms: Long): String {
        val totalSecs = ms / 1000
        val h = totalSecs / 3600
        val m = (totalSecs % 3600) / 60
        val s = totalSecs % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    private fun formatDurationMins(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Discard Session?")
            .setMessage("Going back will discard this session. End and save it instead?")
            .setPositiveButton("End & Save") { _, _ -> saveSession() }
            .setNegativeButton("Discard") { _, _ -> super.onBackPressed() }
            .show()
    }
}
