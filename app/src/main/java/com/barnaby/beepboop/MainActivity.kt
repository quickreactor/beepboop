package com.github.quickreactor.beepboop

import android.content.Intent
import androidx.activity.result.ActivityResult
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var permissionsManager: PermissionsManager
    private val logger by lazy { FileLogger.getInstance(this) }

    // Register activity result launcher for battery optimization settings
    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK || result.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, R.string.battery_optimization_requested, Toast.LENGTH_SHORT).show()
            // Update UI to reflect potential change
            val statusTextView = findViewById<TextView>(R.id.statusTextView)
            val permissionsDetailTextView = findViewById<TextView>(R.id.permissionsDetailTextView)
            val requestPermissionsButton = findViewById<Button>(R.id.requestPermissionsButton)
            val disableBatteryButton = findViewById<Button>(R.id.disableBatteryButton)
            val viewLogsButton = findViewById<Button>(R.id.viewLogsButton)
            val viewRoomsButton = findViewById<Button>(R.id.viewRoomsButton)
            val testMessageButton = findViewById<Button>(R.id.testMessageButton)
            updateUI(statusTextView, permissionsDetailTextView, requestPermissionsButton, disableBatteryButton, viewLogsButton, viewRoomsButton, testMessageButton)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        permissionsManager = PermissionsManager()

        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val permissionsDetailTextView = findViewById<TextView>(R.id.permissionsDetailTextView)
        val viewLogsButton = findViewById<Button>(R.id.viewLogsButton)
        val viewRoomsButton = findViewById<Button>(R.id.viewRoomsButton)
        val testMessageButton = findViewById<Button>(R.id.testMessageButton)
        val disableBatteryButton = findViewById<Button>(R.id.disableBatteryButton)
        val helpButton = findViewById<Button>(R.id.helpButton)
        val requestPermissionsButton = findViewById<Button>(R.id.requestPermissionsButton)

        updateUI(statusTextView, permissionsDetailTextView, requestPermissionsButton, disableBatteryButton, viewLogsButton, viewRoomsButton, testMessageButton)

        viewLogsButton.setOnClickListener {
            val intent = Intent(this, LogViewerActivity::class.java)
            startActivity(intent)
        }

        viewRoomsButton.setOnClickListener {
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
        }

        testMessageButton.setOnClickListener {
            val intent = Intent(this, TestMessageActivity::class.java)
            startActivity(intent)
        }

        disableBatteryButton.setOnClickListener {
            requestIgnoreBatteryOptimizations()
        }

        helpButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        // Initialize battery button state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isBatteryOptDisabled = permissionsManager.isBatteryOptimizationDisabled(this)
            disableBatteryButton.visibility = if (isBatteryOptDisabled) Button.GONE else Button.VISIBLE
        } else {
            disableBatteryButton.visibility = Button.GONE
        }

        requestPermissionsButton.setOnClickListener {
            if (!permissionsManager.checkBeeperInstalled(this)) {
                Toast.makeText(this, R.string.beeper_not_installed, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showPermissionExplanationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val permissionsDetailTextView = findViewById<TextView>(R.id.permissionsDetailTextView)
        val requestPermissionsButton = findViewById<Button>(R.id.requestPermissionsButton)
        val disableBatteryButton = findViewById<Button>(R.id.disableBatteryButton)
        val viewLogsButton = findViewById<Button>(R.id.viewLogsButton)
        val viewRoomsButton = findViewById<Button>(R.id.viewRoomsButton)
        val testMessageButton = findViewById<Button>(R.id.testMessageButton)
        updateUI(statusTextView, permissionsDetailTextView, requestPermissionsButton, disableBatteryButton, viewLogsButton, viewRoomsButton, testMessageButton)
    }

    private fun updateUI(statusTextView: TextView, permissionsDetailTextView: TextView, requestPermissionsButton: Button, disableBatteryButton: Button, viewLogsButton: Button, viewRoomsButton: Button, testMessageButton: Button) {
        logger.logInfo("Updating UI")

        if (!permissionsManager.checkBeeperInstalled(this)) {
            statusTextView.setText(R.string.beeper_not_installed)
            permissionsDetailTextView.text = ""
            requestPermissionsButton.visibility = Button.GONE
            disableBatteryButton.visibility = Button.GONE
            // Disable action buttons when BeepBoop not installed
            viewLogsButton.isEnabled = false
            viewLogsButton.alpha = 0.5f
            viewRoomsButton.isEnabled = false
            viewRoomsButton.alpha = 0.5f
            testMessageButton.isEnabled = false
            testMessageButton.alpha = 0.5f
            return
        }

        val hasPermissions = permissionsManager.hasPermissions(this)
        val status = permissionsManager.getStatus(this)

        if (hasPermissions) {
            statusTextView.setText(R.string.permissions_granted)
            requestPermissionsButton.visibility = Button.GONE
        } else {
            statusTextView.setText(R.string.permissions_not_granted)
            requestPermissionsButton.visibility = Button.VISIBLE
        }

        val readStatus = if (status[PermissionsManager.READ_PERMISSION] == true) getString(R.string.granted) else getString(R.string.denied)
        val sendStatus = if (status[PermissionsManager.SEND_PERMISSION] == true) getString(R.string.granted) else getString(R.string.denied)

        permissionsDetailTextView.text = getString(R.string.permission_status_read, readStatus) + "\n" +
                getString(R.string.permission_status_send, sendStatus)

        // Update battery optimization button visibility
        // Only show if battery optimization is NOT disabled
        val isBatteryOptDisabled = permissionsManager.isBatteryOptimizationDisabled(this)
        if (isBatteryOptDisabled) {
            disableBatteryButton.visibility = Button.GONE
        } else {
            disableBatteryButton.visibility = Button.VISIBLE
        }

        // Enable/disable action buttons based on permission state
        if (hasPermissions) {
            viewLogsButton.isEnabled = true
            viewLogsButton.alpha = 1.0f
            viewRoomsButton.isEnabled = true
            viewRoomsButton.alpha = 1.0f
            testMessageButton.isEnabled = true
            testMessageButton.alpha = 1.0f
        } else {
            viewLogsButton.isEnabled = false
            viewLogsButton.alpha = 0.5f
            viewRoomsButton.isEnabled = false
            viewRoomsButton.alpha = 0.5f
            testMessageButton.isEnabled = false
            testMessageButton.alpha = 0.5f
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_explanation_title)
            .setMessage(R.string.permission_explanation_message)
            .setPositiveButton(R.string.permission_continue_button) { _, _ ->
                permissionsManager.requestPermissions(this, PermissionsManager.PERMISSION_REQUEST_CODE)
            }
            .setCancelable(true)
            .show()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionsManager.PERMISSION_REQUEST_CODE) {
            logger.logInfo("Permission request result received")

            val allGranted = grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                Toast.makeText(this, R.string.permissions_granted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.permissions_not_granted, Toast.LENGTH_SHORT).show()
            }

            val statusTextView = findViewById<TextView>(R.id.statusTextView)
            val permissionsDetailTextView = findViewById<TextView>(R.id.permissionsDetailTextView)
            val requestPermissionsButton = findViewById<Button>(R.id.requestPermissionsButton)
            val disableBatteryButton = findViewById<Button>(R.id.disableBatteryButton)
            val viewLogsButton = findViewById<Button>(R.id.viewLogsButton)
            val viewRoomsButton = findViewById<Button>(R.id.viewRoomsButton)
            val testMessageButton = findViewById<Button>(R.id.testMessageButton)
            updateUI(statusTextView, permissionsDetailTextView, requestPermissionsButton, disableBatteryButton, viewLogsButton, viewRoomsButton, testMessageButton)
        }
    }
}
