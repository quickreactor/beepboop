package com.github.quickreactor.beepboop

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val logger = FileLogger.getInstance(context)
        val permissionsManager = PermissionsManager()
        val notificationHelper = NotificationHelper(context)

        logger.logInfo("Received intent: ${intent.action}")

        if (intent.action != ACTION_SEND_MESSAGE) {
            return
        }

        val roomId = intent.getStringExtra(EXTRA_ROOM_ID)
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        logger.logInfo("Processing message send request - Room: $roomId")

        // Use goAsync() to perform async work beyond the receiver's lifecycle
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when {
                    roomId.isNullOrBlank() -> {
                        logger.logError("Missing or empty roomId")
                        notificationHelper.showFailureNotification(
                            "Unknown",
                            "Missing roomId"
                        )
                    }
                    message.isNullOrBlank() -> {
                        logger.logError("Missing or empty message")
                        notificationHelper.showFailureNotification(
                            roomId ?: "Unknown",
                            "Missing message"
                        )
                    }
                    !permissionsManager.hasPermissions(context) -> {
                        logger.logError("Permissions not granted")
                        notificationHelper.showFailureNotification(
                            roomId ?: "Unknown",
                            "Permissions not granted"
                        )
                    }
                    !permissionsManager.checkBeeperInstalled(context) -> {
                        logger.logError("BeepBoop app not installed")
                        notificationHelper.showFailureNotification(
                            roomId ?: "Unknown",
                            "BeepBoop app not installed"
                        )
                    }
                    else -> {
                        logger.logInfo("Sending message to room: $roomId")
                        logger.logInfo("Message text: $message")
                        logger.truncateIfNeeded()

                        // Insert message via BeepBoop ContentProvider
                        val uri = ("content://com.beeper.api/messages?" +
                                   "roomId=$roomId&text=${Uri.encode(message)}").toUri()
                        val values = ContentValues()  // Empty, not null

                        logger.logInfo("Sending message via BeepBoop ContentProvider")

                        try {
                            val result = context.contentResolver.insert(uri, values)

                            if (result != null) {
                                val sentRoomId = result.getQueryParameter("roomId")
                                val messageId = result.getQueryParameter("messageId")
                                logger.logSuccess("Message sent successfully")
                                logger.logInfo("Room: $sentRoomId, Message ID: $messageId")
                                broadcastResult(context, true, "Message sent successfully")
                            } else {
                                logger.logError("Failed to send message: contentResolver.insert() returned null")
                                notificationHelper.showFailureNotification(
                                    roomId,
                                    "Failed to send message - insert returned null"
                                )
                                broadcastResult(context, false, "Message send failed - returned null")
                            }
                        } catch (e: Exception) {
                            logger.logError("Exception while sending message", e)
                            notificationHelper.showFailureNotification(
                                roomId,
                                "Exception: ${e.message}"
                            )
                            broadcastResult(context, false, "Exception: ${e.message}")
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun broadcastResult(context: Context, success: Boolean, message: String) {
        val intent = Intent(ACTION_SEND_RESULT).apply {
            putExtra(EXTRA_SUCCESS, success)
            putExtra(EXTRA_MESSAGE_RESULT, message)
        }
        context.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_SEND_MESSAGE = "SEND_MESSAGE"
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_MESSAGE = "message"
        const val RESULT_OK = -1
        const val RESULT_ERROR = 0
        const val ACTION_SEND_RESULT = "com.github.quickreactor.beepboop.SEND_RESULT"
        const val EXTRA_SUCCESS = "success"
        const val EXTRA_MESSAGE_RESULT = "message"
    }
}
