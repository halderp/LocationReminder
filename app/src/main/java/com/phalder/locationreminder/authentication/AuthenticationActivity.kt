package com.phalder.locationreminder.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.phalder.locationreminder.R
import com.phalder.locationreminder.databinding.ActivityAuthenticationBinding
import com.phalder.locationreminder.locationreminders.RemindersActivity

// Constants
const val SIGN_IN_RESULT_CODE = 1001
const val TAG = "AuthenticationActivity"

class AuthenticationActivity : AppCompatActivity() {

    // view binding for the Activity
    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // On click listener should be moved to MVVM as well.
        binding.btnLogin.setOnClickListener {
            authenthicateUser()
        }

        // When the user is authenticated they should be navigated to the Reminders screen.

        viewModel.authenticationState.observe(this, Observer<AuthenticationViewModel.AuthenticationState>{ authenticationState ->
            when(authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    // Go to Reminder List Activity
                    startActivity(Intent(this, RemindersActivity::class.java))
                    finish()
                }
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> Log.e(TAG, "Not authenticated!")
                else -> Log.e(TAG, "New $authenticationState state that doesn't require any UI change")
            }
        })
    }

    private fun authenthicateUser() {
        // create providers for 1. email/password 2. google auth
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.geofence)
                .setTheme(R.style.Theme_LocationReminder)
                 .build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                // User successfully signed in
                Log.i(TAG,"Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!" )
            } else {
                // Sign in failed. If response is null the user canceled the
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}