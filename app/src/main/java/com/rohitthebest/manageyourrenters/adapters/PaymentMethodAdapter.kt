package com.rohitthebest.manageyourrenters.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.ItemPaymentMethodBinding

class PaymentMethodAdapter :
    ListAdapter<PaymentMethod, PaymentMethodAdapter.PaymentMethodViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class PaymentMethodViewHolder(val binding: ItemPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

        }

        fun setData(paymentMethod: PaymentMethod?) {

            paymentMethod?.let {

                binding.apply {


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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {

        val binding =
            ItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PaymentMethodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(paymentMethod: PaymentMethod)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

