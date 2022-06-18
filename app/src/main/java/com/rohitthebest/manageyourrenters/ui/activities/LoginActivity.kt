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
import com.rohitthebest.manageyourrenters.database.model.*
import com.rohitthebest.manageyourrenters.databinding.ActivityLoginBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.services.GetAllExpenseAndExpenseCategoryService
import com.rohitthebest.manageyourrenters.services.SyncAllMonthlyPaymentsAndCategoriesService
import com.rohitthebest.manageyourrenters.ui.viewModels.*
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

private const val TAG = "LoginActivity"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val renterViewModel by viewModels<RenterViewModel>()
    private val renterPaymentViewModel by viewModels<RenterPaymentViewModel>()
    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()
    private val partialPaymentViewModel by viewModels<PartialPaymentViewModel>()
    private val emiViewModel by viewModels<EMIViewModel>()
    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isSynced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ManageYourRenters)


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

            syncMonthlyPayments()
            syncExpenses()
        }

        initListeners()
    }


    private fun initListeners() {

        binding.signInButton.setOnClickListener {

            if (isInternetAvailable(this)) {
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

                        syncMonthlyPayments()
                        syncExpenses()

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

    private fun syncMonthlyPayments() {

        if (isInternetAvailable(this)) {

            val intent =
                Intent(applicationContext, SyncAllMonthlyPaymentsAndCategoriesService::class.java)

            this.startService(intent)
        }
    }

    private fun syncExpenses() {

        if (isInternetAvailable(this)) {

            val intent =
                Intent(applicationContext, GetAllExpenseAndExpenseCategoryService::class.java)

            this.startService(intent)
        }

        syncRenters()
    }

    @SuppressLint("SetTextI18n")
    private fun syncRenters() {

        Log.i(TAG, "checkSyncAndNavigateToHomeFragment: ")

        if (!isSynced) {

            binding.loginCL.hide()
            binding.syncingCL.show()

            binding.showSyncingInfoTV.text = "syncing renters..."

            // syncing renters
            CoroutineScope(Dispatchers.IO).launch {

                val renters = getDataFromFireStore(
                    collection = getString(R.string.renters),
                    orderBy = "timeStamp",
                    uid = getUid()!!
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

                        // when there are no renters then, deleting the synced renters from the database
                        // calling the syncBorrowers() method

                        withContext(Dispatchers.Main) {

                            renterViewModel.deleteRenterByIsSynced(getString(R.string.t))

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
                collection = getString(R.string.renter_payments),
                uid = getUid()!!
            ) {
                lifecycleScope.launch {

                    showToast(this@LoginActivity, it.message.toString())
                    syncBorrowers()
                }
            }

            renterPayments?.let {

                if (renterPayments.size() != 0) {

                    withContext(Dispatchers.Main) {

                        renterPaymentViewModel.deleteAllPaymentsByIsSynced(true)
                        delay(100)
                        renterPaymentViewModel.insertPayments(it.toObjects(RenterPayment::class.java))
                        Log.i(TAG, "syncPayments: inserted")

                        syncBorrowers()
                    }

                } else {

                    withContext(Dispatchers.Main) {

                        renterPaymentViewModel.deleteAllPaymentsByIsSynced(true)

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
                        borrowerPaymentViewModel.deleteBorrowerPaymentsByIsSynced(true)
                        partialPaymentViewModel.deletePartialPaymentsByIsSynced(true)
                        syncEMIS()
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

                        syncBorrowersPartialPayments()
                    }
                } else {

                    withContext(Dispatchers.Main) {

                        borrowerPaymentViewModel.deleteBorrowerPaymentsByIsSynced(true)
                        partialPaymentViewModel.deletePartialPaymentsByIsSynced(true)
                        syncEMIS()
                    }
                }
            }

        }
    }

    private suspend fun syncBorrowersPartialPayments() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_partial_payments)

        withContext(Dispatchers.IO) {

            val partialPayments = getDataFromFireStore(
                getString(R.string.partialPayments),
                getUid()!!
            ) {
                showToast(this@LoginActivity, "Something went wrong.")
                saveIsSyncedValueAndNavigateToHomeActivity()
            }

            partialPayments?.let {

                if (partialPayments.size() != 0) {

                    withContext(Dispatchers.Main) {

                        partialPaymentViewModel.deletePartialPaymentsByIsSynced(true)
                        delay(100)
                        partialPaymentViewModel.insertAllPartialPayment(it.toObjects(PartialPayment::class.java))

                        syncEMIS()
                    }
                } else {

                    withContext(Dispatchers.Main) {

                        partialPaymentViewModel.deletePartialPaymentsByIsSynced(true)
                        syncEMIS()
                    }
                }
            }
        }
    }

    private suspend fun syncEMIS() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_emis)

        val emis = getDataFromFireStore(
            getString(R.string.emis),
            getUid()!!,
        ) {
            Log.e(TAG, "syncEMIS: $it")
            showToast(this@LoginActivity, "Something went wrong...")

            saveIsSyncedValueAndNavigateToHomeActivity()
        }

        emis?.let {

            if (emis.size() != 0) {

                withContext(Dispatchers.Main) {

                    emiViewModel.deleteEMIsByIsSynced(true)
                    delay(50)
                    emiViewModel.insertAllEMI(emis.toObjects(EMI::class.java))

                    syncEMIPayments()
                }

            } else {

                withContext(Dispatchers.Main) {

                    emiViewModel.deleteEMIsByIsSynced(true)
                    saveIsSyncedValueAndNavigateToHomeActivity()
                }
            }
        }

    }

    private suspend fun syncEMIPayments() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_emi_payments)

        val emiPayments = getDataFromFireStore(
            getString(R.string.emiPayments),
            getUid()!!,
        ) {
            Log.e(TAG, "syncEMIPayments: $it")
            showToast(this@LoginActivity, "Something went wrong...")

            saveIsSyncedValueAndNavigateToHomeActivity()
        }

        emiPayments?.let {

            if (emiPayments.size() != 0) {

                withContext(Dispatchers.Main) {

                    emiPaymentViewModel.deleteEMIPaymentsByIsSynced(true)
                    delay(50)
                    emiPaymentViewModel.insertAllEMIPayment(emiPayments.toObjects(EMIPayment::class.java))

                    saveIsSyncedValueAndNavigateToHomeActivity()
                }

            } else {

                withContext(Dispatchers.Main) {

                    emiPaymentViewModel.deleteEMIPaymentsByIsSynced(true)
                    saveIsSyncedValueAndNavigateToHomeActivity()
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