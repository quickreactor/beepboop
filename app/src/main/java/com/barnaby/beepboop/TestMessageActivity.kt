package com.github.quickreactor.beepboop

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import android.content.ContentValues

class TestMessageActivity : AppCompatActivity() {

    private lateinit var logger: FileLogger
    private lateinit var permissionsManager: PermissionsManager
    private val roomsList = mutableListOf<Room>()
    private var selectedRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_message)

        logger = FileLogger.getInstance(this)
        permissionsManager = PermissionsManager()

        val roomSpinner = findViewById<Spinner>(R.id.roomSpinner)
        val customRoomCheckBox = findViewById<CheckBox>(R.id.customRoomCheckBox)
        val customRoomEditText = findViewById<EditText>(R.id.customRoomEditText)
        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val closeButton = findViewById<Button>(R.id.closeButton)

        // Load rooms for spinner
        loadRooms(roomSpinner)

        // Room spinner selection
        roomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < roomsList.size) {
                    selectedRoomId = roomsList[position].roomId
                    logger.logInfo("Selected room: ${roomsList[position].name}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedRoomId = null
            }
        }

        // Custom room checkbox toggle
        customRoomCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customRoomEditText.visibility = View.VISIBLE
                roomSpinner.isEnabled = false
                roomSpinner.alpha = 0.5f
            } else {
                customRoomEditText.visibility = View.GONE
                roomSpinner.isEnabled = true
                roomSpinner.alpha = 1.0f
            }
        }

        // Send button
        sendButton.setOnClickListener {
            sendMessage(customRoomEditText, messageEditText)
        }

        // Close button
        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun loadRooms(spinner: Spinner) {
        logger.logInfo("Loading rooms for test message")

        if (!permissionsManager.hasPermissions(this)) {
            Toast.makeText(this, R.string.permissions_not_granted, Toast.LENGTH_LONG).show()
            showErrorState(spinner)
            return
        }

        try {
            val cursor = contentResolver.query(
                "content://com.beeper.api/chats".toUri(),
                arrayOf("roomId", "name", "title", "protocol"),
                null, null,
                "timestamp DESC LIMIT 20"
            )

            cursor?.use {
                val roomIdIndex = it.getColumnIndex("roomId")
                val nameIndex = it.getColumnIndex("name")
                val titleIndex = it.getColumnIndex("title")
                val protocolIndex = it.getColumnIndex("protocol")

                while (it.moveToNext()) {
                    val roomId = if (roomIdIndex >= 0) it.getString(roomIdIndex) else null
                    val name = if (nameIndex >= 0) it.getString(nameIndex) else null
                    val title = if (titleIndex >= 0) it.getString(titleIndex) else null
                    val protocol = if (protocolIndex >= 0) it.getString(protocolIndex) else null

                    if (roomId != null) {
                        val displayName = when {
                            !name.isNullOrBlank() -> name
                            !title.isNullOrBlank() -> title
                            else -> "Unknown"
                        }
                        roomsList.add(Room(roomId, displayName, protocol))
                    }
                }

                logger.logInfo("Loaded ${roomsList.size} rooms for spinner")

                // Create adapter with room names
                val roomNames = roomsList.map { "${it.name} (${getNetworkName(it.protocol)})" }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                if (roomsList.isNotEmpty()) {
                    selectedRoomId = roomsList[0].roomId
                }
            } ?: run {
                logger.logError("Failed to query rooms: cursor is null")
                showErrorState(spinner)
            }
        } catch (e: Exception) {
            logger.logError("Exception while loading rooms", e)
            showErrorState(spinner)
        }
    }

    private fun showErrorState(spinner: Spinner) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(getString(R.string.no_rooms_loaded)))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.isEnabled = false
    }

    private fun sendMessage(customRoomEditText: EditText, messageEditText: EditText) {
        val roomId = if (customRoomEditText.visibility == View.VISIBLE) {
            customRoomEditText.text.toString().trim()
        } else {
            selectedRoomId
        }

        val message = messageEditText.text.toString().trim()

        // Validation
        if (roomId.isNullOrBlank()) {
            Toast.makeText(this, R.string.error_no_room, Toast.LENGTH_SHORT).show()
            return
        }

        if (message.isBlank()) {
            Toast.makeText(this, R.string.error_no_message, Toast.LENGTH_SHORT).show()
            return
        }

        logger.logInfo("Sending test message to room: $roomId")

        // Send message using same method as BeeperService
        val uri = ("content://com.beeper.api/messages?" +
                   "roomId=$roomId&text=${android.net.Uri.encode(message)}").toUri()
        val values = ContentValues()

        try {
            val result = contentResolver.insert(uri, values)

            if (result != null) {
                logger.logSuccess("Test message sent successfully")
                Toast.makeText(this, R.string.message_sent_success, Toast.LENGTH_SHORT).show()
                messageEditText.text.clear()
            } else {
                logger.logError("Failed to send test message: insert returned null")
                Toast.makeText(this, R.string.message_send_failed, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            logger.logError("Exception while sending test message", e)
            Toast.makeText(this, "${getString(R.string.message_send_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getNetworkName(protocol: String?): String {
        return when (protocol?.lowercase()) {
            "whatsapp", "whatsappmulti" -> "WhatsApp"
            "telegram" -> "Telegram"
            "signal" -> "Signal"
            "discord", "discordgo", "discord-go" -> "Discord"
            "slack" -> "Slack"
            "gmessages", "gmessages-sms", "googlemessages", "gmessages-rcs" -> "Google Messages"
            "messenger", "facebookgo", "fb-messenger", "facebook-go" -> "Facebook Messenger"
            "instagram", "instagramgo", "instagram-go" -> "Instagram"
            "beeper" -> "BeepBoop"
            "matrix" -> "Matrix"
            else -> if (protocol.isNullOrBlank()) "Unknown" else protocol
        }
    }
}
