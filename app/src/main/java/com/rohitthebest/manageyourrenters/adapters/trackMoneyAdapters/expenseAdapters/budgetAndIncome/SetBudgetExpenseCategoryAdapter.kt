package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.AdapterSetBudgetExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show

class SetBudgetExpenseCategoryAdapter :
    ListAdapter<Budget, SetBudgetExpenseCategoryAdapter.SetBudgetViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class SetBudgetViewHolder(val binding: AdapterSetBudgetExpenseCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.budgetAddLimitBtn.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onAddBudgetClicked(getItem(absoluteAdapterPosition))
                }
            }

            binding.budgetLimitTV.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onAddBudgetClicked(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(budget: Budget?) {

            budget?.let { myBudget ->

                binding.apply {

                    budgetCategoryNameTV.text = myBudget.categoryName
                    budgetCurrent.text = myBudget.currentExpenseAmount.format(2)

                    if (myBudget.categoryImageUrl.isValid()) {

                        Functions.setImageToImageViewUsingGlide(
                            binding.root.context,
                            categoryImage,
                            myBudget.categoryImageUrl,
                            {},
                            {}
                        )
                    } else {

                        Glide.with(binding.root)
                            .load(R.drawable.expense_shortcut_icon)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(categoryImage)
                    }

                    if (myBudget.budgetLimit == 0.0) {

                        // no budget is set
                        budgetAddLimitBtn.show()
                        budgetLimitTV.hide()
                    } else {
                        budgetAddLimitBtn.hide()
                        budgetLimitTV.show()

                        budgetLimitTV.text = myBudget.budgetLimit.format(2)

                        budgetProgressBar.max = myBudget.budgetLimit.toInt()

                    }

                    if (myBudget.currentExpenseAmount > myBudget.budgetLimit) {
                        budgetProgressBar.progress = myBudget.budgetLimit.toInt()
                    } else {
                        budgetProgressBar.progress = myBudget.currentExpenseAmount.toInt()
                    }

                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<Budget>() {

            override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetBudgetViewHolder {

        val binding = AdapterSetBudgetExpenseCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SetBudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetBudgetViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onAddBudgetClicked(budget: Budget)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

