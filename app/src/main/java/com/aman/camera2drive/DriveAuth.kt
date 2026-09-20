package com.aman.camera2drive

import android.app.Activity
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

object DriveAuth {
    const val DRIVE_SCOPE = DriveScopes.DRIVE_FILE

    fun authorize(
        activity: Activity,
        launchResolution: (IntentSenderRequest) -> Unit,
        onReady: (AuthorizationResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DRIVE_SCOPE)))
            .build()

        Identity.getAuthorizationClient(activity)
            .authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent == null) {
                        onFailure(IllegalStateException("Google authorization needs a resolution but returned none."))
                    } else {
                        launchResolution(
                            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                        )
                    }
                } else {
                    onReady(result)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun fromIntent(activity: Activity, data: Intent?): AuthorizationResult {
        return Identity.getAuthorizationClient(activity)
            .getAuthorizationResultFromIntent(data)
    }

    /**
     * Used by the background uploader. Once the user has granted drive.file,
     * AuthorizationClient can silently return a fresh short-lived access token
     * without showing UI. If consent was revoked, the foreground app must
     * authorize again.
     */
    fun getAccessToken(context: android.content.Context): String? {
        return try {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DRIVE_SCOPE)))
                .build()

            val result = com.google.android.gms.tasks.Tasks.await(
                Identity.getAuthorizationClient(context).authorize(request)
            )

            if (result.hasResolution()) null else result.accessToken
        } catch (_: Exception) {
            null
        }
    }

    fun isApiException(e: Exception): Boolean = e is ApiException
}