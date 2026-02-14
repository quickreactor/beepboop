package com.github.quickreactor.beepboop

import android.content.Intent
import androidx.activity.result.ActivityResult
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class HelpActivity : AppCompatActivity() {

    // Register activity result launcher for battery optimization settings
    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK || result.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, R.string.battery_optimization_requested, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val disableBatteryButton = findViewById<Button>(R.id.disableBatteryButton)
        val closeButton = findViewById<Button>(R.id.closeButton)

        // Set up battery optimization button
        disableBatteryButton.setOnClickListener {
            requestIgnoreBatteryOptimizations()
        }

        // Check if battery optimizations are already disabled
        updateBatteryButtonState(disableBatteryButton)

        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:${packageName}")

                // Check if intent can be handled
                if (intent.resolveActivity(packageManager) != null) {
                    batteryOptimizationLauncher.launch(intent)
                } else {
                    // Fallback to app settings
                    openAppSettings()
                }
            } catch (e: Exception) {
                openAppSettings()
            }
        } else {
            // For older Android versions, battery optimization isn't as aggressive
            Toast.makeText(this, R.string.battery_not_needed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${packageName}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.couldnot_open_settings, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateBatteryButtonState(button: Button) {
        // Try to determine if battery optimization is disabled
        // This isn't directly accessible, so we'll update button text
        // to indicate it's been requested
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.text = getString(R.string.disable_battery_optimization)
        } else {
            button.visibility = Button.GONE
        }
    }
}
