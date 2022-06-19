package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterEmiPaymentBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class EMIPaymentAdapter :
    ListAdapter<EMIPayment, EMIPaymentAdapter.EMIPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class EMIPaymentViewHolder(val binding: AdapterEmiPaymentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.emiPaymentAdapterRoot.setOnClickListener(this)
            binding.emiPaymentMenuIB.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun setData(emiPayment: EMIPayment?) {

            emiPayment?.let { payment ->

                binding.apply {

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

                        root.setCardBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.color_green
                            )
                        )

                    } else {

                        root.setCardBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.color_orange
                            )
                        )
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

                    binding.emiPaymentAdapterRoot.id -> {

                        mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                    }

                    binding.emiPaymentMenuIB.id -> {

                        mListener!!.onMenuButtonBtnClicked(
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

        fun onItemClick(emiPayment: EMIPayment)
        fun onMenuButtonBtnClicked(emiPayment: EMIPayment, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

