package com.github.quickreactor.beepboop

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ActivityCompat

class PermissionsManager {

    fun hasPermissions(context: Context): Boolean {
        val hasRead = checkPermission(context, READ_PERMISSION)
        val hasSend = checkPermission(context, SEND_PERMISSION)
        return hasRead && hasSend
    }

    fun getStatus(context: Context): Map<String, Boolean> {
        return mapOf(
            READ_PERMISSION to checkPermission(context, READ_PERMISSION),
            SEND_PERMISSION to checkPermission(context, SEND_PERMISSION)
        )
    }

    fun requestPermissions(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(READ_PERMISSION, SEND_PERMISSION),
            requestCode
        )
    }

    fun checkBeeperInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(BEEPER_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // For older Android versions, battery optimization isn't as aggressive
        }
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val READ_PERMISSION = "com.beeper.android.permission.READ_PERMISSION"
        const val SEND_PERMISSION = "com.beeper.android.permission.SEND_PERMISSION"
        const val BEEPER_PACKAGE = "com.beeper.android"
        const val PERMISSION_REQUEST_CODE = 1001
    }
}
