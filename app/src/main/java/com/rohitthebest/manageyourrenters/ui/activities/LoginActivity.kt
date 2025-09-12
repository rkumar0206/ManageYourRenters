package com.rohitthebest.manageyourrenters.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.ActivityLoginBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.services.SyncDocumentsFromFirestoreService
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "LoginActivity"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var isSynced = false
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ManageYourRenters)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(baseContext)
        firebaseAuth = Firebase.auth

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

    private fun signIn() {

        showProgressBar()

        val googleIdOption = GetGoogleIdOption.Builder()
            // Your server's client ID, not your Android client ID.
            .setServerClientId(getString(R.string.default_web_client_id))
            // Only show accounts previously used to sign in.
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()


        lifecycleScope.launch {
            try {
                // Launch Credential Manager UI
                val result = credentialManager.getCredential(
                    context = baseContext,
                    request = request
                )

                // Extract credential from the result returned by Credential Manager
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
                hideProgressBar()
                handleSignInError(e)
            }
        }
    }

    // [START handle_sign_in]
    private fun handleSignIn(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }
    // [END handle_sign_in]


    private fun handleSignInError(e: GetCredentialException) {
        when (e) {
            is GetCredentialCancellationException -> {
                Log.w(TAG, "User cancelled the sign-in flow.")
                showToast("Sign-in cancelled.")
            }

            is GetCredentialInterruptedException -> {
                Log.e(TAG, "Sign-in flow interrupted.", e)
                showToast("Sign-in interrupted. Please try again.")
            }

            is GetCredentialUnsupportedException -> {
                Log.e(TAG, "Sign-in operation not supported on this device/provider.", e)
                showToast("Sign-in not supported on this device.")
            }

            is NoCredentialException -> {
                Log.i(TAG, "No Google accounts found or user chose not to sign in.")
                showToast("No Google accounts found or sign-in declined.")
                // You might want to guide the user to add a Google account to their device
                // or offer alternative sign-up/sign-in methods here.
            }

            else -> {
                Log.e(TAG, "Sign-in failed: ${e.message}", e)
                showToast("Sign-in failed. Please try again.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        showProgressBar()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        try {
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        showToast(this, "SignIn successful")

                        syncDataFromFirestore()

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
            e.printStackTrace()
        }
    }

    private fun syncDataFromFirestore() {

        val foregroundService = Intent(
            applicationContext,
            SyncDocumentsFromFirestoreService::class.java
        )
        ContextCompat.startForegroundService(applicationContext, foregroundService)

        navigateToHomeActivity()
    }

    private fun navigateToHomeActivity() {

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

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