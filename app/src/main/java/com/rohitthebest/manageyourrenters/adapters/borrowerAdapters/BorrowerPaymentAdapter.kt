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

class BorrowerPaymentAdapter :
    ListAdapter<BorrowerPayment, BorrowerPaymentAdapter.BorrowerPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class BorrowerPaymentViewHolder(val binding: AdapterBorrowerPaymentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.adapterBorrowerPaymentMCV.setOnClickListener(this)
            binding.deleteBtn.setOnClickListener(this)
            binding.syncBtn.setOnClickListener(this)
            binding.messageBtn.setOnClickListener(this)
            binding.showDocumentBtn.setOnClickListener(this)
            binding.interestBtn.setOnClickListener(this)
            binding.editBorrowerPaymentBtn.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun setData(borrowerPayment: BorrowerPayment?) {

            borrowerPayment?.let { payment ->

                binding.apply {

                    borrowedAmountTV.text =
                        "Borrowed : ${payment.currencySymbol} ${payment.amountTakenOnRent}"
                    paidAmountTV.text =
                        "Paid : ${payment.currencySymbol} ${(payment.amountTakenOnRent - payment.dueLeftAmount)}"

                    dueAmountTV.text = "Due : ${payment.currencySymbol} ${payment.dueLeftAmount}"

                    borrowedPaymentDateTV.text =
                        "Borrowed on : " + WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                            payment.created
                        )

                    if (!payment.isDueCleared) {

                        binding.adapterBorrowerPaymentMCV.strokeColor =
                            ContextCompat.getColor(binding.root.context, R.color.color_orange)
                    } else {

                        binding.adapterBorrowerPaymentMCV.strokeColor =
                            ContextCompat.getColor(binding.root.context, R.color.color_green)
                    }
                }
            }
        }

        override fun onClick(v: View?) {

            if (checkForNullability()) {

                when (v?.id) {

                    binding.adapterBorrowerPaymentMCV.id -> {

                        mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                    }

                    binding.deleteBtn.id -> {

                        mListener!!.onDeleteBtnClick(getItem(absoluteAdapterPosition))
                    }
                    binding.syncBtn.id -> {

                        mListener!!.onSyncBtnClick(getItem(absoluteAdapterPosition))
                    }

                    binding.showDocumentBtn.id -> {

                        mListener!!.onShowDocumentBtnClick(getItem(absoluteAdapterPosition))
                    }
                    binding.messageBtn.id -> {

                        mListener!!.onShowMessageBtnClick(getItem(absoluteAdapterPosition).messageOrNote)
                    }
                    binding.interestBtn.id -> {

                        mListener!!.onInterestBtnClick(getItem(absoluteAdapterPosition))
                    }
                    binding.editBorrowerPaymentBtn.id -> {

                        mListener!!.onEditBtnClick(getItem(absoluteAdapterPosition))
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

        fun onItemClick(borrowerPayment: BorrowerPayment)
        fun onDeleteBtnClick(borrowerPayment: BorrowerPayment)
        fun onSyncBtnClick(borrowerPayment: BorrowerPayment)
        fun onShowMessageBtnClick(message: String)
        fun onShowDocumentBtnClick(borrowerPayment: BorrowerPayment)
        fun onInterestBtnClick(borrowerPayment: BorrowerPayment)
        fun onEditBtnClick(borrowerPayment: BorrowerPayment)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

