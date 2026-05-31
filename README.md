# Chat-App-Firebase-Real-time-messaging-Application
<div align="center">

# 💬 NexChat
### Real-Time Messaging Application for Android

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%7C%20Auth%20%7C%20Storage-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Android Studio](https://img.shields.io/badge/Android%20Studio-Hedgehog-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)](https://developer.android.com/studio)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

*A feature-rich, Telegram-inspired real-time chat application built with Kotlin, Jetpack Compose, and Firebase.*

[Features](#-features) · [Tech Stack](#-tech-stack) · [Architecture](#-architecture) · [Installation](#-installation) · [Firebase Setup](#-firebase-setup) · [Screenshots](#-screenshots)

</div>

---

## 📖 Overview

NexChat is a modern, production-grade Android messaging application that delivers a seamless real-time communication experience. Built with a clean MVVM architecture, Jetpack Compose UI, and Firebase as the backend, NexChat supports text messaging, media sharing, audio/video calls, status updates, contact management, and comprehensive privacy controls — all in a polished, Telegram-inspired dark/light theme interface.

---

## ✨ Features

### 🔐 Authentication
- **Email & Password Login** — Traditional Firebase Auth sign-in
- **Phone Number Authentication** — OTP-based login via Firebase Phone Auth with 60-second resend timer
- **Forgot Password** — Password reset via email link
- **Auto Session Management** — Persistent login state with `SharedPreferences`-backed `SessionManager`
- **New User Profile Setup** — Guided onboarding for phone-auth users with instant Firestore save and background image upload

### 💬 Real-Time Messaging
- **Instant Messaging** — Firestore-powered real-time message delivery
- **Message Types** — Text, Images, Videos, Audio, and Documents
- **Voice Messages** — In-app audio recording with real-time timer display
- **Message Status** — Single tick (sent) and double tick (seen) read receipts
- **Typing Indicator** — Live typing status broadcast to the other participant
- **Message Search** — In-conversation search to filter messages by content
- **Edit Messages** — Edit your own messages within a 2-minute window
- **Delete Messages** — "Delete for Me" (hidden from your view) or "Delete for Everyone" (hard delete)
- **Unified Room IDs** — Deterministic, sorted room ID generation ensuring both participants share the same chat room

### 📞 Audio & Video Calling
- **One-on-One Audio Calls** — Crystal-clear voice calls via Agora RTC SDK
- **One-on-One Video Calls** — Real-time video calling with local preview and remote video streams
- **Camera Switch** — Toggle front/back camera during video calls
- **Mute Toggle** — Mute/unmute microphone during calls
- **Call History** — Full call log with caller/receiver details, timestamps, call type, and missed call indicators
- **Incoming Call Handling** — Firebase Firestore-based call signaling with in-app call screen

### 👤 Profile Management
- **View & Edit Profile** — Name, username, bio, email, phone, and profile photo
- **Username Availability Check** — Real-time username validation with availability indicator
- **Profile Photo Upload** — Cloudinary-powered image upload for profiles
- **View Other Users' Profiles** — Dedicated profile screen for any user, with message/call/mute shortcuts
- **Joined Date Display** — Shows when a user joined NexChat
- **User ID** — Unique Firebase UID display

### 📋 Status / Stories
- **Post Image Status** — Upload image statuses visible to all users
- **Status Feed** — Chronologically sorted status list with user avatars and timestamps
- **24-Hour-Style Status** — Grouped by user with last updated time

### 🔒 Privacy & Security
- **Last Seen Control** — Set visibility to Everyone, My Contacts, or Nobody
- **Profile Photo Privacy** — Control who sees your profile picture
- **About Privacy** — Control visibility of your bio
- **Read Receipts Toggle** — Disable read receipts globally
- **Block User** — Block contacts from messaging or calling you; updates both users' Firestore records
- **Report User** — Report abusive users with reason logging
- **Two-Step Verification (placeholder)** — UI and data model ready for implementation
- **Passcode Lock (placeholder)** — UI and data model ready for implementation

### 🔔 Notifications
- **Firebase Cloud Messaging (FCM)** — Push notifications for new messages
- **Notification Channel** — High-priority Android notification channel for chat messages
- **Deep Link from Notification** — Tap notification to open the relevant chat
- **Mute Notifications** — Per-user mute with duration options (8 Hours, 1 Week, Always)

### 👥 Contacts & Discovery
- **Contacts Sync** — Automatically cross-references device contacts with NexChat users
- **Invite Friends** — Share invite text for users not yet on NexChat
- **Search Users** — Search by name, @username, phone number, or email
- **Online Indicator** — Green dot for currently online users

### 🎨 Appearance & Settings
- **Dark / Light / System Theme** — Toggle themes instantly from the Home screen menu; preference saved via `SessionManager`
- **Data & Storage Settings** — Data saver toggle, auto-download preferences per network type, cache clearing
- **Notification Settings Screen** — Placeholder for granular notification controls
- **Help & Contact** — Developer contact info, report bug, feedback, and about screens
- **Logout** — Clears Firebase Auth session and local preferences

### 🖼️ Media
- **Full-Screen Image Viewer** — Tap any image to open full screen with delete option
- **Video Playback** — Opens system video player for received videos
- **Audio Playback** — Built-in play/pause for audio messages using `MediaPlayer`
- **Document Sharing** — Send and open arbitrary file types
- **Cloudinary Integration** — Reliable CDN-based media hosting for all uploads

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + XML Layouts (hybrid) |
| **Architecture** | MVVM (ViewModel + Repository + StateFlow) |
| **Dependency Injection** | Hilt (Dagger) |
| **Navigation** | Jetpack Navigation Compose |
| **Image Loading** | Coil (Compose), Glide (XML) |
| **Real-Time Database** | Firebase Firestore |
| **Authentication** | Firebase Auth (Email, Phone OTP) |
| **File Storage** | Cloudinary |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Video/Audio Calls** | Agora RTC SDK 4.6.3 |
| **App Security** | Firebase App Check (Play Integrity / Debug) |
| **Local Persistence** | DataStore Preferences, SharedPreferences |
| **Async** | Kotlin Coroutines + Flow |
| **Local DB (scaffold)** | Room |

---

## 🏗 Architecture

NexChat follows the **MVVM (Model-View-ViewModel)** pattern recommended by Google, with a clean layered approach:

```
┌──────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│   Jetpack Compose Screens  ·  XML Activities/Fragments   │
└────────────────────────┬─────────────────────────────────┘
                         │ observes StateFlow / LiveData
┌────────────────────────▼─────────────────────────────────┐
│                    ViewModel Layer                        │
│  HomeViewModel · ChatViewModel · ProfileViewModel        │
│  PrivacyViewModel · StorageViewModel                     │
└────────────────────────┬─────────────────────────────────┘
                         │ calls suspend funs / flows
┌────────────────────────▼─────────────────────────────────┐
│                   Repository Layer                        │
│          ChatRepository · SettingsRepository             │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│                    Data Sources                           │
│    Firebase Firestore · Firebase Auth · Cloudinary       │
│    Firebase FCM · Agora RTC · DataStore                  │
└──────────────────────────────────────────────────────────┘
```

### Key Design Decisions
- **Hilt** provides compile-time-safe dependency injection across ViewModels and Repositories
- **StateFlow** drives reactive UI updates without manual lifecycle management
- **Unified Room ID** (`sorted(uid1, uid2).join("_")`) guarantees both participants share the same Firestore document
- **Firestore SnapshotListeners** power real-time updates for messages, user status, and typing indicators
- **Cloudinary** is used instead of Firebase Storage to avoid egress costs on large media files

---

## 🚀 Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 21
- Android SDK API Level 24+
- A Firebase project (see [Firebase Setup](#-firebase-setup))
- A Cloudinary account
- An Agora account (for calling features)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/amitkushwaha001/Chat-App-Firebase-Real-time-messaging-Application.git
   cd Chat-App-Firebase-Real-time-messaging-Application
   ```

2. **Open in Android Studio**
   - File → Open → Select the cloned folder

3. **Add `google-services.json`**
   - Download from your Firebase project console
   - Place at `app/google-services.json`
   - **> Note:** The `google-services.json` file is not included in this repository for security reasons. Download your own file from Firebase Console and place it inside the `app/` directory before running the project.

4. **Configure secrets** in `app/src/main/java/com/example/nexchat/utils/Constants.kt`:
   ```kotlin
   object Constants {
       const val AGORA_APP_ID = "your_agora_app_id"
       const val CLOUDINARY_CLOUD_NAME = "your_cloud_name"
       const val CLOUDINARY_API_KEY = "your_api_key"
       const val CLOUDINARY_API_SECRET = "your_api_secret"
   }
   ```

5. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or press **Run ▶** in Android Studio.

---

## 🔥 Firebase Setup

### 1. Create a Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click **Add Project** → follow the wizard
3. Register your Android app with package name `com.example.nexchat`
4. Download `google-services.json` and place it in `/app/`

### 2. Enable Authentication Methods
Navigate to **Authentication → Sign-in method** and enable:
- ✅ Email/Password
- ✅ Phone

### 3. Create Firestore Database
- Go to **Firestore Database → Create database**
- Start in **production mode**
- Deploy the included rules:
  ```bash
  firebase deploy --only firestore:rules
  ```
- Or paste the contents of `firestore.rules` in the Firebase console

### 4. Enable Firebase Cloud Messaging
- FCM is enabled by default; no extra configuration required
- Ensure `google-services.json` includes your project's FCM configuration

### 5. Set Up Firebase App Check
- For **debug builds**: The app automatically uses the Debug provider — check Logcat for `Enter this debug secret` to register your debug token in the Firebase console under **App Check → Apps → Manage debug tokens**
- For **release builds**: Play Integrity is used automatically

### 6. Firestore Collections Structure

```
users/                  → User profiles (uid, name, username, email, phone, status, profileImage, ...)
chats/{roomId}/
  messages/{messageId}  → Message documents (text, type, fileUrl, seen, hiddenBy, ...)
conversations/{roomId}  → Last message summaries for chat list
calls/{callId}          → Call signaling and history
statuses/{uid}          → User status posts
privacySettings/{uid}   → Per-user privacy configuration
muteSettings/{uid}      → Per-user mute preferences
storageSettings/{uid}   → Per-user storage/download preferences
reports/                → User-submitted abuse reports
```

---

## 📁 Project Structure

```
NexChat/
├── app/
│   └── src/main/java/com/example/nexchat/
│       ├── activities/
│       │   ├── MainActivity.kt          # Compose host + incoming call handler
│       │   ├── SplashActivity.kt        # Splash with session check
│       │   ├── LoginActivity.kt         # Email login
│       │   ├── RegisterActivity.kt      # Email registration
│       │   ├── PhoneNumberActivity.kt   # Phone auth
│       │   ├── OTPActivity.kt           # OTP verification
│       │   ├── SetupProfileActivity.kt  # New user onboarding
│       │   ├── HomeActivity.kt          # Redirect shim → MainActivity
│       │   ├── ChatActivity.kt          # Legacy chat shim → MainActivity
│       │   ├── CallActivity.kt          # Agora RTC call screen
│       │   ├── ProfileActivity.kt       # Legacy profile view
│       │   ├── EditProfileActivity.kt   # Profile editor
│       │   ├── FullScreenImageActivity.kt
│       │   ├── SearchUserActivity.kt
│       │   ├── SettingsActivity.kt
│       │   └── ForgotPasswordActivity.kt
│       ├── adapters/
│       │   ├── MessageAdapter.kt        # XML RecyclerView message list
│       │   ├── UserAdapter.kt           # User/chat list adapter
│       │   ├── CallsAdapter.kt          # Call history adapter
│       │   ├── StatusAdapter.kt         # Status feed adapter
│       │   └── HomePagerAdapter.kt
│       ├── di/
│       │   ├── AppModule.kt             # Hilt: Firebase providers
│       │   └── DatabaseModule.kt        # Hilt: Room scaffold
│       ├── fragments/
│       │   ├── ChatsFragment.kt
│       │   ├── ContactsFragment.kt
│       │   ├── StatusFragment.kt
│       │   └── CallsFragment.kt
│       ├── models/
│       │   ├── User.kt
│       │   ├── Message.kt
│       │   ├── CallLog.kt
│       │   ├── Status.kt / UserStatus.kt
│       │   ├── PrivacySettings.kt
│       │   └── StorageSettings.kt
│       ├── notifications/
│       │   └── MyFirebaseMessagingService.kt
│       ├── repository/
│       │   ├── ChatRepository.kt
│       │   └── SettingsRepository.kt
│       ├── ui/
│       │   ├── navigation/
│       │   │   └── NavGraph.kt          # Compose Navigation host
│       │   ├── screens/
│       │   │   ├── HomeScreen.kt
│       │   │   ├── ChatScreen.kt        # Full chat UI + composer + bubbles
│       │   │   ├── ProfileScreen.kt
│       │   │   ├── ContactsScreen.kt
│       │   │   ├── SettingsScreen.kt
│       │   │   ├── PrivacySecurityScreen.kt
│       │   │   ├── DataStorageScreen.kt
│       │   │   ├── NotificationsScreen.kt
│       │   │   ├── HelpContactScreen.kt
│       │   │   ├── SavedMessagesScreen.kt
│       │   │   └── NewGroupScreen.kt
│       │   └── theme/
│       │       ├── Color.kt             # Telegram-inspired palette
│       │       ├── Theme.kt             # Dark / Light color schemes
│       │       └── Type.kt
│       ├── utils/
│       │   ├── AudioRecorder.kt
│       │   ├── CloudinaryHelper.kt
│       │   ├── Constants.kt
│       │   ├── ContactsHelper.kt
│       │   ├── Converters.kt
│       │   ├── NetworkUtils.kt
│       │   └── SessionManager.kt
│       ├── viewmodel/
│       │   ├── HomeViewModel.kt
│       │   ├── ChatViewModel.kt
│       │   ├── ProfileViewModel.kt
│       │   ├── PrivacyViewModel.kt
│       │   └── StorageViewModel.kt
│       └── MyApp.kt                     # Application class (Hilt + Firebase init)
├── firestore.rules
├── storage.rules
├── firebase.json
└── README.md
```

---

## 📸 Screenshots

> *Screenshots coming soon. To contribute screenshots, open a PR with images in a `/screenshots` folder.*

| Splash | Login | Home | Chat |
|--------|-------|------|------|
| `splash.png` | `login.png` | `home.png` | `chat.png` |

| Profile | Calls | Status | Settings |
|---------|-------|--------|----------|
| `profile.png` | `calls.png` | `status.png` | `settings.png` |

---

## 🔮 Future Enhancements

- [ ] **Group Chats** — UI scaffolding already in place (`NewGroupScreen.kt`)
- [ ] **Saved Messages** — Personal bookmark screen (screen exists, backend pending)
- [ ] **End-to-End Encryption** — Message encryption layer
- [ ] **Story Viewers** — Track who viewed your status
- [ ] **Message Reactions** — Emoji reactions on messages (data model already includes `reactions` field)
- [ ] **Reply to Message** — Thread-style replies (data model includes `replyToId`)
- [ ] **Two-Step Verification** — Full implementation of the existing privacy setting
- [ ] **Passcode / Biometric Lock** — App-level security
- [ ] **Active Sessions Manager** — View and revoke active login sessions
- [ ] **Blocked Users List** — Manage blocked contacts
- [ ] **Notification Settings** — Per-chat and global notification granularity
- [ ] **Message Forwarding** — Forward messages to other chats
- [ ] **Disappearing Messages** — Auto-delete messages after a set time
- [ ] **In-App Sticker / GIF Support**
- [ ] **Cloud Backup / Restore** — Chat history backup

---

## 🤝 Contributing

Contributions are warmly welcome! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit** your changes with a clear message
   ```bash
   git commit -m "feat: add disappearing messages"
   ```
4. **Push** to your fork
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Open a Pull Request** — describe what you changed and why

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `ktlint` for formatting
- Write unit tests for new business logic in `app/src/test/`

---

## 🧪 Testing

The project includes both unit tests and instrumented UI tests:

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires a connected device/emulator)
./gradlew connectedAndroidTest
```

**Unit test coverage includes:**
- `MessageModelTest` — Hidden-by logic, message type defaults
- `ChatLogicTest` — Unified room ID generation, phone number formatting
- `CallHistoryTest` — Call log participants, missed call status
- `StatusTest` — Status object creation, UserStatus aggregation

**UI tests (Espresso) cover:**
- Login screen element visibility
- Home screen tab presence and FAB
- Chat activity UI elements
- Profile screen components

---

## 📄 License

```
MIT License

Copyright (c) 2026 amitkushwaha001

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

See [LICENSE](LICENSE) for full terms.

---

## 👨‍💻 Author

<div align="center">

**Amit Kushwaha**

[![GitHub](https://img.shields.io/badge/GitHub-amitkushwaha001-181717?style=for-the-badge&logo=github)](https://github.com/amitkushwaha001)
[![Email](https://img.shields.io/badge/Email-amitkushwaha200215%40gmail.com-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:amitkushwaha200215@gmail.com)
[![Phone](https://img.shields.io/badge/Phone-%2B91%208700530415-25D366?style=for-the-badge&logo=whatsapp&logoColor=white)](tel:+918700530415)

*Made with ❤️ in India 🇮🇳*

</div>

---

<div align="center">

⭐ **If you found this project helpful, please give it a star!** ⭐

</div>
