package com.padrivinglog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SessionAdapter(
    private var sessions: List<DrivingSession>,
    private val onClick: (DrivingSession) -> Unit
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate    : TextView = view.findViewById(R.id.tvItemDate)
        val tvTimes   : TextView = view.findViewById(R.id.tvItemTimes)
        val tvDuration: TextView = view.findViewById(R.id.tvItemDuration)
        val tvTags    : TextView = view.findViewById(R.id.tvItemTags)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]

        holder.tvDate.text     = session.date
        holder.tvTimes.text    = "${session.startTime} – ${session.endTime}"
        holder.tvDuration.text = formatMins(session.totalMinutes)

        val tags = buildList {
            if (session.nightMinutes > 0)         add("🌙 Night: ${formatMins(session.nightMinutes)}")
            if (session.inclemWeatherMinutes > 0) add("🌧 Weather: ${formatMins(session.inclemWeatherMinutes)}")
        }
        if (tags.isEmpty()) {
            holder.tvTags.visibility = View.GONE
        } else {
            holder.tvTags.visibility = View.VISIBLE
            holder.tvTags.text = tags.joinToString("  ·  ")
        }

        holder.itemView.setOnClickListener { onClick(session) }
    }

    override fun getItemCount() = sessions.size

    fun updateSessions(newSessions: List<DrivingSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    private fun formatMins(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
