package com.rohitthebest.manageyourrenters.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Renter

class ShowRentersAdapter :
    ListAdapter<Renter, ShowRentersAdapter.RenterViewHolder>(DiffUtilCallback()) {

    inner class RenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setData(renter: Renter) {

            itemView.apply {


            }
        }
    }

    class DiffUtilCallback() : DiffUtil.ItemCallback<Renter>() {

        override fun areItemsTheSame(oldItem: Renter, newItem: Renter): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Renter, newItem: Renter): Boolean {

            return oldItem.id == newItem.id
                    && oldItem.timeStamp == newItem.timeStamp
                    && oldItem.key == newItem.key
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenterViewHolder {

        return RenterViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_show_renter, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RenterViewHolder, position: Int) {

        getItem(position)?.let {

            holder.setData(it)
        }
    }

}