package com.rohitthebest.manageyourrenters.adapters.borrowerAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.strikeThrough

class BorrowerPaymentAdapter :
    ListAdapter<BorrowerPayment, BorrowerPaymentAdapter.BorrowerPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class BorrowerPaymentViewHolder(val binding: AdapterBorrowerPaymentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.adapterBorrowerPaymentMCV.setOnClickListener(this)
            binding.messageBtn.setOnClickListener(this)
            binding.interestBtn.setOnClickListener(this)
            binding.menuBtnForBorrowerPayment.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun setData(borrowerPayment: BorrowerPayment?) {

            val context = binding.root.context
            borrowerPayment?.let { payment ->
                binding.apply {
                    borrowedAmountTV.text =
                        context.getString(
                            R.string.borrowed_currency_symbol_amount_taken,
                            payment.currencySymbol,
                            payment.amountTakenOnRent.format(2)
                        )
                    paidAmountTV.text =
                        context.getString(
                            R.string.paid_currency_symbol_amount,
                            payment.currencySymbol,
                            (payment.totalAmountPaid).format(2)
                        )

                    if (payment.isInterestAdded && payment.interest != null) {

                        dueAmountTV.textSize = 16.0f

                        dueAmountTV.text = context
                            .getString(
                                R.string.due_currency_symbol_amount_interest,
                                payment.currencySymbol,
                                payment.dueLeftAmount.format(2),
                                payment.totalInterestTillNow.format(2)
                            )
                    } else {

                        dueAmountTV.text = context
                            .getString(
                                R.string.due_currency_symbol_amount,
                                payment.currencySymbol,
                                payment.dueLeftAmount.format(2)
                            )
                    }


                    if (payment.isDueCleared) {

                        binding.borrowedAmountTV.strikeThrough()
                        binding.paidAmountTV.strikeThrough()
                        binding.dueAmountTV.strikeThrough()
                    } else {

/*
                        if (payment.isInterestAdded && payment.interest != null) {

                            val interestCalculatorFields = InterestCalculatorFields(
                                0L,
                                payment.amountTakenOnRent,
                                payment.interest!!,
                                calculateNumberOfDays(payment.created, System.currentTimeMillis())
                            )

                            val interestAndAmount = calculateInterestAndAmount(
                                interestCalculatorFields
                            )

                            if (interestAndAmount.first > 0.0) {

                                dueAmountTV.textSize = 16.0f

                                if (payment.dueLeftAmount == payment.amountTakenOnRent) {
                                    dueAmountTV.text = context
                                        .getString(
                                            R.string.due_currency_symbol_amount_interest,
                                            payment.currencySymbol,
                                            payment.dueLeftAmount.format(2),
                                            interestAndAmount.first.format(2)
                                        )

                                } else {

                                    dueAmountTV.text = context
                                        .getString(
                                            R.string.due_currency_symbol_amount_interest,
                                            payment.currencySymbol,
                                            payment.dueLeftAmount.format(2),
                                            interestAndAmount.first.format(2)
                                        )

                                    // showing paid amount with interest included
                                    paidAmountTV.text =
                                        context.getString(
                                            R.string.paid_currency_symbol_amount,
                                            payment.currencySymbol,
                                            ((payment.amountTakenOnRent + interestAndAmount.first) - payment.dueLeftAmount).format(
                                                2
                                            )
                                        )

                                }

                            }
                        }
*/

                    }

                    if (payment.isSynced) {

                        binding.root.setCardBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.color_green
                            )
                        )
                    } else {

                        binding.root.setCardBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.color_orange
                            )
                        )
                    }

                    if (payment.dueLeftAmount <= 0.0) {

                        dueAmountTV.changeTextColor(context, R.color.color_green)
                    } else {

                        dueAmountTV.changeTextColor(context, R.color.color_orange)
                    }

                    borrowedPaymentDateTV.text = context.getString(
                        R.string.borrowed_on,
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            payment.created
                        )
                    )

                }
            }
        }

        override fun onClick(v: View?) {

            if (checkForNullability()) {

                when (v?.id) {

                    binding.adapterBorrowerPaymentMCV.id -> {

                        mListener!!.onItemClick(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }

                    binding.messageBtn.id -> {

                        mListener!!.onShowMessageBtnClick(getItem(absoluteAdapterPosition).messageOrNote)
                    }
                    binding.interestBtn.id -> {

                        mListener!!.onInterestBtnClick(getItem(absoluteAdapterPosition))
                    }
                    binding.menuBtnForBorrowerPayment.id -> {

                        mListener!!.onMenuBtnClick(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }
                }
            }
        }

        private fun checkForNullability(): Boolean =
            mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<BorrowerPayment>() {

            override fun areItemsTheSame(
                oldItem: BorrowerPayment,
                newItem: BorrowerPayment
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: BorrowerPayment,
                newItem: BorrowerPayment
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowerPaymentViewHolder {

        val binding = AdapterBorrowerPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return BorrowerPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BorrowerPaymentViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(borrowerPayment: BorrowerPayment, position: Int)
        fun onShowMessageBtnClick(message: String)
        fun onInterestBtnClick(borrowerPayment: BorrowerPayment)
        fun onMenuBtnClick(borrowerPayment: BorrowerPayment, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

