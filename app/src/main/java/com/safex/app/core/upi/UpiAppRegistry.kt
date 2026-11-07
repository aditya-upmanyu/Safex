package com.safex.app.core.upi

/**
 * Configurable registry of supported UPI applications.
 * Update package identifiers here without touching the detection engine.
 */
object UpiAppRegistry {

    data class UpiApp(
        val packageName: String,
        val displayName: String
    )

    val supportedApps: List<UpiApp> = listOf(
        UpiApp("com.google.android.apps.nbu.paisa.user", "Google Pay"),
        UpiApp("com.phonepe.app", "PhonePe"),
        UpiApp("net.one97.paytm", "Paytm"),
        UpiApp("in.org.npci.upiapp", "BHIM"),
        UpiApp("com.dreamplug.androidapp", "CRED"),
        UpiApp("com.sbi.lotusintouch", "YONO SBI"),
        UpiApp("com.csam.icici.bank.imobile", "iMobile Pay"),
        UpiApp("com.axis.mobile", "Axis Mobile"),
        UpiApp("com.kotak.mobilebanking", "Kotak Mobile Banking"),
        UpiApp("com.mobikwik_new", "MobiKwik")
    )

    val packageNames: Set<String> = supportedApps.map { it.packageName }.toSet()

    fun getDisplayName(packageName: String): String =
        supportedApps.find { it.packageName == packageName }?.displayName ?: packageName

    fun isUpiApp(packageName: String): Boolean = packageNames.contains(packageName)
}
