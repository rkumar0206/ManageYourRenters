package com.rohitthebest.manageyourrenters.adapters.borrowerAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.AdapterShowRenterBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.hide

class BorrowerAdapter :
    ListAdapter<Borrower, BorrowerAdapter.BorrowerViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class BorrowerViewHolder(val binding: AdapterShowRenterBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.renterAdapterCV.setOnClickListener(this)
            binding.adapterRenterEditBtn.setOnClickListener(this)
            binding.adapterRenterMobileTV.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            if (checkForNullability(absoluteAdapterPosition)) {

                when (v?.id) {

                    binding.renterAdapterCV.id -> {

                        mListener!!.onBorrowerClicked(getItem(absoluteAdapterPosition).key)
                    }

                    binding.adapterRenterEditBtn.id -> {

                        mListener!!.onMenuButtonClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }

                    binding.adapterRenterMobileTV.id -> {

                        mListener!!.onMobileNumberClicked(
                            getItem(absoluteAdapterPosition).mobileNumber,
                            binding.adapterRenterMobileTV
                        )
                    }
                }
            }

        }

        @SuppressLint("SetTextI18n")
        fun setData(borrower: Borrower?) {

            borrower?.let { b ->

                binding.apply {

                    textView23.hide()
                    adapterRenterAddressTV.hide()

                    if (b.totalDueAmount > 0) {

                        // using room num tv for showing total due
                        adapterRoomNumTV.changeTextColor(
                            binding.root.context,
                            R.color.color_orange
                        )
                    } else {

                        adapterRoomNumTV.changeTextColor(
                            binding.root.context,
                            R.color.color_green
                        )
                    }


                    // using room number textView for showing the due amount of the borrower
                    adapterRoomNumTV.text = "Total Due : ₹ ${b.totalDueAmount}"

                    adapterRenterNameTV.text = b.name
                    adapterRenterEmailTV.text = b.emailId
                    adapterDocumnetNameTV.text = if (b.otherDocumentName != "") {

                        "${b.otherDocumentName} : "
                    } else {

                        "Other document : "
                    }
                    adapterRenterDocNumTV.text = b.otherDocumentNumber
                    adapterRenterMobileTV.text = b.mobileNumber
                    adapterRenterTimeTV.text = "Added on : ${
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            b.created
                        )
                    }"

                    if (b.isSynced) {

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
                }
            }
        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
                    mListener != null
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

        fun onBorrowerClicked(borrowerKey: String)
        fun onMenuButtonClicked(borrower: Borrower, position: Int)
        fun onMobileNumberClicked(mobileNumber: String, view: View)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

