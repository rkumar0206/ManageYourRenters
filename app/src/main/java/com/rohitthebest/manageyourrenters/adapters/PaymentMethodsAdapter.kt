package com.rohitthebest.manageyourrenters.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.ItemPaymentMethodBinding

class PaymentMethodsAdapter :
    ListAdapter<PaymentMethod, PaymentMethodsAdapter.PaymentMethodsViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class PaymentMethodsViewHolder(val binding: ItemPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.root.setOnClickListener(this)
            binding.editBtn.setOnClickListener(this)
            binding.syncBtn.setOnClickListener(this)
        }

        fun setData(paymentMethod: PaymentMethod?) {

            paymentMethod?.let {

                binding.apply {

                    paymentMethodNameTV.text = it.paymentMethod
                    syncBtn.isVisible = !it.isSynced
                }
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {

                binding.editBtn.id -> {

                    if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        mListener!!.onEditBtnClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }
                }

                binding.root.id -> {

                    if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        mListener!!.onItemClick(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }
                }

                binding.syncBtn.id -> {
                    if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        mListener!!.onSyncBtnClicked(
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
        fun onEditBtnClicked(paymentMethod: PaymentMethod, position: Int)
        fun onSyncBtnClicked(paymentMethod: PaymentMethod, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

