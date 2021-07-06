package com.rohitthebest.manageyourrenters.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.ActivityHomeBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "HomeActivity"

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mAuth: FirebaseAuth

    private val renterViewModel: RenterViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = Firebase.auth

        // checking if the user is logged in
        if (mAuth.currentUser == null) {

            navigateToLoginActivity()

        } else {

            updateUI()
        }

        binding.profileImage.setOnClickListener {

            showBottomSheetProfileDialog()
        }

        binding.downloadChangesBtn.setOnClickListener {

            showAlertMessageAndNavigateToLoginActivity()
        }
    }

    private fun showAlertMessageAndNavigateToLoginActivity() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Sync changes from cloud?")
            .setMessage("If any changes has been done on cloud this will sync them with your phone.")
            .setPositiveButton("Request sync") { d, _ ->

                if (Functions.isInternetAvailable(this)) {

                    Functions.saveBooleanToSharedPreference(
                        this,
                        Constants.IS_SYNCED_SHARED_PREF_NAME,
                        Constants.IS_SYNCED_SHARED_PREF_KEY,
                        false
                    )

                    navigateToLoginActivity()

                    d.dismiss()
                } else {

                    Functions.showNoInternetMessage(this)
                }
            }.setNegativeButton("Cancel") { d, _ ->

                d.dismiss()
            }
            .create()
            .show()

    }

    private fun navigateToLoginActivity() {

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun updateUI() {

        if (mAuth.currentUser != null) {

            try {

                if (mAuth.currentUser!!.photoUrl != null) {

                    Glide.with(this)
                        .load(mAuth.currentUser!!.photoUrl)
                        .into(binding.profileImage)
                }
            } catch (e: Exception) {

                e.printStackTrace()
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun showBottomSheetProfileDialog() {

        MaterialDialog(this, BottomSheet(layoutMode = LayoutMode.WRAP_CONTENT)).show {

            customView(
                R.layout.user_info_with_sign_out_layout,
                scrollable = true
            )

            val userName = getCustomView().findViewById<TextView>(R.id.userNameTV)
            val emailId = getCustomView().findViewById<TextView>(R.id.userEmailTV)
            val signOutBtn = getCustomView().findViewById<MaterialCardView>(R.id.signOutBtn)

            userName.text = mAuth.currentUser?.displayName
            emailId.text = mAuth.currentUser?.email

            signOutBtn.setOnClickListener {

                if (Functions.isInternetAvailable(this@HomeActivity)) {

                    signOut()

                    dismiss()
                } else {

                    Functions.showNoInternetMessage(this@HomeActivity)
                }
            }
        }
    }

    private fun signOut() {

        try {
            MaterialAlertDialogBuilder(this)
                .setTitle("Are You Sure?")
                .setPositiveButton("Yes") { _, _ ->

                    mAuth.signOut()

                    //[Google Sign Out]
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(this, gso)

                    googleSignInClient?.signOut()?.addOnCompleteListener {
                        Log.i(TAG, "Google signOut Successful")

                        try {
                            Log.i(TAG, "signOut: Google signOut Successful")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    Functions.showToast(this, "SignOut Successful")

                    //deleting everything saved on SQLite
                    deleteEverythingFromSQLite()

                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteEverythingFromSQLite() {

        try {
            renterViewModel.deleteAllRenter()
            paymentViewModel.deleteAllPayments()
            changeIsSyncedValue()

        } catch (e: Exception) {
        }
    }

    private fun changeIsSyncedValue() {

        try {

            Functions.saveBooleanToSharedPreference(
                this,
                Constants.IS_SYNCED_SHARED_PREF_NAME,
                Constants.IS_SYNCED_SHARED_PREF_KEY,
                false
            )

            Log.i(TAG, "saveIsSyncedValue: changed the value of isSynced to false")

            lifecycleScope.launch {

                delay(200)

                withContext(Dispatchers.Main) {

                    navigateToLoginActivity()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "saveData: ${e.message}")
        }

    }
}