package com.rohitthebest.manageyourrenters.adapters

import android.annotation.SuppressLint
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import kotlinx.android.synthetic.main.adapter_show_renter.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShowRentersAdapter :
    ListAdapter<Renter, ShowRentersAdapter.RenterViewHolder>(DiffUtilCallback()) {

    private var mListener : OnClickListener? = null

    inner class RenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnCreateContextMenuListener {

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
                adapterDocumnetNameTV.text = renter.otherDocumentName
                adapterRenterDocNumTV.text = renter.otherDocumentNumber
            }
        }

        init {

            itemView.setOnClickListener(this)
            itemView.adapterIsSyncedBtn.setOnClickListener(this)
            itemView.adapterExtendRenterInfoBtn.setOnClickListener(this)

            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                itemView.id -> {

                    if(checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onRenterClicked(getItem(absoluteAdapterPosition))
                    }
                }

                itemView.adapterExtendRenterInfoBtn.id -> {

                    val interpolator = OvershootInterpolator()

                    if (itemView.extendedInfoCL.visibility != View.VISIBLE) {

                        itemView.adapterExtendRenterInfoBtn.animate()
                            .setInterpolator(interpolator)
                            .rotation(180f).setDuration(200).start()

                        itemView.extendedInfoCL.show()
                        itemView.extendedInfoCL.animate().translationY(0f).alpha(1f)
                            .setInterpolator(interpolator)
                            .setDuration(350).start()

                    } else {

                        itemView.adapterExtendRenterInfoBtn.animate()
                            .setInterpolator(interpolator)
                            .rotation(0f).setDuration(200).start()

                        itemView.extendedInfoCL.animate().translationY(-140f).alpha(0f)
                            .setInterpolator(interpolator).setDuration(350).start()

                        GlobalScope.launch {
                            delay(100)

                            itemView.extendedInfoCL.hide()
                        }
                    }
                }

                itemView.adapterIsSyncedBtn.id -> {

                    if(checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onSyncButtonClicked(getItem(absoluteAdapterPosition))
                    }
                }
            }

        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
                    mListener != null
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {

            val delete = menu?.add(1,1,1,"Delete")
            val edit = menu?.add(1,2,2,"Edit")

            delete?.setOnMenuItemClickListener {

                if(checkForNullability(absoluteAdapterPosition)) {

                    mListener!!.onDeleteClicked(getItem(absoluteAdapterPosition))
                }
                true
            }

            edit?.setOnMenuItemClickListener {

                if(checkForNullability(absoluteAdapterPosition)) {

                    mListener!!.onEditClicked(getItem(absoluteAdapterPosition))
                }

                true
            }
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

        fun onRenterClicked(renter : Renter)
        fun onSyncButtonClicked(renter : Renter)
        //fun onExtendInfoButtonClicked(renter : Renter)
        fun onDeleteClicked(renter : Renter)
        fun onEditClicked(renter : Renter)
    }

    fun setOnClickListener(listener : OnClickListener) {

        mListener = listener
    }

}