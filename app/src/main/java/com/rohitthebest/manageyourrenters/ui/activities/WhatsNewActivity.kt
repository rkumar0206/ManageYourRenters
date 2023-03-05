package com.rohitthebest.manageyourrenters.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.WhatsNewAdapter
import com.rohitthebest.manageyourrenters.data.AppUpdate
import com.rohitthebest.manageyourrenters.data.WhatsNew
import com.rohitthebest.manageyourrenters.databinding.ActivityWhatsNewBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.APP_VERSION
import com.rohitthebest.manageyourrenters.others.Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION2
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.openLinkInBrowser
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage

class WhatsNewActivity : AppCompatActivity(), WhatsNewAdapter.OnClickListener {

    private lateinit var binding: ActivityWhatsNewBinding

    private var appUpdate: AppUpdate? = null
    private lateinit var whatsNewAdapter: WhatsNewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWhatsNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUpdate = intent.getStringExtra(Constants.APP_UPDATE_FIRESTORE_DOCUMENT_KEY)
            ?.convertJsonToObject(AppUpdate::class.java)

        if (appUpdate == null) {
            onBackPressedDispatcher.onBackPressed()
        } else {

            if (appUpdate?.version == APP_VERSION) {

                binding.appUpdateBtn.hide()
                binding.textView86.text = "Using latest version ($APP_VERSION)"
            } else {
                binding.appUpdateBtn.show()
                binding.appUpdateBtn.text =
                    getString(R.string.update_to_version, appUpdate?.version)

                binding.appUpdateBtn.setOnClickListener {

                    showAlertDialogAndDownloadAPK()
                }
            }

            whatsNewAdapter = WhatsNewAdapter()
            setUpRecyclerView()

            whatsNewAdapter.submitList(appUpdate!!.whatsNew)
        }
    }

    private fun showAlertDialogAndDownloadAPK() {

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.download_apk))
            .setMessage("Go to your Downloads/${this.getString(R.string.app_name)} folder and install the apk once downloaded.")
            .setPositiveButton(getString(R.string.download)) { dialog, _ ->

                if (isInternetAvailable(this)) {
                    downloadFileFromUrl(
                        this,
                        appUpdate?.apk_url,
                        "myr_${appUpdate!!.version}.apk"
                    )
                } else {
                    showNoInternetMessage(this)
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()

    }

    private fun setUpRecyclerView() {

        binding.whatsNewRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = whatsNewAdapter
        }

        whatsNewAdapter.setOnClickListener(this)
    }

    override fun onItemClick(whatsNew: WhatsNew) {

        if (whatsNew.feature.startsWith("https") || whatsNew.feature.startsWith("http")) {

            openLinkInBrowser(this, whatsNew.feature)
        }

        if (whatsNew.image.isValid()) {

            val intent = Intent(this, ShowImageActivity::class.java)
            intent.putExtra(
                Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION,
                whatsNew.convertToJsonString()
            )

            // sending the tag to ShowImageActivity to handle the image send by WhatsNewActivity
            intent.putExtra(
                GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION2,
                getString(R.string.whats_new)
            )

            startActivity(intent)
        }

    }
}