package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterEmiPaymentBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show

class EMIPaymentAdapter :
    ListAdapter<EMIPayment, EMIPaymentAdapter.EMIPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class EMIPaymentViewHolder(val binding: AdapterEmiPaymentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.emiPaymentDocIB.setOnClickListener(this)
            binding.emiPaymentSyncedIV.setOnClickListener(this)
            binding.emiPaymentMessageIB.setOnClickListener(this)
            binding.emiPaymentDeleteIB.setOnClickListener(this)


        }

        @SuppressLint("SetTextI18n")
        fun setData(emiPayment: EMIPayment?) {

            emiPayment?.let { payment ->

                binding.apply {

                    // showing delete button only on the first item
                    if (absoluteAdapterPosition == 0) {

                        emiPaymentDeleteIB.show()
                    } else {

                        emiPaymentDeleteIB.hide()
                    }

                    emiAmountPaymentTV.text = "Amount : ${payment.amountPaid}"

                    // setting moth TV
                    val monthTVText = if (payment.fromMonth == payment.tillMonth) {

                        // payment is for a single month
                        "For month : ${payment.fromMonth}"
                    } else {

                        "From month : ${payment.fromMonth}\nTill month :    ${payment.tillMonth}"
                    }

                    emiMonthPaidTV.text = monthTVText

                    emiPaidOnTV.text = "Paid On : ${
                        WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(payment.created)
                    }"

                    if (payment.isSynced) {

                        emiPaymentSyncedIV.setImageResource(R.drawable.ic_baseline_sync_24_green)
                    } else {

                        emiPaymentSyncedIV.setImageResource(R.drawable.ic_baseline_sync_24)
                    }
                }
            }
        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
                    mListener != null
        }

        override fun onClick(v: View?) {

            if (checkForNullability(absoluteAdapterPosition)) {

                when (v?.id) {

                    binding.emiPaymentDocIB.id -> {

                        mListener!!.onEMIDocumentBtnClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }

                    binding.emiPaymentSyncedIV.id -> {

                        mListener!!.onSyncBtnClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }

                    binding.emiPaymentMessageIB.id -> {

                        mListener!!.onEMIPaymentMessageBtnClicked(getItem(absoluteAdapterPosition).message)
                    }
                    binding.emiPaymentDeleteIB.id -> {

                        mListener!!.onDeleteEMIPaymentBtnClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }

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

        fun onEMIDocumentBtnClicked(emiPayment: EMIPayment, position: Int)
        fun onSyncBtnClicked(emiPayment: EMIPayment, position: Int)
        fun onEMIPaymentMessageBtnClicked(message: String)
        fun onDeleteEMIPaymentBtnClicked(emiPayment: EMIPayment, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

