package com.safex.app.util

import java.io.File

object RootCheckUtil {
    
    private val SU_BINARY_PATHS = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/su",
        "/system/bin/.ext/.su",
        "/system/usr/we-need-root/su-backup",
        "/system/xbin/mu"
    )

    private val ROOT_PACKAGES = arrayOf(
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk"
    )

    fun isDeviceRooted(): Boolean {
        return checkForSuBinary() || checkForRootPackages() || checkForBusyBox() || checkBuildTags()
    }

    private fun checkForSuBinary(): Boolean {
        return SU_BINARY_PATHS.any { path ->
            try {
                File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun checkForRootPackages(): Boolean {
        return ROOT_PACKAGES.any { packageName ->
            try {
                Class.forName(packageName)
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    private fun checkForBusyBox(): Boolean {
        return try {
            File("/system/xbin/busybox").exists() ||
            File("/system/bin/busybox").exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun checkBuildTags(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    fun getRootStatus(): String {
        return if (isDeviceRooted()) "ROOTED" else "NOT_ROOTED"
    }
}
