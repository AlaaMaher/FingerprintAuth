package com.alaa.fingerprintauth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

class BiometricAuthenticator(val ctx: Context) {

    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val biometricManager = BiometricManager.from(ctx)
    private lateinit var biometricPrompt: BiometricPrompt

    fun isBiometricAuthAvailable(): BiometricAuthStatus {
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthStatus.NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED
            else -> BiometricAuthStatus.NOT_AVAILABLE
        }
    }

    fun promptBiometricAuth(
        title: String,
        subtitle: String,
        cancelBtnText: String,
        fragmentActivity: FragmentActivity,
        onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
        onFailed: () -> Unit,
        onError: (code: Int, message: String) -> Unit
    ) {

        when (isBiometricAuthAvailable()) {
            BiometricAuthStatus.NOT_AVAILABLE -> {
                onError(BiometricAuthStatus.NOT_AVAILABLE.id, "Not Available For This Device!")
                return
            }

            BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE -> {
                onError(
                    BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE.id, "Not Available at this moment!"
                )
                return
            }

            BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED -> {
                onError(
                    BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED.id,
                    "It seems you didn't add any fingerprint"
                )
                return
            }

            else -> Unit
        }
        biometricPrompt =
            BiometricPrompt(fragmentActivity, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    onFailed()
                }
            })
        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle)
            .setNegativeButtonText(cancelBtnText).build()
        biometricPrompt.authenticate(promptInfo)
    }

}