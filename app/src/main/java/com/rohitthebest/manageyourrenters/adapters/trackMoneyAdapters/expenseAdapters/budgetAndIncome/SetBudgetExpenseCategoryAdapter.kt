package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.AdapterSetBudgetExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.changeTextColor
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

                    mListener!!.onAddBudgetClicked(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }

            binding.budgetMenuBtn.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onBudgetMenuBtnClicked(
                        getItem(absoluteAdapterPosition),
                        binding.budgetMenuBtn,
                        absoluteAdapterPosition
                    )
                }
            }

            binding.budgetSyncBtn.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onBudgetSyncBtnClicked(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }

            binding.root.setOnClickListener {
                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    mListener!!.onItemClicked(
                        getItem(absoluteAdapterPosition).expenseCategoryKey,
                        getItem(absoluteAdapterPosition).budgetLimit != 0.0
                    )
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
                        budgetMenuBtn.hide()
                        budgetSyncBtn.hide()
                    } else {
                        budgetAddLimitBtn.hide()
                        budgetLimitTV.show()
                        budgetMenuBtn.show()

                        budgetSyncBtn.isVisible = !myBudget.isSynced

                        budgetLimitTV.text = myBudget.budgetLimit.format(2)

                        budgetProgressBar.max = myBudget.budgetLimit.toInt()

                    }

                    val progressInPercent =
                        ((myBudget.currentExpenseAmount / myBudget.budgetLimit) * 100).toInt()

                    when {

                        (progressInPercent in 0..35) -> {

                            budgetCurrent.changeTextColor(binding.root.context, R.color.color_green)
                            budgetProgressBar.progressTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.color_green
                                )
                            )
                        }

                        (progressInPercent in 36..68) -> {

                            budgetCurrent.changeTextColor(
                                binding.root.context,
                                R.color.color_yellow
                            )
                            budgetProgressBar.progressTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.color_yellow
                                )
                            )
                        }

                        progressInPercent > 68 -> {

                            budgetCurrent.changeTextColor(
                                binding.root.context,
                                R.color.color_Red
                            )
                            budgetProgressBar.progressTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.color_Red
                                )
                            )
                        }
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

        fun onItemClicked(expenseCategoryKey: String, isBudgetLimitAdded: Boolean)
        fun onAddBudgetClicked(budget: Budget, position: Int)
        fun onBudgetMenuBtnClicked(budget: Budget, view: View, position: Int)

        fun onBudgetSyncBtnClicked(budget: Budget, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

