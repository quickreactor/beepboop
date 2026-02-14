package com.github.quickreactor.beepboop

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LogViewerActivity : AppCompatActivity() {

    private lateinit var logger: FileLogger
    private lateinit var logsTextView: TextView
    private lateinit var copyLogsButton: Button
    private lateinit var clearLogsButton: Button
    private lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_viewer_activity)

        logger = FileLogger.getInstance(this)

        logsTextView = findViewById(R.id.logsTextView)
        copyLogsButton = findViewById(R.id.copyLogsButton)
        clearLogsButton = findViewById(R.id.clearLogsButton)
        closeButton = findViewById(R.id.closeButton)

        loadLogs()

        copyLogsButton.setOnClickListener {
            copyLogs()
        }

        clearLogsButton.setOnClickListener {
            logger.clearLogs()
            loadLogs()
        }

        closeButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadLogs()
    }

    private fun loadLogs() {
        val logs = logger.getLogs()
        if (logs.isBlank()) {
            logsTextView.setText(R.string.no_logs)
        } else {
            logsTextView.text = logs
        }
    }

    private fun copyLogs() {
        val logs = logger.getLogs()
        if (logs.isBlank()) {
            Toast.makeText(this, R.string.no_logs, Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BeepBoop Logs", logs)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, R.string.logs_copied, Toast.LENGTH_SHORT).show()
    }
}
