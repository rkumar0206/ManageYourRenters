package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.Income
import com.rohitthebest.manageyourrenters.databinding.AdapterIncomeListItemBinding
import com.rohitthebest.manageyourrenters.utils.format

class IncomeRVAdapter(
    val linkedPaymentMethodsMap: Map<String, String> = emptyMap(),
) : ListAdapter<Income, IncomeRVAdapter.IncomeViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class IncomeViewHolder(val binding: AdapterIncomeListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

        }

        fun setData(income: Income?) {

            income?.let { myIncome ->

                binding.apply {

                    incomeValueTV.text = myIncome.income.format(2)
                    incomeSourceValueTV.text = myIncome.source

                    if (linkedPaymentMethodsMap.isNotEmpty()) {
                        lincomeLinkedPaymentMethodsTV.text = myIncome.getPaymentMethodString(
                            paymentMethodsMap = linkedPaymentMethodsMap
                        )
                    }

                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<Income>() {

            override fun areItemsTheSame(oldItem: Income, newItem: Income): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: Income, newItem: Income): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {

        val binding =
            AdapterIncomeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return IncomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(income: Income)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

