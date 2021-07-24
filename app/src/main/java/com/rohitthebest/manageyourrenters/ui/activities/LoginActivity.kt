package com.rohitthebest.manageyourrenters.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.ActivityLoginBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

private const val TAG = "LoginActivity"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val renterViewModel by viewModels<RenterViewModel>()
    private val paymentViewModel by viewModels<PaymentViewModel>()
    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isSynced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        isSynced = Functions.loadBooleanFromSharedPreference(
            this,
            Constants.IS_SYNCED_SHARED_PREF_NAME,
            Constants.IS_SYNCED_SHARED_PREF_KEY
        )

        if (mAuth.currentUser != null && !isSynced) {

            syncSavedDataFromFirebase()
        }

        initListeners()
    }


    private fun initListeners() {

        binding.signInButton.setOnClickListener {

            if (Functions.isInternetAvailable(this)) {
                signIn()
            } else {
                Functions.showNoInternetMessage(this)
            }
        }
    }

    // [START signin]
    private fun signIn() {

        showProgressBar()

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(
            signInIntent,
            Constants.RC_SIGN_IN
        )
    }
    // [END signin]


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Constants.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                try {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                    // [START_EXCLUDE]
                    showToast(this, "SignIn Un-successful")
                    hideProgressBar()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        showProgressBar()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        try {
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        showToast(this, "SignIn successful")

                        syncSavedDataFromFirebase()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        showToast(this, "Authentication Failed.")
                        hideProgressBar()

                    }
                    // [START_EXCLUDE]
                    hideProgressBar()
                    // [END_EXCLUDE]
                }
        } catch (e: Exception) {
        }
    }

    //[START OF SYNC]
    @SuppressLint("SetTextI18n")
    private fun syncSavedDataFromFirebase() {

        Log.i(TAG, "checkSyncAndNavigateToHomeFragment: ")

        if (!isSynced) {

            binding.loginCL.hide()
            binding.syncingCL.show()

            binding.showSyncingInfoTV.text = "syncing renters..."

            // syncing renters
            CoroutineScope(Dispatchers.IO).launch {

                val renters = getDataFromFireStore(
                    getString(R.string.renters),
                    getUid()!!
                ) {

                    // on failure
                    showToast(this@LoginActivity, it.message!!)

                    signIn()
                }

                // adding renters to the local database
                renters?.let {

                    if (it.size() != 0) {

                        withContext(Dispatchers.Main) {

                            renterViewModel.deleteRenterByIsSynced(getString(R.string.t))
                            delay(100)
                            renterViewModel.insertRenters(it.toObjects(Renter::class.java))
                            syncRentersPayments()
                        }
                    } else {

                        withContext(Dispatchers.Main) {

                            renterViewModel.deleteRenterByIsSynced(getString(R.string.t))

                            showToast(
                                this@LoginActivity,
                                "You have not added any renters yet!!"
                            )
                            syncBorrowers()
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncRentersPayments() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_renters_payments)

        withContext(Dispatchers.IO) {

            val renterPayments = getDataFromFireStore(
                getString(R.string.payments),
                getUid()!!
            ) {
                lifecycleScope.launch {

                    showToast(this@LoginActivity, it.message.toString())
                    syncBorrowers()
                }
            }

            renterPayments?.let {


                if (renterPayments.size() != 0) {

                    withContext(Dispatchers.Main) {

                        paymentViewModel.deleteAllPaymentsByIsSynced(getString(R.string.t))
                        delay(100)
                        paymentViewModel.insertPayments(it.toObjects(Payment::class.java))
                        Log.i(TAG, "syncPayments: inserted")

                        syncBorrowers()
                    }

                } else {

                    withContext(Dispatchers.Main) {

                        paymentViewModel.deleteAllPaymentsByIsSynced(getString(R.string.t))

                        syncBorrowers()
                    }
                }
            }
        }
    }

    private suspend fun syncBorrowers() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_borrowers)

        withContext(Dispatchers.IO) {

            val borrowers = getDataFromFireStore(
                getString(R.string.borrowers),
                getUid()!!,
            ) {
                showToast(this@LoginActivity, "Something went wrong.")
                saveIsSyncedValueAndNavigateToHomeActivity()
            }

            borrowers?.let {

                if (borrowers.size() != 0) {

                    withContext(Dispatchers.Main) {

                        borrowerViewModel.deleteBorrowerByIsSynced(true)
                        delay(100)
                        borrowerViewModel.insertBorrowers(it.toObjects(Borrower::class.java))

                        syncBorrowersPayments()
                    }
                } else {

                    withContext(Dispatchers.Main) {

                        borrowerViewModel.deleteBorrowerByIsSynced(true)
                        saveIsSyncedValueAndNavigateToHomeActivity()
                    }
                }
            }
        }
    }

    private suspend fun syncBorrowersPayments() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_borrower_payments)

        withContext(Dispatchers.IO) {

            val borrowerPayments = getDataFromFireStore(
                getString(R.string.borrowerPayments),
                getUid()!!,
            ) {

                showToast(this@LoginActivity, "Something went wrong.")
                saveIsSyncedValueAndNavigateToHomeActivity()
            }
            borrowerPayments?.let {

                if (borrowerPayments.size() != 0) {

                    withContext(Dispatchers.Main) {

                        borrowerPaymentViewModel.deleteBorrowerPaymentsByIsSynced(true)

                        delay(100)
                        borrowerPaymentViewModel.insertBorrowerPayments(it.toObjects(BorrowerPayment::class.java))

                        saveIsSyncedValueAndNavigateToHomeActivity()
                    }
                } else {

                    withContext(Dispatchers.Main) {

                        borrowerPaymentViewModel.deleteBorrowerPaymentsByIsSynced(true)
                        saveIsSyncedValueAndNavigateToHomeActivity()
                    }
                }
            }

        }
    }

    private fun saveIsSyncedValueAndNavigateToHomeActivity() {

        showProgressBar()

        isSynced = true

        Functions.saveBooleanToSharedPreference(
            this@LoginActivity,
            Constants.IS_SYNCED_SHARED_PREF_NAME,
            Constants.IS_SYNCED_SHARED_PREF_KEY,
            isSynced
        )

        hideProgressBar()

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    //[end OF SYNC]

    private fun showProgressBar() {

        try {

            binding.progressBar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.progressBar.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}