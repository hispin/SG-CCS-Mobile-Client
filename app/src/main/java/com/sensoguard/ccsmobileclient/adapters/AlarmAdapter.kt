package com.sensoguard.ccsmobileclient.adapters

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.Alarm
import com.sensoguard.ccsmobileclient.global.getStrDateTimeByMilliSeconds

class AlarmAdapter(
    private var alarms: ArrayList<Alarm>,
    val context: Context,
    var itemClick: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((alarms[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_item, parent, false)

        return ViewHolder(view, itemClick)
    }

    /**
     * replace the alarms that are shown
     */
    //the whole list is replaced, so all the items should be refreshed
    @SuppressLint("NotifyDataSetChanged")
    fun setDetects(alarms: ArrayList<Alarm>?) {
        alarms?.let { this.alarms = it }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val _itemView: View, private val itemClick: (Alarm) -> Unit) :
        RecyclerView.ViewHolder(_itemView) {
        private var tvHub: TextView? = null
        private var tvZone: TextView? = null
        private var tvDate: TextView? = null
        private var tvTime: TextView? = null
        private var tvCoordinates: TextView? = null
        private var ivOpenGoogleMap: ImageView? = null
        private var tvType: TextView? = null


        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClick.invoke(alarms[position])
                }
            }

            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    //toggle the status of ready to delete
                    alarms[position].isReadyToDelete = !alarms[position].isReadyToDelete
                    notifyItemChanged(position, alarms[position])
                }
                return@setOnLongClickListener false
            }
        }


        fun bindReservation(alarm: Alarm) {
            tvHub = _itemView.findViewById(R.id.tvId)
            tvZone = _itemView.findViewById(R.id.tvZone)
            tvDate = _itemView.findViewById(R.id.tvDate)
            tvType = _itemView.findViewById(R.id.tvType)
            tvTime = _itemView.findViewById(R.id.tvTime)
            tvCoordinates = _itemView.findViewById(R.id.tvCoordinates)
            ivOpenGoogleMap = _itemView.findViewById(R.id.ivOpenGoogleMap)

            val textColor = if (alarm.isArmed == true && alarm.isLocallyDefined) {
                ContextCompat.getColor(context, R.color.red)
            } else {
                ContextCompat.getColor(context, R.color.black)
            }
            tvZone?.setTextColor(textColor)
            tvDate?.setTextColor(textColor)
            tvHub?.setTextColor(textColor)
            tvType?.setTextColor(textColor)
            tvTime?.setTextColor(textColor)
            tvCoordinates?.setTextColor(textColor)


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

            val coordinates = "${alarm.latitude} , ${alarm.longitude}"
            tvCoordinates?.text = coordinates

            ivOpenGoogleMap?.setOnClickListener {
                openGoogleMap(alarm.latitude, alarm.longitude)
            }

            //set selected/unselected
            itemView.alpha = if (alarm.isReadyToDelete) 0.5F else 1.0F
        }

        /**
         * open google map to navigation
         */
        private fun openGoogleMap(latitude: Double?, longitude: Double?) {
            val gmmIntentUri = "google.navigation:q=$latitude,$longitude&mode=d".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            try {
                context.startActivity(mapIntent)
            } catch (ex: ActivityNotFoundException) {
                //google maps is not installed
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
