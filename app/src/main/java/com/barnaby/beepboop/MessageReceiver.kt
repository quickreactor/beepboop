package com.github.quickreactor.beepboop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MessageReceiver : BroadcastReceiver() {

    private lateinit var logger: FileLogger
    private lateinit var permissionsManager: PermissionsManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        logger = FileLogger.getInstance(context)
        permissionsManager = PermissionsManager()

        logger.logInfo("Received intent: ${intent.action}")

        if (intent.action == ACTION_SEND_MESSAGE) {
            val roomId = intent.getStringExtra(EXTRA_ROOM_ID)
            val message = intent.getStringExtra(EXTRA_MESSAGE)

            logger.logInfo("Processing message send request - Room: $roomId")

            when {
                roomId.isNullOrBlank() -> {
                    logger.logError("Missing or empty roomId")
                    setResultCode(RESULT_ERROR)
                    setResultData("Missing roomId")
                }
                message.isNullOrBlank() -> {
                    logger.logError("Missing or empty message")
                    setResultCode(RESULT_ERROR)
                    setResultData("Missing message")
                }
                !permissionsManager.hasPermissions(context) -> {
                    logger.logError("Permissions not granted")
                    setResultCode(RESULT_ERROR)
                    setResultData("Permissions not granted")
                }
                !permissionsManager.checkBeeperInstalled(context) -> {
                    logger.logError("BeepBoop app not installed")
                    setResultCode(RESULT_ERROR)
                    setResultData("BeepBoop app not installed")
                }
                else -> {
                    logger.logInfo("Starting BeeperService")
                    val serviceIntent = Intent(context, BeeperService::class.java).apply {
                        putExtra(BeeperService.EXTRA_ROOM_ID, roomId)
                        putExtra(BeeperService.EXTRA_MESSAGE, message)
                    }
                    context.startService(serviceIntent)
                    setResultCode(RESULT_OK)
                    setResultData("Message send initiated")
                }
            }
        }
    }

    companion object {
        const val ACTION_SEND_MESSAGE = "SEND_MESSAGE"
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_MESSAGE = "message"
        const val RESULT_OK = -1
        const val RESULT_ERROR = 0
    }
}
