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
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWER_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMI_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMIs
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSE_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENT_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.PARTIAL_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTER_PAYMENTS
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
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()
    private val monthlyPaymentViewModel by viewModels<MonthlyPaymentViewModel>()

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
                        syncRenters()
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
    private fun syncRenters() {

        Log.i(TAG, "checkSyncAndNavigateToHomeFragment: ")

        if (!isSynced) {

            binding.loginCL.hide()
            binding.syncingCL.show()

            binding.showSyncingInfoTV.text = "syncing renters..."

            // syncing renters
            CoroutineScope(Dispatchers.IO).launch {

                val renters = getDataFromFireStore(
                    collection = RENTERS,
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
                collection = RENTER_PAYMENTS,
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
                BORROWERS,
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
                BORROWER_PAYMENTS,
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
                PARTIAL_PAYMENTS,
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
            EMIs,
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
                    syncExpenseCategory()
                }
            }
        }

    }

    private suspend fun syncEMIPayments() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_emi_payments)

        val emiPayments = getDataFromFireStore(
            EMI_PAYMENTS,
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

                    syncExpenseCategory()
                }

            } else {

                withContext(Dispatchers.Main) {

                    emiPaymentViewModel.deleteEMIPaymentsByIsSynced(true)
                    syncExpenseCategory()
                }
            }
        }
    }

    private suspend fun syncExpenseCategory() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_expense)

        val expenseCategory = getDataFromFireStore(
            EXPENSE_CATEGORIES,
            getUid()!!,
        ) {
            Log.d(TAG, "syncExpenseCategory: $it")
            showToast(this@LoginActivity, "Something went wrong...")

            saveIsSyncedValueAndNavigateToHomeActivity()
        }

        expenseCategory?.let {

            if (expenseCategory.size() != 0) {

                withContext(Dispatchers.Main) {

                    expenseCategoryViewModel.deleteExpenseCategoryByIsSyncedValue(true)
                    delay(50)
                    expenseCategoryViewModel.insertAllExpenseCategory(
                        expenseCategory.toObjects(
                            ExpenseCategory::class.java
                        )
                    )

                    syncExpenses()
                }

            } else {

                withContext(Dispatchers.Main) {

                    expenseCategoryViewModel.deleteExpenseCategoryByIsSyncedValue(true)
                    syncMonthlyPaymentsCategory()
                }
            }
        }
    }

    private suspend fun syncExpenses() {

        val expenses = getDataFromFireStore(
            EXPENSES,
            getUid()!!,
        ) {
            Log.d(TAG, "syncExpenses: $it")
            showToast(this@LoginActivity, "Something went wrong...")

            saveIsSyncedValueAndNavigateToHomeActivity()
        }

        expenses?.let {

            if (expenses.size() != 0) {

                withContext(Dispatchers.Main) {

                    expenseViewModel.deleteAllExpensesByIsSynced(true)
                    delay(50)
                    expenseViewModel.insertAllExpense(
                        expenses.toObjects(
                            Expense::class.java
                        )
                    )

                    syncMonthlyPaymentsCategory()
                }

            } else {

                withContext(Dispatchers.Main) {

                    expenseViewModel.deleteAllExpensesByIsSynced(true)
                    syncMonthlyPaymentsCategory()
                }
            }
        }
    }

    private suspend fun syncMonthlyPaymentsCategory() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_monthly_payment)

        val monthlyPaymentCategories = getDataFromFireStore(
            MONTHLY_PAYMENT_CATEGORIES,
            getUid()!!,
        ) {

            Log.d(TAG, "syncMonthlyPaymentsCategory: $it")
            showToast(this@LoginActivity, "Something went wrong...")
            saveIsSyncedValueAndNavigateToHomeActivity()
        }

        monthlyPaymentCategories?.let {

            if (monthlyPaymentCategories.size() != 0) {

                withContext(Dispatchers.Main) {

                    monthlyPaymentCategoryViewModel.deleteAllMonthlyPaymentCategoriesByIsSynced(true)
                    delay(50)
                    monthlyPaymentCategoryViewModel.insertAllMonthlyPaymentCategory(
                        monthlyPaymentCategories.toObjects(
                            MonthlyPaymentCategory::class.java
                        )
                    )

                    syncMonthlyPayments()
                }

            } else {

                withContext(Dispatchers.Main) {

                    monthlyPaymentCategoryViewModel.deleteAllMonthlyPaymentCategoriesByIsSynced(true)
                    saveIsSyncedValueAndNavigateToHomeActivity()
                }
            }
        }
    }

    private suspend fun syncMonthlyPayments() {

        binding.showSyncingInfoTV.text = getString(R.string.sync_monthly_payment)

        val monthlyPayments = getDataFromFireStore(
            MONTHLY_PAYMENTS,
            getUid()!!,
        ) {

            Log.d(TAG, "syncMonthlyPayments: $it")
            showToast(this@LoginActivity, "Something went wrong...")
            saveIsSyncedValueAndNavigateToHomeActivity()
        }

        monthlyPayments?.let {

            if (monthlyPayments.size() != 0) {

                withContext(Dispatchers.Main) {

                    monthlyPaymentViewModel.deleteAllMonthlyPaymentByIsSynced(true)
                    delay(50)
                    monthlyPaymentViewModel.insertAllMonthlyPayment(
                        monthlyPayments.toObjects(
                            MonthlyPayment::class.java
                        )
                    )

                    saveIsSyncedValueAndNavigateToHomeActivity()
                }

            } else {

                withContext(Dispatchers.Main) {

                    monthlyPaymentViewModel.deleteAllMonthlyPaymentByIsSynced(true)
                    saveIsSyncedValueAndNavigateToHomeActivity()
                }
            }
        }
    }

    //[END OF SYNC]

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