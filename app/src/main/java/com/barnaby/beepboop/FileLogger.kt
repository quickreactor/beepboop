package com.github.quickreactor.beepboop

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileLogger private constructor(context: Context) {
    private val logFile: File
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    init {
        logFile = File(context.filesDir, "beeper_log.txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
    }

    private fun writeLog(level: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] [$level] $message\n"

        try {
            FileWriter(logFile, true).use { writer ->
                writer.write(logEntry)
                writer.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun log(level: String, message: String) {
        writeLog(level, message)
    }

    fun logInfo(message: String) {
        writeLog("INFO", message)
    }

    fun logSuccess(message: String) {
        writeLog("SUCCESS", message)
    }

    fun logError(message: String) {
        writeLog("ERROR", message)
    }

    fun logError(message: String, exception: Throwable) {
        val stackTrace = exception.stackTraceToString()
        writeLog("ERROR", "$message\n$stackTrace")
    }

    fun getLogs(): String {
        return try {
            logFile.readText()
        } catch (e: IOException) {
            "Error reading logs: ${e.message}"
        }
    }

    fun clearLogs() {
        try {
            logFile.writeText("")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun truncateIfNeeded() {
        try {
            if (logFile.length() > MAX_FILE_SIZE) {
                val lines = logFile.readLines()
                if (lines.size > MAX_LINES) {
                    val linesToKeep = lines.takeLast(MAX_LINES)
                    logFile.writeText(linesToKeep.joinToString("\n") + "\n")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val MAX_FILE_SIZE = 1024 * 1024L // 1MB
        private const val MAX_LINES = 1000

        @Volatile
        private var instance: FileLogger? = null

        fun getInstance(context: Context): FileLogger {
            return instance ?: synchronized(this) {
                instance ?: FileLogger(context.applicationContext).also { instance = it }
            }
        }
    }
}
