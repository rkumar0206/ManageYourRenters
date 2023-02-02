package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.databinding.ItemExpenseBinding
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class ExpenseAdapter(
    val categoryName: String = "",
    val paymentMethodsMap: Map<String, String> = emptyMap(),
    val isCalledFromExpenseBottomSheetFragment: Boolean = false
) :
    ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ExpenseViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.rootL.setOnClickListener {

                if (checkForNullability()) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.expenseMenuBtn.setOnClickListener {

                if (checkForNullability()) {

                    mListener!!.onMenuBtnClicked(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }

        }

        fun setData(expense: Expense?) {

            expense?.let { exp ->

                binding.apply {

                    val prettyTime = PrettyTime()
                    expenseDateTV.text = prettyTime.format(Date(exp.created))

                    expenseAmountTV.text = exp.amount.toString()

                    if (exp.isSynced) {

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

                    if (exp.spentOn.isValid()) {

                        expenseSpentOnTV.show()
                        //expenseSpentOnTV.text = setSpannableText("Spent on :- ${exp.spentOn}")
                        expenseSpentOnTV.text = exp.spentOn

                    } else {

                        if (categoryName.isValid()) {

                            expenseSpentOnTV.show()
                            expenseSpentOnTV.text = categoryName
                        } else {

                            expenseSpentOnTV.hide()
                        }
                    }

                    if (isCalledFromExpenseBottomSheetFragment) {
                        // hide menu button
                        expenseMenuBtn.hide()
                    }

                    if (paymentMethodsMap.isNotEmpty())
                        expensePaymentMethodsTV.text = exp.getPaymentMethodString(paymentMethodsMap)
                    else {
                        expensePaymentMethodsTV.hide()
                    }
                }
            }
        }

/*
        private fun setSpannableText(s: String): CharSequence {

            val span = StyleSpan(Typeface.BOLD_ITALIC)

            val spannableStringBuilder = SpannableStringBuilder(s)

           spannableStringBuilder.setSpan(
                span,
                0,
                11,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return spannableStringBuilder
        }
*/

        private fun checkForNullability(): Boolean {

            return absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null
        }

    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<Expense>() {

            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {

        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(expense: Expense)
        fun onMenuBtnClicked(expense: Expense, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

