package com.rohitthebest.manageyourrenters.adapters

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.data.StyleType
import com.rohitthebest.manageyourrenters.data.WhatsNew
import com.rohitthebest.manageyourrenters.databinding.AdapterWhatsNewBinding
import com.rohitthebest.manageyourrenters.utils.applyStyles
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show

class WhatsNewAdapter :
    ListAdapter<WhatsNew, WhatsNewAdapter.WhatsNewViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class WhatsNewViewHolder(val binding: AdapterWhatsNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.whatsNewTV.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.whatsNewExpandBtn.setOnClickListener {
                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(whatsNew: WhatsNew?) {

            whatsNew?.let { new ->

                binding.apply {

                    if (new.styleType != null && new.styleType == StyleType.HTML) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            whatsNewTV.text = Html.fromHtml(
                                new.feature, HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                        } else {
                            whatsNewTV.text = Html.fromHtml(
                                new.feature
                            )
                        }

                    } else {
                        whatsNewTV.applyStyles(
                            new.feature, new.textStyle ?: ""
                        )
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

