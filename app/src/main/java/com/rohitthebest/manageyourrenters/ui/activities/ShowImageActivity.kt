package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.ImageHeaderParser
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.WhatsNew
import com.rohitthebest.manageyourrenters.databinding.ActivityShowImageBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowImageBinding
    private var imageUrl: String = ""
    private var imageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ManageYourRenters)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        imageName = System.currentTimeMillis().toString()

        val tag =
            intent.getStringExtra(Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION2)

        if (tag.isValid()) {

            when (tag) {

                getString(R.string.whats_new) -> {

                    handleImageReceivedFromWhatsNew()
                }

                else -> {

                    handleImageReceivedFromSupportingDocument()
                }
            }
        } else {
            handleImageReceivedFromSupportingDocument()
        }


        binding.downloadImageFAB.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.download))
                .setMessage(getString(R.string.download_message))
                .setPositiveButton(getString(R.string.download)) { dialog, _ ->

                    downloadFileFromUrl(
                        this,
                        imageUrl,
                        imageName
                    )
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()

        }

    }

    private fun handleImageReceivedFromSupportingDocument() {

        val document =
            intent.getStringExtra(Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION)
                ?.convertJsonToObject(SupportingDocument::class.java)

        imageUrl = document?.documentUrl ?: ""
        imageName = document?.documentName ?: System.currentTimeMillis().toString()

        binding.imageIV.setImage()
    }

    private fun handleImageReceivedFromWhatsNew() {

        binding.progressBar.show()

        val whatsNew =
            intent.getStringExtra(Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION)
                ?.convertJsonToObject(WhatsNew::class.java)

        imageUrl = whatsNew?.image ?: ""

        if (whatsNew?.imageType != null && whatsNew.imageType != ImageHeaderParser.ImageType.GIF) {
            binding.imageIV.setImage()
        } else {

            Glide.with(this)
                .asGif()
                .load(imageUrl)
                .error(R.drawable.ic_outline_error_outline_24)
                .listener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.hide()
                        binding.imageIV.setImageResource(R.drawable.ic_outline_error_outline_24)
                        return false
                    }

                    override fun onResourceReady(
                        resource: GifDrawable?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {

                        binding.progressBar.hide()
                        return false
                    }
                })
                .fitCenter()
                .into(binding.imageIV)
        }

    }

    private fun ImageView.setImage() {

        if (!imageUrl.isValid()) onBackPressedDispatcher.onBackPressed()

        binding.progressBar.show()

        setImageToImageViewUsingGlide(
            applicationContext,
            this,
            imageUrl,
            {
                binding.imageIV.setImageResource(R.drawable.ic_outline_error_outline_24)
            },
            {
                binding.progressBar.hide()
            }
        )
    }
}