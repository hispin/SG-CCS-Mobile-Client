package com.sensoguard.ccsmobileclient.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.Alarm
import com.sensoguard.ccsmobileclient.global.getStrDateTimeByMilliSeconds
import com.sensoguard.ccsmobileclient.interfaces.OnAdapterListener

class AlarmAdapter (private var alarms: ArrayList<Alarm>, val context: Context, val onAdapterListener: OnAdapterListener, var itemClick: (Alarm) -> Unit) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((alarms[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.alarm_item, parent, false)


        return ViewHolder(view, itemClick)
    }

    fun setDetects(_alarm: ArrayList<Alarm>?) {
        _alarm?.let { alarms = it }
        //TODO how to define with this
    }

    inner class ViewHolder(private val _itemView: View, private val itemClick: (Alarm) -> Unit) :
        RecyclerView.ViewHolder(_itemView) {
        private var tvHub: TextView? = null
        private var tvZone: TextView? = null
        private var tvDate: TextView? = null
        private var tvTime: TextView? = null
        private var ivOpenGoogleMap: ImageView? = null

        //TODO press twice
        private var tvType: TextView? = null


        init {
            itemView.setOnClickListener {
                itemClick.invoke(alarms[adapterPosition])
            }
        }


        fun bindReservation(alarm: Alarm) {
            tvHub = _itemView.findViewById(R.id.tvId)
            tvZone = _itemView.findViewById(R.id.tvZone)
            tvDate = _itemView.findViewById(R.id.tvDate)
            tvType = _itemView.findViewById(R.id.tvType)
            tvTime = _itemView.findViewById(R.id.tvTime)
            ivOpenGoogleMap = _itemView.findViewById(R.id.ivOpenGoogleMap)

            if (alarm.isArmed != null
                && alarm.isArmed!!
                && alarm.isLocallyDefined != null
                && alarm.isLocallyDefined
            ) {
                tvZone?.setTextColor(ContextCompat.getColor(context, R.color.red))
                tvDate?.setTextColor(ContextCompat.getColor(context, R.color.red))
                tvHub?.setTextColor(ContextCompat.getColor(context, R.color.red))
                tvType?.setTextColor(ContextCompat.getColor(context, R.color.red))
                tvTime?.setTextColor(ContextCompat.getColor(context, R.color.red))
            } else {
                tvZone?.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvDate?.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvHub?.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvType?.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvTime?.setTextColor(ContextCompat.getColor(context, R.color.black))
            }


            tvDate?.text =
                alarm.timeInMillis?.let { getStrDateTimeByMilliSeconds(it, "dd/MM/yy", context) }
            tvTime?.text =
                alarm.timeInMillis?.let { getStrDateTimeByMilliSeconds(it, "kk:mm:ss", context) }

            // if it is a new system set it simply
            if (alarm.isNewSystem) {
                tvHub?.text = alarm.id
                tvZone?.text = alarm.zone
            } else {
                val tmp = alarm.id?.split("-")
                if (tmp != null && tmp.size > 1) {
                    tvHub?.text = tmp[0]
                    tvZone?.text = tmp[1]
                }
            }
            tvType?.text = alarm.type

            ivOpenGoogleMap?.setOnClickListener {
                openGoogleMap(alarm.latitude, alarm.longitude)
            }
        }

        /**
         * open google map to navigation
         */
        private fun openGoogleMap(latitude: Double?, longitude: Double?) {
            val gmmIntentUri =
                Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        }
    }
}