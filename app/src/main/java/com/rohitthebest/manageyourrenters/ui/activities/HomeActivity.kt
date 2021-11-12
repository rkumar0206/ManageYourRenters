package com.rohitthebest.manageyourrenters.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.rohitthebest.manageyourrenters.data.RenterTypes
import com.rohitthebest.manageyourrenters.databinding.ActivityHomeBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.ProfileBottomSheet
import com.rohitthebest.manageyourrenters.ui.viewModels.*
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private lateinit var renterTypeList: ArrayList<RenterTypes>
    private lateinit var renterTypeAdapter: RenterTypeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.shortcutExpenses.setOnClickListener(this)
        binding.shortcutMonthlyPayments.setOnClickListener(this)
        binding.shortcutEmis.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.shortcutExpenses.id -> {

                // todo :  open expense category fragment
            }

            binding.shortcutMonthlyPayments.id -> {

                // todo :  open monthly payments category fragment
            }

            binding.shortcutEmis.id -> {

                // todo :  open emis fragment
            }
        }
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

                    Functions.saveBooleanToSharedPreference(
                        this,
                        Constants.IS_SYNCED_SHARED_PREF_NAME,
                        Constants.IS_SYNCED_SHARED_PREF_KEY,
                        false
                    )

                    navigateToLoginActivity()

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
                .setTitle("Are You Sure?")
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