package com.rohitthebest.manageyourrenters.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.WhatsNew
import com.rohitthebest.manageyourrenters.databinding.AdapterWhatsNewBinding
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show

class WhatsNewAdapter :
    ListAdapter<WhatsNew, WhatsNewAdapter.WhatsNewViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class WhatsNewViewHolder(val binding: AdapterWhatsNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.root.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(whatsNew: WhatsNew?) {

            whatsNew?.let { new ->

                binding.apply {

                    if (new.feature.contains("-heading")) {

                        whatsNewTV.changeTextColor(binding.root.context, R.color.primaryTextColor)
                        whatsNewTV.setTypeface(null, Typeface.BOLD)
                        whatsNewTV.textSize = 20f
                        whatsNewTV.text = new.feature.replace("-heading", "")
                    } else {

                        whatsNewTV.changeTextColor(binding.root.context, R.color.secondaryTextColor)
                        whatsNewTV.setTypeface(null, Typeface.NORMAL)
                        whatsNewTV.textSize = 16f
                        whatsNewTV.text = new.feature
                    }

                    if (new.image.isValid()) {

                        whatsNewExpandBtn.show()
                    } else {
                        whatsNewExpandBtn.hide()
                    }
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<WhatsNew>() {

            override fun areItemsTheSame(oldItem: WhatsNew, newItem: WhatsNew): Boolean =
                oldItem.feature == newItem.feature

            override fun areContentsTheSame(oldItem: WhatsNew, newItem: WhatsNew): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatsNewViewHolder {

        val binding =
            AdapterWhatsNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WhatsNewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WhatsNewViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(whatsNew: WhatsNew)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

