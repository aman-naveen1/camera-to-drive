package com.aman.camera2drive

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

object DriveAuth {
    const val RC_SIGN_IN = 2401
    private const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file"

    fun signIn(activity: Activity, ready: () -> Unit) {
        if (GoogleSignIn.getLastSignedInAccount(activity) != null) {
            ready()
            return
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DRIVE_SCOPE))
            .build()
        activity.startActivityForResult(
            GoogleSignIn.getClient(activity, options).signInIntent,
            RC_SIGN_IN
        )
    }
}