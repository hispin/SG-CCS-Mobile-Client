package com.sensoguard.detectsensor.adapters

import android.content.Context
//import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import com.sensoguard.detectsensor.classes.Sensor
import com.sensoguard.detectsensor.interfaces.OnAdapterListener
import java.util.ArrayList
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.detectsensor.R


class SensorsAdapter (private var sensors: ArrayList<Sensor>, val context: Context, val onAdapterListener: OnAdapterListener, val isShowAll:Boolean, var itemClick: (Sensor) -> Unit) : RecyclerView.Adapter<SensorsAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((sensors[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.sensors.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.sensor_item, parent, false)


        return ViewHolder(view, itemClick)
    }

    fun setDetects(_detectors: ArrayList<Sensor>?) {
        _detectors?.let{sensors= it}
        //TODO how to define with this
    }

    inner class ViewHolder(private val _itemView: View, private val itemClick: (Sensor) -> Unit) : RecyclerView.ViewHolder(_itemView) {
        private var tvId: TextView?=null
        private var tvName: TextView?=null
        private var togIsActive: ToggleButton?=null
        private var ibEditName:ImageButton?=null
        private var tvIsLocate:TextView?=null

        //TODO press twice
        var etName: TextView?=null


        init {
            itemView.setOnClickListener {
                itemClick.invoke(sensors[adapterPosition])
            }
        }


        fun bindReservation(sensor: Sensor) {
            tvId = _itemView.findViewById(R.id.tvId)
           tvName = _itemView.findViewById(R.id.tvName)
           togIsActive = _itemView.findViewById(R.id.togIsActive)
           etName = _itemView.findViewById(R.id.etName)
           ibEditName = _itemView.findViewById(R.id.ibEditName)
           tvIsLocate = _itemView.findViewById(R.id.tvIsLocate)


           if(isShowAll){
               //in position 0 show the titles

               //check if the sensor is located in map then sign it in accordance
               togIsActive?.isChecked = sensor.isArmed()
               if(sensor.getLongtitude()==null || sensor.getLatitude()== null){
                   tvIsLocate?.setTextColor( ContextCompat.getColor(context,R.color.red1))
                   tvIsLocate?.text = context.resources.getString(R.string.not_located)
                   tvIsLocate?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_not_located, 0, 0, 0)
               }else{
                   tvIsLocate?.setTextColor( ContextCompat.getColor(context,R.color.turquoise_blue))
                   tvIsLocate?.text = context.resources.getString(R.string.located)
                   tvIsLocate?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_located, 0, 0, 0)
               }

               togIsActive?.setOnCheckedChangeListener { buttonView, isChecked ->
                   sensor.setArm(isChecked)
                   onAdapterListener.saveDetector(sensor)
               }


               ibEditName?.setOnClickListener{
                   tvName?.visibility = View.INVISIBLE
                   etName?.visibility = View.VISIBLE
                   etName?.requestFocus()
                   val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                   manager!!.showSoftInput(etName, 0)
                   return@setOnClickListener
               }


//               tvName?.setOnLongClickListener {
//                   tvName?.visibility = View.INVISIBLE
//                   etName?.visibility = View.VISIBLE
//                   etName?.requestFocus()
//                   val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//                   manager!!.showSoftInput(etName, 0)
//                   return@setOnLongClickListener true
//               }

               etName?.setOnEditorActionListener { v, actionId, event ->
                   if (actionId == EditorInfo.IME_ACTION_DONE) {
                       if (!v.text.isNullOrEmpty()) {
                           sensor.setName(v.text.toString())
                           onAdapterListener.saveNameSensor(sensor)
                       }
                       tvName?.visibility = View.VISIBLE
                       etName?.visibility = View.INVISIBLE
                       //TODO find better way to hide the softkey
                       val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                       imm.hideSoftInputFromWindow(v.windowToken, 0)
                       true
                   } else {
                       false
                   }
               }
           }else {
               togIsActive?.visibility=View.GONE
               tvIsLocate?.visibility=View.GONE
           }


               tvId?.text = sensor.getId()
               tvName?.text = sensor.getName()
               etName?.hint= sensor.getName()

        }
    }
}