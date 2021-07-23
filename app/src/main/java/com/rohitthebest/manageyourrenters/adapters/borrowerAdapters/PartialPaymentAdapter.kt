package com.rohitthebest.manageyourrenters.adapters.borrowerAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterPartialPaymentBinding
import com.rohitthebest.manageyourrenters.utils.setDateInTextView

class PartialPaymentAdapter(val currencySymbol: String = "₹") :
    ListAdapter<PartialPayment, PartialPaymentAdapter.PartialPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class PartialPaymentViewHolder(val binding: AdapterPartialPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.deletePartialPayment.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onDeleteBtnClick(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun setData(partialPayment: PartialPayment?) {

            partialPayment?.let { pPayment ->

                binding.apply {

                    partialPaymentDateTV.setDateInTextView(
                        pPayment.created
                    )

                    partialPaymentTV.text = "$currencySymbol ${pPayment.amount}"
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<PartialPayment>() {

            override fun areItemsTheSame(
                oldItem: PartialPayment,
                newItem: PartialPayment
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PartialPayment,
                newItem: PartialPayment
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartialPaymentViewHolder {

        val binding =
            AdapterPartialPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PartialPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartialPaymentViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onDeleteBtnClick(partialPayment: PartialPayment, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

