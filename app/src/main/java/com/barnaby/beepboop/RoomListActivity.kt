package com.github.quickreactor.beepboop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Data class for a room
data class Room(
    val roomId: String,
    val name: String,
    val protocol: String?
)

class RoomListActivity : AppCompatActivity() {

    private lateinit var logger: FileLogger
    private lateinit var recyclerView: RecyclerView
    private lateinit var closeButton: Button
    private val roomsList = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        logger = FileLogger.getInstance(this)
        recyclerView = findViewById(R.id.roomsRecyclerView)
        closeButton = findViewById(R.id.closeButton)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RoomAdapter(roomsList) { roomId ->
            copyToClipboard(roomId)
        }

        loadRooms()

        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun loadRooms() {
        logger.logInfo("Loading rooms from BeepBoop")

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
                        // Use title if name is null or empty
                        val displayName = when {
                            !name.isNullOrBlank() -> name
                            !title.isNullOrBlank() -> title
                            else -> "Unknown"
                        }
                        roomsList.add(Room(roomId, displayName, protocol))
                        logger.logInfo("Room: $displayName - $roomId (protocol: $protocol)")
                    }
                }

                logger.logInfo("Loaded ${roomsList.size} rooms")

                // Notify adapter of data changes
                recyclerView.adapter?.notifyDataSetChanged()

                if (roomsList.isEmpty()) {
                    showEmptyState()
                }
            } ?: run {
                logger.logError("Failed to query rooms: cursor is null")
                showErrorToast(R.string.query_failed)
            }
        } catch (e: Exception) {
            logger.logError("Exception while loading rooms", e)
            showErrorToast(R.string.query_failed)
        }
    }

    private fun showEmptyState() {
        // Adapter will handle empty state via placeholder view
    }

    private fun copyToClipboard(roomId: String) {
        val clipboard = getSystemService(android.content.ClipboardManager::class.java)
        val clip = android.content.ClipData.newPlainText("Room ID", roomId)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, getString(R.string.room_id_copied), Toast.LENGTH_SHORT).show()
    }

    private fun showErrorToast(messageResId: Int) {
        Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show()
    }

    // ViewHolder for room items
    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val networkIcon: ImageView = itemView.findViewById(R.id.networkIcon)
        val roomName: TextView = itemView.findViewById(R.id.roomName)
        val roomDetails: TextView = itemView.findViewById(R.id.roomDetails)
    }

    // Adapter for RecyclerView
    class RoomAdapter(
        private val rooms: List<Room>,
        private val onRoomClick: (String) -> Unit
    ) : RecyclerView.Adapter<RoomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.room_list_item, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            val room = rooms[position]
            holder.roomName.text = room.name

            // Get network icon drawable
            val iconDrawable = getNetworkIconDrawable(room.protocol)
            holder.networkIcon.setImageResource(iconDrawable)

            // Set room details (network name + room ID preview)
            val networkName = getNetworkName(room.protocol)
            val roomIdPreview = if (room.roomId.length > 30) {
                "${room.roomId.take(30)}..."
            } else {
                room.roomId
            }
            holder.roomDetails.text = "$networkName • $roomIdPreview"

            // Set click listener
            holder.itemView.setOnClickListener {
                onRoomClick(room.roomId)
            }
        }

        override fun getItemCount(): Int = rooms.size

        private fun getNetworkIconDrawable(protocol: String?): Int {
            return when (protocol?.lowercase()) {
                "whatsapp", "whatsap", "whatsappmulti" -> R.drawable.ic_network_whatsapp
                "telegram" -> R.drawable.ic_network_telegram
                "signal" -> R.drawable.ic_network_signal
                "discord", "discordgo", "discord-go" -> R.drawable.ic_network_discord
                "slack" -> R.drawable.ic_network_slack
                "gmessages", "gmessages-sms", "googlemessages", "gmessages-rcs" -> R.drawable.ic_network_gmessages
                "messenger", "facebookgo", "fb-messenger", "facebook-go" -> R.drawable.ic_network_messenger
                "instagram", "instagramgo", "instagram-go" -> R.drawable.ic_network_instagram
                "beeper", "matrix" -> R.drawable.ic_network_matrix
                else -> R.drawable.ic_network_unknown
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
                "beeper" -> "Beeper"
                "matrix" -> "Matrix"
                else -> if (protocol.isNullOrBlank()) "Unknown" else protocol
            }
        }
    }
}
