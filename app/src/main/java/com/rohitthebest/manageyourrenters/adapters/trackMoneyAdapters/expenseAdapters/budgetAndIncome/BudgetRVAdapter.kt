package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.AdapterBudgetBinding
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.isValid

private const val TAG = "BudgetRVAdapter"

class BudgetRVAdapter : ListAdapter<Budget, BudgetRVAdapter.BudgetViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class BudgetViewHolder(val binding: AdapterBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.root.setOnClickListener {

                if (isPositionAndMlistenerValid()) {
                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.baMenuBtn.setOnClickListener {
                if (isPositionAndMlistenerValid()) {
                    mListener!!.onMenuBtnClick(
                        getItem(absoluteAdapterPosition),
                        binding.baMenuBtn,
                        absoluteAdapterPosition
                    )
                }
            }
        }

        private fun isPositionAndMlistenerValid(): Boolean {
            return mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION
        }

        fun setData(budget: Budget?) {

            budget?.let { myBudget ->

                binding.apply {

                    baCategoryNameTV.text = myBudget.categoryName

                    if (myBudget.categoryImageUrl.isValid()) {

                        Functions.setImageToImageViewUsingGlide(
                            binding.root.context,
                            baCategoryImageIV,
                            myBudget.categoryImageUrl,
                            {},
                            {}
                        )
                    } else {

                        Glide.with(binding.root)
                            .load(R.drawable.expense_shortcut_icon)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(baCategoryImageIV)
                    }

                    baSpentVsLimitTV.text = binding.root.context.getString(
                        R.string.spentVsLimit,
                        myBudget.currentExpenseAmount.format(2),
                        myBudget.budgetLimit.format(2)
                    )

                    val numberOfDaysLeftInMonth =
                        WorkingWithDateAndTime.getNumberOfDaysLeftInAnyMonth(
                            myBudget.month, myBudget.year
                        )

                    Log.d(TAG, "setData: numberOfDaysLeftInMonth: $numberOfDaysLeftInMonth")

                    var perDayExpense = if (numberOfDaysLeftInMonth != 0) {
                        (myBudget.budgetLimit - myBudget.currentExpenseAmount) / numberOfDaysLeftInMonth
                    } else {
                        0.0
                    }

                    if (perDayExpense < 0) perDayExpense = 0.0

                    baPerDayExpenseTV.text = binding.root.context.getString(
                        R.string.budgetPerDay, perDayExpense.format(2),
                        numberOfDaysLeftInMonth.toString()
                    )

                    val progressInPercent =
                        ((myBudget.currentExpenseAmount / myBudget.budgetLimit) * 100).toInt()

                    percentTV.text = if (progressInPercent > 100.0) {
                        binding.root.context.getString(R.string._100)
                    } else {
                        "$progressInPercent%"
                    }

                    changeProgressColorsByProgressPercent(progressInPercent)

                    baProgressBar.max = myBudget.budgetLimit.toInt()

                    if (myBudget.currentExpenseAmount > myBudget.budgetLimit) {
                        baProgressBar.progress = myBudget.budgetLimit.toInt()
                    } else {
                        baProgressBar.progress = myBudget.currentExpenseAmount.toInt()
                    }
                }
            }
        }

        private fun changeProgressColorsByProgressPercent(progressInPercent: Int) {

            when {

                (progressInPercent in 0..35) -> {
                    val colorGreen = ContextCompat.getColor(
                        binding.root.context,
                        R.color.color_green
                    )
                    binding.baProgressBar.progressTintList = ColorStateList.valueOf(
                        colorGreen
                    )
                    binding.percentMCV.strokeColor = colorGreen
                }

                (progressInPercent in 36..68) -> {
                    val colorYellow = ContextCompat.getColor(
                        binding.root.context,
                        R.color.color_yellow
                    )
                    binding.baProgressBar.progressTintList = ColorStateList.valueOf(
                        colorYellow
                    )
                    binding.percentMCV.strokeColor = colorYellow
                }

                else -> {
                    val colorRed = ContextCompat.getColor(
                        binding.root.context,
                        R.color.color_Red
                    )
                    binding.baProgressBar.progressTintList = ColorStateList.valueOf(
                        colorRed
                    )

                    binding.percentMCV.strokeColor = colorRed
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {

        val binding =
            AdapterBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(budget: Budget)
        fun onMenuBtnClick(budget: Budget, view: View, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

