package com.rohitthebest.manageyourrenters.adapters.borrowerAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.AdapterShowRenterBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import kotlinx.android.synthetic.main.adapter_show_renter.view.*

class BorrowerAdapter :
    ListAdapter<Borrower, BorrowerAdapter.BorrowerViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class BorrowerViewHolder(val binding: AdapterShowRenterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

        }

        @SuppressLint("SetTextI18n")
        fun setData(borrower: Borrower?) {

            borrower?.let { borrower ->

                binding.apply {

                    textView23.hide()
                    adapterRenterAddressTV.hide()

                    // using room number textView for showing the due amount of the borrower
                    adapterRoomNumTV.text = "Total Due : ${borrower.totalDueAmount}"

                    adapterRenterNameTV.text = borrower.name
                    adapterRenterEmailTV.text = borrower.emailId
                    adapterDocumnetNameTV.text = borrower.otherDocumentName
                    adapterRenterDocNumTV.text = borrower.otherDocumentNumber
                    adapterRenterMobileTV.text = borrower.mobileNumber
                    adapterRenterTimeTV.text = "Added on : ${
                        WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                            borrower.created
                        )
                    }"

                    if (borrower.totalDueAmount > 0.0) {

                        root.strokeColor =
                            ContextCompat.getColor(root.context, R.color.color_orange)
                    } else {

                        root.strokeColor =
                            ContextCompat.getColor(root.context, R.color.colorGrey)
                    }

                    if (borrower.isSynced) {

                        itemView.adapterIsSyncedBtn.setImageResource(R.drawable.ic_baseline_sync_24_green)
                    } else {

                        itemView.adapterIsSyncedBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                    }
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<Borrower>() {

            override fun areItemsTheSame(oldItem: Borrower, newItem: Borrower): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Borrower, newItem: Borrower): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowerViewHolder {

        val binding =
            AdapterShowRenterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BorrowerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BorrowerViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(borrower: Borrower)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

