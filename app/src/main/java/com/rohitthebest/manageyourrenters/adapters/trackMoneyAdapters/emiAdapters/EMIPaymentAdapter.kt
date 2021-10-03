package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterEmiPaymentBinding

class EMIPaymentAdapter :
    ListAdapter<EMIPayment, EMIPaymentAdapter.EMIPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class EMIPaymentViewHolder(val binding: AdapterEmiPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {


        }

        fun setData(emiPayment: EMIPayment?) {

            emiPayment?.let {

                binding.apply {


                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<EMIPayment>() {

            override fun areItemsTheSame(oldItem: EMIPayment, newItem: EMIPayment): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EMIPayment, newItem: EMIPayment): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EMIPaymentViewHolder {

        val binding =
            AdapterEmiPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return EMIPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EMIPaymentViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(emiPayment: EMIPayment)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

