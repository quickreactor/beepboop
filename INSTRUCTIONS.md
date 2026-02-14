# Son of Beeper - Instructions

## Quick Start

### 1. Build the App

#### Windows
```bash
gradlew.bat assembleDebug
```

#### Mac/Linux
```bash
chmod +x gradlew
./gradlew assembleDebug
```

The APK will be created at: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Install on Your Device

1. Enable "Install from unknown sources" in your device settings
2. Transfer the APK to your device
3. Tap the APK file to install

### 3. Grant Permissions

1. Open the "Son of Beeper" app
2. You'll see a welcome screen - tap "Grant Permissions"
3. Approve the Beeper permission requests
4. You should see "Permissions granted! Son of Beeper is ready to use."

### 4. Configure Tasker

Create a new Tasker task with these settings:

**Intent Action:** `SEND_MESSAGE`

**Extras:**
- `roomId` - Your Beeper room ID (String)
- `message` - Your message text (String)

**Example Tasker Configuration:**
```
Action: Send Intent
Action: SEND_MESSAGE
Extra: roomId:!your:room.id:server.com
Extra: message:Hello from Tasker!
Target: Broadcast Receiver
```

## Finding Your Room ID

You can find your Beeper room ID by:

1. **Open the chat** in Beeper
2. **Tap the chat name/header** to open chat details
3. **Look for Room ID** in the information section
4. It usually looks like: `!aBcDeFgHiJkLmNoPqRsT:matrix.org`

## Testing

1. Create a Tasker task with a test message
2. Run the task
3. Check Beeper to see if the message was sent
4. Open Son of Beeper and tap "View Logs" to see the activity

## Troubleshooting

### "Beeper app not installed"
- Make sure you have the official Beeper app installed
- Beeper must be logged in

### "Permissions not granted"
- Open Son of Beeper and tap "Request Permissions"
- Ensure both READ and SEND permissions are granted

### Message not sending
- Check that the room ID is correct (case-sensitive!)
- Open Son of Beeper → View Logs to see error details
- Make sure Beeper has an internet connection

### Notifications not appearing
- Android 13+ requires notification permission
- Go to Settings → Apps → Son of Beeper → Notifications
- Enable "Allow notifications"

## Features

- **Send messages via Tasker** - Use Tasker to send Beeper messages automatically
- **Room ID support** - Send to any Beeper room by ID
- **File logging** - All activity is logged for debugging
- **Failure notifications** - Get notified if a message fails to send
- **Log viewer** - View logs directly in the app

## File Locations

- **App data**: `/data/data/com.barnaby.sonofbeeper/`
- **Log file**: `/data/data/com.barnaby.sonofbeeper/files/beeper_log.txt`
- **APK**: `app/build/outputs/apk/debug/app-debug.apk`

## Tasker Example Use Cases

### Send "I'm home" when connected to home WiFi
1. Create a Profile: State → Net → WiFi Connected → [Your Home WiFi]
2. Create a Task:
   - Action: Send Intent
   - Action: `SEND_MESSAGE`
   - Extra `roomId`: `!partner:chat.id`
   - Extra `message`: I'm home!

### Send location when battery is low
1. Create a Profile: State → Power → Battery Level < 15%
2. Create a Task:
   - Action: Get Location
   - Action: Send Intent
   - Action: `SEND_MESSAGE`
   - Extra `roomId`: `!partner:chat.id`
   - Extra `message`: My battery is low! Location: %LOC

### Send message at a specific time
1. Create a Profile: Time → 9:00 AM
2. Create a Task:
   - Action: Send Intent
   - Action: `SEND_MESSAGE`
   - Extra `roomId`: `!work:chat.id`
   - Extra `message`: Good morning team!

## Support

For issues or questions:
- Check the logs in the Son of Beeper app
- Verify Beeper permissions are granted
- Ensure room ID is correct

## Version

Version: 1.0
Package: com.barnaby.sonofbeeper
