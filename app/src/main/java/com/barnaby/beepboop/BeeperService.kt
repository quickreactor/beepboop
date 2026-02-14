package com.github.quickreactor.beepboop

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.net.toUri

class BeeperService : Service() {

    private lateinit var logger: FileLogger
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        logger = FileLogger.getInstance(this)
        notificationHelper = NotificationHelper(this)
        logger.logInfo("BeeperService created")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val roomId = it.getStringExtra(EXTRA_ROOM_ID)
            val message = it.getStringExtra(EXTRA_MESSAGE)

            logger.logInfo("Received roomId: $roomId")
            logger.logInfo("Received message: $message")

            if (roomId != null && message != null) {
                sendMessage(roomId, message)
            } else {
                logger.logError("Missing roomId or message in intent")
                notificationHelper.showFailureNotification(
                    "Unknown",
                    "Missing roomId or message parameter"
                )
                broadcastResult(false, "Missing parameters")
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }

    private fun sendMessage(roomId: String, message: String) {
        logger.logInfo("Sending message to room: $roomId")
        logger.logInfo("Message text: $message")
        logger.truncateIfNeeded()

        // Working method: query params in URI + empty ContentValues
        // Note: BeepBoop docs show null ContentValues, but v4.38.3 requires empty ContentValues()
        val uri = ("content://com.beeper.api/messages?" +
                   "roomId=$roomId&text=${Uri.encode(message)}").toUri()
        val values = ContentValues()  // Empty, not null

        logger.logInfo("Sending message via BeepBoop ContentProvider")

        try {
            val result = contentResolver.insert(uri, values)

            if (result != null) {
                val sentRoomId = result.getQueryParameter("roomId")
                val messageId = result.getQueryParameter("messageId")
                logger.logSuccess("Message sent successfully")
                logger.logInfo("Room: $sentRoomId, Message ID: $messageId")
                broadcastResult(true, "Message sent successfully")
            } else {
                logger.logError("Failed to send message: contentResolver.insert() returned null")
                notificationHelper.showFailureNotification(
                    roomId,
                    "Failed to send message - insert returned null"
                )
                broadcastResult(false, "Message send failed - returned null")
            }
        } catch (e: Exception) {
            logger.logError("Exception while sending message", e)
            notificationHelper.showFailureNotification(
                roomId,
                "Exception: ${e.message}"
            )
            broadcastResult(false, "Exception: ${e.message}")
        }
    }

    private fun broadcastResult(success: Boolean, message: String) {
        val intent = Intent(ACTION_SEND_RESULT).apply {
            putExtra(EXTRA_SUCCESS, success)
            putExtra(EXTRA_MESSAGE_RESULT, message)
        }
        sendBroadcast(intent)
    }

    companion object {
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_MESSAGE = "message"
        const val ACTION_SEND_RESULT = "com.github.quickreactor.beepboop.SEND_RESULT"
        const val EXTRA_SUCCESS = "success"
        const val EXTRA_MESSAGE_RESULT = "message"
    }
}
