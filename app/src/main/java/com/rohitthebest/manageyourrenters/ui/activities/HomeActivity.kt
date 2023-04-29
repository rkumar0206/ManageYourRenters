package com.rohitthebest.manageyourrenters.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.RenterTypeAdapter
import com.rohitthebest.manageyourrenters.data.AppUpdate
import com.rohitthebest.manageyourrenters.data.RenterTypes
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.ActivityHomeBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.APP_UPDATE_FIRESTORE_DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.APP_VERSION
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_BORROWERS
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_EMI
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_EXPENSE
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_FRAGMENT_NAME_KEY
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_HOUSE_RENTERS
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.APP_UPDATES
import com.rohitthebest.manageyourrenters.ui.ProfileBottomSheet
import com.rohitthebest.manageyourrenters.ui.viewModels.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBooleanToSharedPreference
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.convertToJsonString
import com.rohitthebest.manageyourrenters.utils.getDocumentFromFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*

private const val TAG = "HomeActivity"

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), RenterTypeAdapter.OnClickListener,
    ProfileBottomSheet.OnItemClickListener, View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mAuth: FirebaseAuth

    private val renterViewModel: RenterViewModel by viewModels()
    private val borrowerViewModel: BorrowerViewModel by viewModels()
    private val emiViewModel: EMIViewModel by viewModels()
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private lateinit var renterTypeList: ArrayList<RenterTypes>
    private lateinit var renterTypeAdapter: RenterTypeAdapter

    private var appUpdate: AppUpdate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ManageYourRenters)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = Firebase.auth

        // checking if the user is logged in
        if (mAuth.currentUser == null) {

            navigateToLoginActivity()
        }

        initListeners()

        populateRenterTypeList()

        renterTypeAdapter = RenterTypeAdapter()
        setUpRecyclerView()
        renterTypeAdapter.submitList(renterTypeList)

        handleShortcuts()

        checkForUpdates()

        //Issue #78
        checkIfDefaultPaymentMethodIsInserted()
    }

    //Issue #78
    private fun checkIfDefaultPaymentMethodIsInserted() {

        paymentMethodViewModel.getPaymentMethodByKey(Constants.PAYMENT_METHOD_DEBIT_CARD_KEY)
            .observe(this) { paymentMethod ->

                Log.d(TAG, "checkIfDefaultPaymentMethodIsInserted: $paymentMethod")

                if (paymentMethod == null) {

                    val paymentMethodCash = PaymentMethod(
                        key = Constants.PAYMENT_METHOD_CASH_KEY,
                        paymentMethod = Constants.CASH_PAYMENT_METHOD,
                        uid = "",
                        isSynced = true
                    )

                    val paymentMethodDebitCard = PaymentMethod(
                        key = Constants.PAYMENT_METHOD_DEBIT_CARD_KEY,
                        paymentMethod = Constants.DEBIT_CARD_PAYMENT_METHOD,
                        uid = "",
                        isSynced = true
                    )

                    val paymentMethodCreditCard = PaymentMethod(
                        key = Constants.PAYMENT_METHOD_CREDIT_CARD_KEY,
                        paymentMethod = Constants.CREDIT_CARD_PAYMENT_METHOD,
                        uid = "",
                        isSynced = true
                    )

                    val paymentMethodOthers = PaymentMethod(
                        key = Constants.PAYMENT_METHOD_OTHER_KEY,
                        paymentMethod = Constants.OTHER_PAYMENT_METHOD,
                        uid = "",
                        isSynced = true
                    )

                    paymentMethodViewModel.insertAllPaymentMethod(
                        listOf(
                            paymentMethodCash,
                            paymentMethodDebitCard,
                            paymentMethodCreditCard,
                            paymentMethodOthers
                        )
                    )
                }
            }
    }

    private fun checkForUpdates(shouldOpenWhatsNewActivity: Boolean = false) {

        // checking for app update
        if (isInternetAvailable(this)
        ) {

            if (shouldOpenWhatsNewActivity) {
                showToast(this, getString(R.string.checking_for_updates))
            }

            CoroutineScope(Dispatchers.IO).launch {

                getDocumentFromFirestore(
                    collection = APP_UPDATES,
                    documentKey = APP_UPDATE_FIRESTORE_DOCUMENT_KEY,
                    successListener = { documentSnapshot ->

                        appUpdate = documentSnapshot?.toObject(AppUpdate::class.java)
                        Log.d(TAG, "onCreate: Successfully fetched app update $appUpdate")
                    },
                    failureListener = { ex ->
                        ex.printStackTrace()
                    }
                )
                withContext(Dispatchers.Main) {

                    compareAppVersionFromCloud()
                    if (shouldOpenWhatsNewActivity && (appUpdate != null && !appUpdate!!.isEmpty())) {
                        openWhatsNewActivity()
                    }
                }
            }
        } else {

            compareAppVersionFromCloud()
        }
    }

    private fun compareAppVersionFromCloud() {

        if (appUpdate != null && !appUpdate!!.isEmpty()) {

            // compare the version
            if (appUpdate?.version != APP_VERSION) {

                Log.d(
                    TAG,
                    "compareAppVersionFromCloud: Version $APP_VERSION does not match with firestore's version ${appUpdate?.version}"
                )

                showToast(this, getString(R.string.update_avaliable))
                binding.toolbar.menu.findItem(R.id.menu_app_update)
                    .setIcon(R.drawable.ic_update_icon_with_badge)
            } else {
                binding.toolbar.menu.findItem(R.id.menu_app_update)
                    .setIcon(R.drawable.ic_round_upgrade_24)
            }
        } else {

            binding.toolbar.menu.findItem(R.id.menu_app_update)
                .setIcon(R.drawable.ic_round_upgrade_24)
        }
    }

    private fun handleShortcuts() {

        when (intent.action) { // we can also use intent.data for the same purpose

            SHORTCUT_EXPENSE -> {
                onClick(binding.shortcutExpenses)
            }
            SHORTCUT_MONTHLY_PAYMENTS -> {
                onClick(binding.shortcutMonthlyPayments)
            }
            SHORTCUT_EMI -> {
                onClick(binding.shortcutEmis)
            }
            SHORTCUT_HOUSE_RENTERS -> {
                onItemClick(RenterTypes(1, "", 0))
            }
            SHORTCUT_BORROWERS -> {
                onItemClick(RenterTypes(2, "", 0))
            }
        }

    }

    private fun initListeners() {

        binding.toolbar.menu.findItem(R.id.menu_sync_with_cloud).setOnMenuItemClickListener {

            showAlertMessageAndNavigateToLoginActivity()
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_profile).setOnMenuItemClickListener {

            showBottomSheetProfileDialog()
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_app_update).setOnMenuItemClickListener {

            if (appUpdate != null) {
                openWhatsNewActivity()
            } else {
                if (isInternetAvailable(this)) {
                    checkForUpdates(true)
                } else {
                    showToast(this, getString(R.string.unable_to_fetch_details), Toast.LENGTH_LONG)
                }
            }
            true
        }

        binding.shortcutExpenses.setOnClickListener(this)
        binding.shortcutMonthlyPayments.setOnClickListener(this)
        binding.shortcutEmis.setOnClickListener(this)

    }

    private fun openWhatsNewActivity() {

        val intent = Intent(this, WhatsNewActivity::class.java)
        intent.putExtra(APP_UPDATE_FIRESTORE_DOCUMENT_KEY, appUpdate.convertToJsonString())

        startActivity(intent)
    }

    override fun onClick(v: View?) {

        val intent = Intent(this, TrackMoneyActivity::class.java)

        when (v?.id) {

            binding.shortcutExpenses.id -> {

                intent.putExtra(SHORTCUT_FRAGMENT_NAME_KEY, getString(R.string.expenses))
            }

            binding.shortcutMonthlyPayments.id -> {

                intent.putExtra(SHORTCUT_FRAGMENT_NAME_KEY, getString(R.string.monthly_payments))
            }

            binding.shortcutEmis.id -> {

                intent.putExtra(SHORTCUT_FRAGMENT_NAME_KEY, getString(R.string.emis))
            }
        }

        startActivity(intent)
    }

    private fun populateRenterTypeList() {

        renterTypeList = ArrayList()

        renterTypeList.add(
            RenterTypes(
                id = 1,
                renterType = getString(R.string.house_renters),
                image = R.drawable.ic_house_renters
            )
        )

        renterTypeList.add(
            RenterTypes(
                id = 2,
                renterType = getString(R.string.borrowers),
                image = R.drawable.ic_borrower
            )
        )

        renterTypeList.add(
            RenterTypes(
                id = 3,
                renterType = getString(R.string.track_money),
                image = R.drawable.ic_track_money
            )
        )
    }

    private fun setUpRecyclerView() {

        binding.renterTypesRV.apply {

            adapter = renterTypeAdapter
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            setHasFixedSize(true)
        }

        renterTypeAdapter.setOnClickListener(this)
    }

    override fun onItemClick(renterType: RenterTypes) {

        when (renterType.id) {

            1 -> {

                val intent = Intent(this, HouseRentersActivity::class.java)
                startActivity(intent)
            }

            2 -> {

                val intent = Intent(this, BorrowerActivity::class.java)
                startActivity(intent)
            }

            else -> {

                val intent = Intent(this, TrackMoneyActivity::class.java)
                startActivity(intent)
            }

        }
    }

    private fun showAlertMessageAndNavigateToLoginActivity() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Sync changes from cloud?")
            .setMessage("If any changes has been done on cloud this will sync them with your phone.")
            .setPositiveButton("Request sync") { d, _ ->

                if (isInternetAvailable(this)) {

                    changeIsSyncedValue()
                    d.dismiss()
                } else {

                    showNoInternetMessage(this)
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

    @SuppressLint("SetTextI18n")
    private fun showBottomSheetProfileDialog() {

        this.supportFragmentManager.let { fragmentManager ->

            ProfileBottomSheet.newInstance(null)
                .apply {
                    show(fragmentManager, TAG)
                }.setOnClickListener(this)
        }
    }

    override fun onSignOutBtnClicked() {

        if (isInternetAvailable(this)) {

            signOut()
        } else {

            showNoInternetMessage(this)
        }
    }

    private fun signOut() {

        try {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.are_you_sure))
                .setMessage("You will be signed out from this app.")
                .setPositiveButton("Yes") { _, _ ->

                    mAuth.signOut()

                    //[Google Sign Out]
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(this, gso)

                    googleSignInClient.signOut().addOnCompleteListener {
                        Log.i(TAG, "Google signOut Successful")

                        try {
                            Log.i(TAG, "signOut: Google signOut Successful")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    showToast(this, "SignOut Successful")

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
            borrowerViewModel.deleteAllBorrower()
            emiViewModel.deleteAllEMIs()
            expenseCategoryViewModel.deleteAllExpenseCategories()
            monthlyPaymentCategoryViewModel.deleteAllMonthlyPaymentCategories()
            paymentMethodViewModel.deleteAllPaymentMethods()
            changeIsSyncedValue()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeIsSyncedValue() {

        try {

            saveBooleanToSharedPreference(
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