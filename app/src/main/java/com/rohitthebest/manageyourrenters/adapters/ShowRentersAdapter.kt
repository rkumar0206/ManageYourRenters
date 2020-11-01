package com.rohitthebest.manageyourrenters.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import kotlinx.android.synthetic.main.adapter_show_renter.view.*

class ShowRentersAdapter :
    ListAdapter<Renter, ShowRentersAdapter.RenterViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class RenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun setData(renter: Renter) {

            itemView.apply {

                adapterRenterNameTV.text = renter.name
                adapterRoomNumTV.text = renter.roomNumber
                adapterRenterTimeTV.text = "Added on : ${
                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                        renter.timeStamp
                    )
                }"
                adapterRenterMobileTV.text = renter.mobileNumber
                adapterRenterEmailTV.text = renter.emailId
                adapterDocumnetNameTV.text = if (renter.otherDocumentName != "") {

                    "${renter.otherDocumentName} : "
                } else {

                    "Other document : "
                }
                adapterRenterDocNumTV.text = renter.otherDocumentNumber
                adapterRenterAddressTV.text = renter.address

                if (renter.isSynced == context.getString(R.string.t)) {

                    itemView.adapterIsSyncedBtn.setImageResource(R.drawable.ic_baseline_sync_24_green)
                } else {

                    itemView.adapterIsSyncedBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                }
            }
        }

        init {

            itemView.setOnClickListener(this)
            itemView.adapterIsSyncedBtn.setOnClickListener(this)
            itemView.adapterRenterEditBtn.setOnClickListener(this)
            itemView.adapterRenterDeleteBtn.setOnClickListener(this)

        }

        override fun onClick(v: View?) {

            when (v?.id) {

                itemView.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onRenterClicked(getItem(absoluteAdapterPosition))
                    }
                }

                itemView.adapterRenterEditBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onEditClicked(getItem(absoluteAdapterPosition))
                    }

                }

                itemView.adapterRenterDeleteBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onDeleteClicked(getItem(absoluteAdapterPosition))
                    }
                }

                itemView.adapterIsSyncedBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onSyncButtonClicked(getItem(absoluteAdapterPosition))
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

        return RenterViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_show_renter, parent, false)
        )
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
    }

    fun setOnClickListener(listener: OnClickListener) {

        mListener = listener
    }

}