# Changelog

All notable changes to BeepBoop will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-02-14

### Added
- Initial public release
- Send BeepBoop messages via Tasker intents
- Test message interface for non-Tasker users
- Room browser with latest 20 rooms
- Network icon support for 8+ messaging platforms:
  - WhatsApp
  - Telegram
  - Signal
  - Discord
  - Slack
  - Google Messages
  - Facebook Messenger
  - Instagram
  - Matrix
- Copy Room ID to clipboard
- Battery optimization handling for Android 6.0+
- Help/instructions page
- Log viewer with copy functionality
- File-based logging (auto-truncates at 1MB)
- Failure notifications

### Features
- BroadcastReceiver for Tasker integration
- Background service for message sending
- Runtime permission management
- Schema discovery for BeepBoop ContentProvider API
- MIT License

[1.0.0]: https://github.com/barnaby/beepboop/releases/tag/v1.0.0
