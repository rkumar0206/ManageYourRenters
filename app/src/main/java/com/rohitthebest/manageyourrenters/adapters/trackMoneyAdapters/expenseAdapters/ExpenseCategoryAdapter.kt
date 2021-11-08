package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.ItemExpenseCategoryBinding

class ExpenseCategoryAdapter :
    ListAdapter<ExpenseCategory, ExpenseCategoryAdapter.ExpenseCategoryViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ExpenseCategoryViewHolder(val binding: ItemExpenseCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

        }

        fun setData(expenseCategory: ExpenseCategory?) {

            expenseCategory?.let { expenseCat ->

                binding.apply {

                    expenseCategoryNameTV.text = expenseCat.categoryName
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<ExpenseCategory>() {

            override fun areItemsTheSame(
                oldItem: ExpenseCategory,
                newItem: ExpenseCategory
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ExpenseCategory,
                newItem: ExpenseCategory
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseCategoryViewHolder {

        val binding =
            ItemExpenseCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ExpenseCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseCategoryViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(expenseCategory: ExpenseCategory)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

