# BeepBoop

> Automate sending messages on Beeper with Tasker

An Android app that allows sending Beeper messages via Tasker intents or built-in test message interface.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)

## Instructions

### 1. Install BeepBoop and Grant Permissions

**From GitHub Releases (Recommended)**
1. Download the latest APK from [Releases](../../releases)
2. Enable "Install from unknown sources" in your device settings
3. Install the downloaded APK
4. Open BeepBoop

**Grant Required Permissions:**
1. Tap "Grant Permissions" when prompted
2. Approve both BeepBoop permissions:
   - READ_PERMISSION - Access your BeepBoop chats and messages
   - SEND_PERMISSION - Send messages through BeepBoop

**Disable Battery Optimization (Important)**
Android may kill BeepBoop in the background, preventing messages from sending:
1. Open BeepBoop
2. Tap "Help"
3. Tap "Disable Battery Optimization"
4. Select "No restrictions" or similar option

This ensures BeepBoop stays awake and can send messages reliably.

### 2. Get Room IDs

You need Room IDs to send messages. Use the built-in Room Browser:

1. Open BeepBoop
2. Tap "View Rooms"
3. Browse your latest 20 BeepBoop rooms with network icons
4. Tap any room to copy its Room ID to clipboard
5. Use Room ID in Tasker or Test Message

> **Note:** Room IDs look like `!room:server.com` - Tap to copy and paste into Tasker

### 3. Configure Tasker

1. Open Tasker
2. Create a new Task
3. Add Action → Send Intent
4. Configure intent:

**Intent Settings:**
- **Action:** `SEND_MESSAGE`
- **Target:** Broadcast Receiver
- **Package:** `com.github.quickreactor.beepboop`

**Extras (use `:` separator, not `=`):**
- `roomId:!room:server.com`
- `message:Hello from Tasker!`

**Complete Example:**
```
Action: Send Intent
Action: SEND_MESSAGE
Extra: roomId:!your:room.id:server.com
Extra: message:Hello from Tasker!
Target: Broadcast Receiver
Package: com.github.quickreactor.beepboop
```

5. Save and run your Task to send a message!

### Optional: Send Without Tasker

You can test without Tasker using the built-in Test Message feature:
1. Open BeepBoop
2. Tap "Test Message"
3. Select a room from the dropdown or enter custom Room ID
4. Type your message
5. Tap "Send Message"

## Installation

### From GitHub Releases (Recommended)

1. Download the latest APK from [Releases](../../releases)
2. Enable "Install from unknown sources" in your device settings
3. Install the downloaded APK

### Building from Source

1. Clone this repository:
   ```bash
   git clone https://github.com/barnaby/beepboop.git
   ```

2. Open in Android Studio and sync Gradle

3. Build APK:
   ```bash
   # Windows
   gradlew.bat assembleDebug

   # Unix/Linux/Mac
   chmod +x gradlew && ./gradlew assembleDebug
   ```

4. Install on device:
   - Transfer `app/build/outputs/apk/debug/app-debug.apk` to your device
   - Install and grant permissions

## Permissions

BeepBoop requires these permissions to function:

| Permission | Purpose |
|------------|---------|
| `com.beeper.android.permission.READ_PERMISSION` | Read BeepBoop data (rooms, messages) |
| `com.beeper.android.permission.SEND_PERMISSION` | Send messages via BeepBoop |
| `android.permission.POST_NOTIFICATIONS` | Show failure notifications (Android 13+) |

### Battery Optimization

Android may kill BeepBoop in the background, preventing messages from sending. To disable battery optimization:

1. Open BeepBoop
2. Tap "Help"
3. Tap "Disable Battery Optimization"
4. Select "No restrictions" or similar option

## Troubleshooting

**"Permission Denied" error**
- Open BeepBoop and verify permissions are granted
- Re-request permissions if needed

**Messages not sending**
- Check that battery optimization is disabled
- Verify Room ID is correct (format: `!room:server.com`)
- View logs in BeepBoop for detailed error messages

**Room list shows "No rooms found"**
- Ensure permissions are granted
- Check that BeepBoop is logged in and has active conversations
- Try refreshing by re-opening BeepBoop

## Development

See [CLAUDE.md](CLAUDE.md) for detailed development information including:
- Architecture overview
- Build requirements
- BeepBoop ContentProvider API
- Code structure
- Testing guidelines

### Build Requirements

- **compileSdk:** 34
- **minSdk:** 26 (Android 8.0)
- **targetSdk:** 34
- **JDK:** 17
- **AGP:** 9.0.0
- **Kotlin:** 2.2.10

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Beeper Team** - For the ContentProvider API that makes this app possible
- **SimpleIcons** - For the high-quality network icons
- **Tasker** - For the powerful automation platform

---

**Note:** BeepBoop requires the Beeper app to function. This app is not affiliated with or endorsed by the Beeper team.
