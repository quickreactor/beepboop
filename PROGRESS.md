# Son of Beeper - Development Status

## Current State (2026-02-12 00:33)

### What's Working ✅
- Intent receiving from Tasker: WORKING
- Room ID extraction: WORKING (same on Android and Desktop)
- Message extraction: WORKING
- Permissions: GRANTED
- App UI: All features implemented and stable
- Room List Viewer: WORKING (can view and copy room IDs)
- Schema Discovery: WORKING (discovered correct column names)

### The Problem ❌
**ContentProvider.insert() returns `null` for all attempts**

---

## Critical Discovery 🎯

### Beeper ContentProvider Schema (from actual data)

**Available columns (18 total):**
```
roomId, originalId, senderContactId, timestamp, isSentByMe, isDeleted,
type, text_content, image_path, image_width, image_height,
video_path, video_width, video_height, order, reactions,
displayName, is_search_match
```

**Sample message data:**
```
roomId = !dK-BNZ1Fe-I0O6u7SN5Km4G6s68:ba_DOnaLUcZcIKV79my_ytGcnreAoU.local-signal.localhost
text_content = Tomorrow i try again for one a day
type = TEXT
isSentByMe = 0
```

### The Root Cause Identified

**We were using wrong column name for message content:**
- ❌ Tried: `text`, `body`, `message` - All failed (columns don't exist)
- ✅ Correct: `text_content` - This is the actual column name!

### Why All Attempts Failed

All 6 column name combinations returned `null` because:
1. `text` column doesn't exist
2. `body` column doesn't exist
3. `message` column doesn't exist

The actual column is named **`text_content`**!

---

## Current Implementation Status

### Files Created/Modified

#### ✅ BeeperService.kt
- **Status:** Needs fix for column name
- **Current code:** Uses `put("text", Uri.encode(message))`
- **Required fix:** Change to `put("text_content", Uri.encode(message))`
- **Has:** Comprehensive schema discovery and testing
- **Has:** 6 alternative column name attempts (all failed)
- **Has:** Detailed logging for debugging

#### ✅ RoomListActivity.kt
- **Status:** WORKING
- **Features:**
  - Queries Beeper's `/chats` endpoint
  - Displays all rooms with room IDs
  - Tap to copy room ID to clipboard
  - Handles errors gracefully

#### ✅ MainActivity.kt
- **Status:** WORKING
- **Features:**
  - Shows permission status
  - View Logs button
  - View Rooms button
  - Request Permissions button

#### ✅ LogViewerActivity.kt
- **Status:** WORKING
- **Features:**
  - View logs
  - Copy logs to clipboard
  - Clear logs
  - Selectable text

#### ✅ MessageReceiver.kt
- **Status:** WORKING
- **Features:**
  - Receives SEND_MESSAGE broadcast
  - Validates permissions
  - Starts BeeperService
  - Removed permission requirement (allows Tasker access)

#### ✅ PermissionsManager.kt
- **Status:** WORKING
- **Features:**
  - Checks Beeper installation
  - Checks READ_PERMISSION and SEND_PERMISSION
  - Requests permissions at runtime

---

## Tasker Configuration

### Working Configuration ✅

**Action:** `SEND_MESSAGE`

**Target:** `Broadcast Receiver`

**Package:** `com.barnaby.sonofbeeper`

**Extras (format with colon, no spaces):**
```
roomId:!eopfbOevRCKijHYFUy:beeper.com
message:Hello
```

**Important:**
- Use `:` separator (not `=`)
- No spaces around separator
- Room ID must be exact match from Room List

---

## Beeper App Information

### Version (from adb)
- **Version Name:** 4.38.3
- **Version Code:** 1744137646
- **Min SDK:** 26
- **Target SDK:** 36

### ContentProvider Details
- **Authority:** `com.beeper.api`
- **Provider Class:** `com.beeper.api.MyContentProvider`
- **Endpoints:**
  - `/chats` - List of rooms/chats
  - `/messages` - List of messages

---

## Development History

### What We've Tried (All Failed)

1. ❌ Query parameters in URI + null ContentValues (documentation approach)
   - Result: NullPointerException crash

2. ❌ ContentValues with `put("text", message)`
   - Result: Returns `null`

3. ❌ ContentValues with `put("text", Uri.encode(message))`
   - Result: Returns `null`

4. ❌ Alternative column names: `room_id`, `body`, `message`
   - Result: Returns `null`

5. ✅ Schema discovery
   - Result: SUCCESS - Found actual column names!

---

## Next Steps (When Resuming)

### Immediate Action Required

**Fix BeeperService.kt line 58:**

```kotlin
// CURRENT (WRONG):
put("text", Uri.encode(message))

// CHANGE TO (CORRECT):
put("text_content", Uri.encode(message))
```

### After Fix Implementation

1. Rebuild and install app
2. Test message send from Tasker
3. Check logs for success or new errors

### If Still Fails (Additional Considerations)

**Potential required columns based on schema:**
- `type` - Should be "TEXT" (from sample data)
- `isSentByMe` - Should be "1" for outgoing messages
- `senderContactId` - Might be required

**Alternative approaches to consider:**
- Try `contentResolver.call()` method instead of `insert()`
- Try different URI (e.g., `/messages/send`)
- Research Beeper's actual API documentation deeper

---

## References

### Official Beeper Documentation
- Integration Guide: Provided by user
- Shows: Query parameters in URI + null ContentValues
- **Issue:** Doesn't match actual implementation (docs may be outdated)

### Key Findings from Testing
1. **URI format:** `content://com.beeper.api/messages` ✅ Correct
2. **Room ID format:** `!room:server.com` ✅ Correct
3. **Message column:** `text_content` (not `text`) ✅ Discovered
4. **Permissions:** READ_PERMISSION and SEND_PERMISSION ✅ Required

---

## Technical Notes

### ContentProvider Behavior Observed
- `query()` - Works, returns data
- `insert()` - Returns `null` (insert fails silently)
- `update()` - Not tested yet
- `call()` - Not tested yet

### Tasker Intent Handling
- Action: `SEND_MESSAGE` - Working
- Extra format with `:` separator - Working
- Extra format with `=` separator - Broken (don't use)

### URL Encoding
- Applied to message text using `Uri.encode(message)`
- Required per Beeper documentation
- Should be kept in final implementation

---

## File Locations

```
D:\Documents\Code\son-of-beeper\
├── app\
│   ├── src\main\
│   │   ├── AndroidManifest.xml
│   │   ├── java\com\barnaby\sonofbeeper\
│   │   │   ├── MainActivity.kt
│   │   │   ├── LogViewerActivity.kt
│   │   │   ├── RoomListActivity.kt
│   │   │   ├── MessageReceiver.kt
│   │   │   ├── BeeperService.kt ← NEEDS FIX (line 58)
│   │   │   ├── PermissionsManager.kt
│   │   │   ├── FileLogger.kt
│   │   │   └── NotificationHelper.kt
│   │   └── res\
│   │       ├── layout\
│   │       │   ├── main_activity.xml
│   │       │   ├── log_viewer_activity.xml
│   │       │   ├── activity_room_list.xml
│   │       │   └── welcome_activity.xml
│   │       └── values\
│   │           └── strings.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Build Information

### Android Configuration
- **compileSdk:** 34
- **minSdk:** 26
- **targetSdk:** 34
- **AGP Version:** 9.0.0
- **Kotlin Version:** 2.2.10

### Dependencies
- `androidx.core:core-ktx:1.12.0`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.11.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`

---

## Current Task

**READY TO FIX:** Change `text` to `text_content` in BeeperService.kt

**Location:** `D:\Documents\Code\son-of-beeper\app\src\main\java\com\barnaby\sonofbeeper\BeeperService.kt`

**Line:** 58 (in `sendMessage()` function)

**Current:**
```kotlin
put("text", Uri.encode(message))
```

**Required:**
```kotlin
put("text_content", Uri.encode(message))
```

---

*Last updated: 2026-02-12 00:33*
*Status: Ready to implement critical fix*
