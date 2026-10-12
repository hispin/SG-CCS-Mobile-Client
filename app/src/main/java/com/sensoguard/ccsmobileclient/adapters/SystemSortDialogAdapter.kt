package com.sensoguard.ccsmobileclient.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.ccsmobileclient.R
import com.sensoguard.ccsmobileclient.classes.SystemSort

class SystemSortDialogAdapter(
    private var systems: ArrayList<SystemSort>
) : RecyclerView.Adapter<SystemSortDialogAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((systems[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.systems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_system_filter_dialog, parent, false)

        return ViewHolder(view)
    }

    /**
     * replace the systems that are shown
     */
    //the whole list is replaced, so all the items should be refreshed
    @SuppressLint("NotifyDataSetChanged")
    fun setDetects(systems: ArrayList<SystemSort>?) {
        systems?.let { this.systems = it }
        notifyDataSetChanged()
    }

    class ViewHolder(private val _itemView: View) : RecyclerView.ViewHolder(_itemView) {
        private var tvName: TextView? = null
        private var togIsSelected: ToggleButton? = null

        fun bindReservation(systemSort: SystemSort) {
            tvName = _itemView.findViewById(R.id.tvName)
            togIsSelected = _itemView.findViewById(R.id.togIsSelected)

            togIsSelected?.setOnCheckedChangeListener { _, isChecked ->
                systemSort.isSorted = isChecked
            }

            tvName?.text = systemSort.unit
        }
    }
}
