package com.example.data.security

import android.content.Context
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest

class AppLockManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("manobseba_applock", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false) && !prefs.getString(KEY_PIN_HASH, null).isNullOrBlank()

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC, false)

    fun savePin(pin: String, enableBiometric: Boolean) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_PIN_HASH, sha256(pin))
            .putBoolean(KEY_BIOMETRIC, enableBiometric)
            .apply()
    }

    fun disableLock() {
        prefs.edit()
            .putBoolean(KEY_ENABLED, false)
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_BIOMETRIC, false)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return stored == sha256(pin)
    }

    fun canUseBiometric(context: Context): Boolean {
        val mgr = BiometricManager.from(context)
        return mgr.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticateBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("মানবসেবা ফাউন্ডেশন আনলক")
            .setSubtitle("সংবেদনশীল সদস্য ও আর্থিক তথ্য সুরক্ষিত রয়েছে")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance(KeyProperties.DIGEST_SHA256).digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val KEY_ENABLED = "lock_enabled"
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_BIOMETRIC = "biometric_enabled"
    }
}
