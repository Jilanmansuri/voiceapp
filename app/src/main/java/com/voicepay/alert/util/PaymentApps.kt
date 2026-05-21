package com.voicepay.alert.util

/**
 * Supported UPI / wallet app package names and display labels.
 */
object PaymentApps {

    val SUPPORTED_PACKAGES: Map<String, String> = linkedMapOf(
        "com.phonepe.app" to "PhonePe",
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM",
        "in.amazon.mShop.android.shopping" to "Amazon Pay"
    )

    fun displayName(packageName: String): String =
        SUPPORTED_PACKAGES[packageName] ?: packageName
}
