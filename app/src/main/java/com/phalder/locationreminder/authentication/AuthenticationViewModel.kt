package com.phalder.locationreminder.authentication

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class AuthenticationViewModel: ViewModel() {
    enum class AuthenticationState {
        AUTHENTICATED,
        UNAUTHENTICATED,
    }
    var firebaseUserLiveData: FirebaseUserLiveData = FirebaseUserLiveData()
    val authenticationState = Transformations.map(firebaseUserLiveData) { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}