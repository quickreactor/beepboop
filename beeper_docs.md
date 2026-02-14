Content Providers

Query chats, messages, and contacts from Beeper using Android's ContentProvider APIs

Build Android apps and connected devices that integrate with Beeper using standard Android ContentProvider APIs. Content providers encapsulate data and provide mechanisms for defining data security, serving as the standard interface between processes.

Experimental

Content providers are experimental and APIs are subject to change.
What you can build

    Universal Search - Search across all chats and messages
    Widgets & Dashboards - Display chat summaries and unread counts
    Automation - Send messages and react to changes
    Wearables - Integrate with watches and IoT devices

Key features

    Authority: com.beeper.api
    Permissions: Runtime (request at first use)
    Data Access: Chats, messages, contacts
    Operations: Query, insert, observe changes
    Protocol Support: WhatsApp, Telegram, Signal, and more

Quick start

    Add permissions to your manifest
    AndroidManifest.xml

    <uses-permission android:name="com.beeper.android.permission.READ_PERMISSION" />
    <uses-permission android:name="com.beeper.android.permission.SEND_PERMISSION" />

    These are custom Beeper permissions. Declare them in the manifest and request them at runtime with a permission prompt.

    // Request at runtime (e.g., in an Activity or Fragment)
    val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val hasRead = results["com.beeper.android.permission.READ_PERMISSION"] == true
        val hasSend = results["com.beeper.android.permission.SEND_PERMISSION"] == true
        // Handle granted/denied states
    }

    requestPermissions.launch(arrayOf(
        "com.beeper.android.permission.READ_PERMISSION",
        "com.beeper.android.permission.SEND_PERMISSION"
    ))

    Query recent chats

    import androidx.core.net.toUri

    val uri = "content://com.beeper.api/chats?limit=50".toUri()
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val titleIdx = cursor.getColumnIndexOrThrow("title")
        val previewIdx = cursor.getColumnIndexOrThrow("messagePreview")
        val unreadIdx = cursor.getColumnIndexOrThrow("unreadCount")

        while (cursor.moveToNext()) {
            val title = cursor.getString(titleIdx)
            val preview = cursor.getString(previewIdx)
            val unread = cursor.getInt(unreadIdx)
            // Display chat info in your UI
        }
    }

    Send a message

    import android.net.Uri
    import androidx.core.net.toUri

    val message = "Hello from my app!"
    val roomId = "!room:server.com"

    val result = contentResolver.insert(
        ("content://com.beeper.api/messages?" +
         "roomId=$roomId&text=${Uri.encode(message)}").toUri(),
        null
    )

    result?.let {
        val messageId = it.getQueryParameter("messageId")
        // Message sent successfully
    }

    Observe changes

    import android.os.Handler
    import android.os.Looper
    import android.database.ContentObserver
    import androidx.core.net.toUri

    contentResolver.registerContentObserver(
        "content://com.beeper.api/chats".toUri(),
        true,
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                // Re-query and refresh your UI
            }
        }
    )

    Only content://com.beeper.api/chats currently emits change notifications. Observers for messages or contacts will not fire.

Common use cases

    Unread Counter
    Search Messages
    Protocol Filter

// Get total unread count across all chats
val uri = "content://com.beeper.api/chats/count?isUnread=1".toUri()
val cursor = contentResolver.query(uri, null, null, null, null)
val unreadTotal = cursor?.use {
    if (it.moveToFirst()) {
        it.getInt(it.getColumnIndexOrThrow("count"))
    } else 0
} ?: 0

Performance tips

Best practices

    Always Set Limits - Default limit is 100, but always specify limit to avoid large queries
    Use Filters - Filter by roomIds, protocol, or other parameters to reduce data
    Batch Operations - Combine multiple filters in one query instead of multiple queries
    Lifecycle Awareness - Register/unregister observers with your component lifecycle

Supported protocols
Protocol	Identifier	Features
WhatsApp	whatsapp	Full
Telegram	telegram	Full
Signal	signal	Full
Matrix/Beeper	beeper, matrix	Full
Discord	discord	Full
Slack	slack	Full
Google Messages	gmessages	Full
Others	Various	Query




API Reference

Complete reference for Beeper Content Provider APIs
Base configuration
Setting	Value
Authority	com.beeper.api
Default Limit	100 rows (if not specified)
Required Permissions	READ_PERMISSION, SEND_PERMISSION

READ_PERMISSION and SEND_PERMISSION are runtime permissions and must be requested via an in-app prompt.
Chats API
List chats

Retrieves a list of chats/conversations.

    Endpoint
    Parameters
    Columns
    Example

import androidx.core.net.toUri

val cursor = contentResolver.query(
    "content://com.beeper.api/chats?limit=50&isUnread=1".toUri(),
    null, null, null, null
)

cursor?.use { c ->
    val roomIdIdx = c.getColumnIndexOrThrow("roomId")
    val titleIdx = c.getColumnIndexOrThrow("title")
    val unreadIdx = c.getColumnIndexOrThrow("unreadCount")

    while (c.moveToNext()) {
        val roomId = c.getString(roomIdIdx)
        val title = c.getString(titleIdx)
        val unread = c.getInt(unreadIdx)
        println("$title: $unread unread")
    }
}

Count chats

Get the total count of chats matching filters.

    Endpoint
    Parameters
    Example

val cursor = contentResolver.query(
    "content://com.beeper.api/chats/count?isUnread=1".toUri(),
    null, null, null, null
)

val count = cursor?.use {
    if (it.moveToFirst()) {
        it.getInt(it.getColumnIndexOrThrow("count"))
    } else 0
} ?: 0

Messages API
List messages

Retrieves messages with optional filtering and search.

    Endpoint
    Parameters
    Columns
    Search Example
    OpenAtUnread Example

// Search with context
val searchTerm = "important"
val uri = ("content://com.beeper.api/messages?" +
          "query=${Uri.encode(searchTerm)}" +
          "&contextBefore=3&contextAfter=2").toUri()

contentResolver.query(uri, null, null, null, null)?.use { cursor ->
    val textIdx = cursor.getColumnIndexOrThrow("text_content")
    val matchIdx = cursor.getColumnIndexOrThrow("is_search_match")

    while (cursor.moveToNext()) {
        val text = cursor.getString(textIdx)
        val isMatch = cursor.getInt(matchIdx) == 1

        if (isMatch) {
            // This is the matching message
            println("MATCH: $text")
        } else {
            // This is context
            println("Context: $text")
        }
    }
}

Count messages

Get total message count with filters.

    Endpoint
    Example

content://com.beeper.api/messages/count

Reactions format

The reactions column encodes reactions as comma-separated entries:

Format

emoji|senderId|isSentByMe|order

Example: 😀|@alice:server.com|0|123,👍|@me:server.com|1|124

// Parse reactions
val reactionsRaw = cursor.getString(cursor.getColumnIndexOrThrow("reactions")) ?: ""
val reactions = reactionsRaw.split(',')
    .filter { it.isNotEmpty() }
    .map { part ->
        val fields = part.split('|')
        Reaction(
            emoji = fields.getOrNull(0) ?: "",
            senderId = fields.getOrNull(1) ?: "",
            isSentByMe = fields.getOrNull(2) == "1",
            order = fields.getOrNull(3)?.toLongOrNull() ?: 0
        )
    }

Send message

Send a text message to a chat.

    Endpoint
    Parameters
    Return Value
    Example

import android.net.Uri
import androidx.core.net.toUri

val message = "Hello World!"
val roomId = "!room:server.com"

val resultUri = contentResolver.insert(
    ("content://com.beeper.api/messages?" +
     "roomId=$roomId&text=${Uri.encode(message)}").toUri(),
    null
)

if (resultUri != null) {
    val sentRoomId = resultUri.getQueryParameter("roomId")
    val messageId = resultUri.getQueryParameter("messageId")
    println("Sent message $messageId to $sentRoomId")
} else {
    println("Failed to send message")
}

Contacts API
List contacts

Retrieve contacts with optional filtering.

    Endpoint
    Parameters
    Columns
    Example

val cursor = contentResolver.query(
    "content://com.beeper.api/contacts?query=John".toUri(),
    null, null, null, null
)

cursor?.use { c ->
    val idIdx = c.getColumnIndexOrThrow("id")
    val nameIdx = c.getColumnIndexOrThrow("displayName")
    val roomsIdx = c.getColumnIndexOrThrow("roomIds")

    while (c.moveToNext()) {
        val contactId = c.getString(idIdx)
        val name = c.getString(nameIdx)
        val rooms = c.getString(roomsIdx).split(",")

        println("$name is in ${rooms.size} rooms")
    }
}

Count contacts

Get total contact count with filters.

    Endpoint
    Example

val count = contentResolver.query(
    "content://com.beeper.api/contacts/count?protocol=whatsapp".toUri(),
    null, null, null, null
)?.use { cursor ->
    if (cursor.moveToFirst()) {
        cursor.getInt(cursor.getColumnIndexOrThrow("count"))
    } else 0
} ?: 0

Common URI examples

# Recent unread chats
content://com.beeper.api/chats?isUnread=1&limit=10

# WhatsApp chats only
content://com.beeper.api/chats?protocol=whatsapp

# Search messages for "meeting"
content://com.beeper.api/messages?query=meeting

# Active chats (not archived or low priority)
content://com.beeper.api/chats?

Integration Guide

Permissions, notifications, troubleshooting, and best practices for Beeper Content Provider integration
Permissions
Required permissions

Beeper uses custom Android permissions that must be declared in your app’s manifest:

    Manifest Declaration
    Permission Details
    Checking & Requesting

AndroidManifest.xml

<uses-permission android:name="com.beeper.android.permission.READ_PERMISSION" />
<uses-permission android:name="com.beeper.android.permission.SEND_PERMISSION" />

Users can deny permissions; handle gracefully and explain limited functionality if not granted.

See Android’s uses-permission and permission documentation.
Change notifications
ContentObserver setup

Use ContentObserver to react to data changes in real-time:

    Create an observer

    class ChatObserver(handler: Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            // Data changed - refresh your UI
            refreshChatList()
        }
    }

    Register the observer

    import androidx.core.net.toUri

    val observer = ChatObserver(Handler(Looper.getMainLooper()))

    contentResolver.registerContentObserver(
        "content://com.beeper.api/chats".toUri(),
        true,  // Notify for descendant URIs
        observer
    )

    Unregister when done

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(observer)
    }

Common observer patterns

    Lifecycle-Aware
    Current support

class ChatViewModel : ViewModel() {
    private var observer: ContentObserver? = null

    fun startObserving(contentResolver: ContentResolver) {
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                loadChats()
            }
        }

        contentResolver.registerContentObserver(
            "content://com.beeper.api/chats".toUri(),
            true,
            observer!!
        )
    }

    override fun onCleared() {
        observer?.let {
            getApplication<Application>()
                .contentResolver
                .unregisterContentObserver(it)
        }
    }
}

Some Cursor implementations auto-update, but explicit observers ensure consistent behavior across Android versions.
Troubleshooting
Common issues and solutions
No rows returned

Causes:

    Beeper not installed/logged in
    Missing permissions
    Too restrictive filters

Solutions:

    Verify Beeper installation
    Check manifest permissions
    Add limit parameter
    Adjust filters

Permission denied

Causes:

    Permissions not declared in manifest
    Runtime permissions not granted

Solutions:

    Add permissions to manifest
    Request permissions at runtime

Message send fails

Causes:

    Invalid room ID
    Text not URL-encoded
    Network issues

Solutions:

    Verify roomId format
    Use Uri.encode() for text
    Check Beeper connectivity

UI not updating

Causes:

    No ContentObserver registered
    Wrong Handler/Looper
    Observer not on main thread

Solutions:

    Register ContentObserver
    Use Handler(Looper.getMainLooper())
    Refresh on main thread

Debug checklist

    Verify Beeper installation

    val packageManager = context.packageManager
    try {
        packageManager.getPackageInfo("com.beeper.android", 0)
        // Beeper is installed
    } catch (e: PackageManager.NameNotFoundException) {
        // Beeper not installed
    }

    Check permissions

    val permissions = listOf(
        "com.beeper.android.permission.READ_PERMISSION",
        "com.beeper.android.permission.SEND_PERMISSION"
    )

    permissions.forEach { permission ->
        val granted = checkSelfPermission(permission) ==
                      PackageManager.PERMISSION_GRANTED
        Log.d("Permissions", "$permission: $granted")
    }

    Test basic query

    try {
        val cursor = contentResolver.query(
            "content://com.beeper.api/chats?limit=1".toUri(),
            null, null, null, null
        )
        Log.d("Debug", "Query returned ${cursor?.count ?: 0} rows")
        cursor?.close()
    } catch (e: Exception) {
        Log.e("Debug", "Query failed", e)
    }

Frequently asked questions

    General
    Technical
    Performance

What’s the authority?
    com.beeper.api
Do I need runtime permissions?
    Yes. Request READ_PERMISSION and SEND_PERMISSION at runtime in addition to declaring them in the manifest.
What’s the default limit?
    Messages and contacts default to 100 if not specified. Always specify limit for chats to avoid large queries.
Can I use this from a service?
    Yes, ContentProviders work from any Android component with a Context.

Best practices
Always URL encode

// ✅ Good
Uri.encode("Hello & welcome!")

// ❌ Bad
"Hello & welcome!"

Use lifecycle components

// Register in onStart
override fun onStart() {
    super.onStart()
    registerObserver()
}

// Unregister in onStop
override fun onStop() {
    super.onStop()
    unregisterObserver()
}

Handle null results

// Always check for null
val result = contentResolver.insert(uri, null)
if (result != null) {
    // Success
} else {
    // Handle failure
}

Batch operations

// ✅ Single query with multiple IDs
"roomIds=!room1:server,!room2:server"

// ❌ Multiple queries
query("roomIds=!room1:server")
query("roomIds=!room2:server")

Testing Tips

Development testing

    Use Android Studio’s Database Inspector to view ContentProvider data
    Test with different Beeper account states (logged out, no chats, many chats)
    Verify permission handling by testing without declaring permissions
    Test observer behavior by sending messages from another device
    Check edge cases like empty results, special characters, long text
