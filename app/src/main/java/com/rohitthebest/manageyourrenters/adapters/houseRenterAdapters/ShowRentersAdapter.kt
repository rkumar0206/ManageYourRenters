package com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.AdapterShowRenterBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class ShowRentersAdapter :
    ListAdapter<Renter, ShowRentersAdapter.RenterViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class RenterViewHolder(val binding: AdapterShowRenterBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun setData(renter: Renter) {

            binding.apply {

                if (renter.dueOrAdvanceAmount < 0.0) {

                    binding.renterAdapterCV.strokeColor =
                        ContextCompat.getColor(binding.root.context, R.color.color_orange)
                } else {

                    binding.renterAdapterCV.strokeColor =
                        ContextCompat.getColor(binding.root.context, R.color.colorGrey)
                }

                binding.adapterRenterNameTV.text = renter.name
                binding.adapterRoomNumTV.text = renter.roomNumber
                binding.adapterRenterTimeTV.text = "Added on : ${
                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                        renter.timeStamp
                    )
                }"
                binding.adapterRenterMobileTV.text = renter.mobileNumber
                binding.adapterRenterEmailTV.text = renter.emailId
                binding.adapterDocumnetNameTV.text = if (renter.otherDocumentName != "") {

                    "${renter.otherDocumentName} : "
                } else {

                    "Other document : "
                }
                binding.adapterRenterDocNumTV.text = renter.otherDocumentNumber
                binding.adapterRenterAddressTV.text = renter.address

                if (renter.isSynced == binding.root.context.getString(R.string.t)) {

                    binding.adapterIsSyncedBtn.setImageResource(R.drawable.ic_baseline_sync_24_green)
                } else {

                    binding.adapterIsSyncedBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                }
            }
        }

        init {

            binding.root.setOnClickListener(this)
            binding.adapterIsSyncedBtn.setOnClickListener(this)
            binding.adapterRenterEditBtn.setOnClickListener(this)
            binding.adapterRenterDeleteBtn.setOnClickListener(this)
            binding.adapterRenterMobileTV.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                binding.root.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onRenterClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.adapterRenterEditBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onEditClicked(getItem(absoluteAdapterPosition))
                    }

                }

                binding.adapterRenterDeleteBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onDeleteClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.adapterIsSyncedBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onSyncButtonClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.adapterRenterMobileTV.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onMobileNumberClicked(getItem(absoluteAdapterPosition).mobileNumber)
                    }
                }
            }

        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
                    mListener != null
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Renter>() {

        override fun areItemsTheSame(oldItem: Renter, newItem: Renter): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Renter, newItem: Renter): Boolean {

            return oldItem.id == newItem.id
                    && oldItem.timeStamp == newItem.timeStamp
                    && oldItem.key == newItem.key
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenterViewHolder {

        val binding =
            AdapterShowRenterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return RenterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RenterViewHolder, position: Int) {

        getItem(position)?.let {

            holder.setData(it)
        }
    }

    interface OnClickListener {

        fun onRenterClicked(renter: Renter)
        fun onSyncButtonClicked(renter: Renter)

        //fun onExtendInfoButtonClicked(renter : Renter)
        fun onDeleteClicked(renter: Renter)
        fun onEditClicked(renter: Renter)
        fun onMobileNumberClicked(mobileNumber: String)
    }

    fun setOnClickListener(listener: OnClickListener) {

        mListener = listener
    }

}