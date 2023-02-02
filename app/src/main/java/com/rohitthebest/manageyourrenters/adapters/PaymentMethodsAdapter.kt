package com.rohitthebest.manageyourrenters.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.ItemPaymentMethodBinding
import com.rohitthebest.manageyourrenters.others.Constants

class PaymentMethodsAdapter :
    ListAdapter<PaymentMethod, PaymentMethodsAdapter.PaymentMethodsViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class PaymentMethodsViewHolder(val binding: ItemPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.rootL.setOnClickListener(this)
            binding.menuBtn.setOnClickListener(this)
        }

        fun setData(paymentMethod: PaymentMethod?) {

            paymentMethod?.let {
                binding.apply {
                    paymentMethodNameTV.text = it.paymentMethod

                    binding.root.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            if (it.isSynced) R.color.color_green else R.color.color_orange
                        )
                    )

                    val defaultPaymentMethods = listOf(
                        Constants.PAYMENT_METHOD_OTHER_KEY,
                        Constants.PAYMENT_METHOD_CASH_KEY,
                        Constants.PAYMENT_METHOD_DEBIT_CARD_KEY,
                        Constants.PAYMENT_METHOD_CREDIT_CARD_KEY
                    )

                    binding.menuBtn.isVisible = !defaultPaymentMethods.contains(paymentMethod.key)
                }
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {

                binding.menuBtn.id -> {

                    if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        mListener!!.onMenuBtnClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }
                }

                binding.rootL.id -> {

                    if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        mListener!!.onItemClick(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<PaymentMethod>() {

            override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(
                oldItem: PaymentMethod,
                newItem: PaymentMethod
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodsViewHolder {

        val binding =
            ItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PaymentMethodsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentMethodsViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(paymentMethod: PaymentMethod, position: Int)
        fun onMenuBtnClicked(paymentMethod: PaymentMethod, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

