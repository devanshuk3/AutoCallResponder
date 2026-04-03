**AutoCallResponder – Android Missed Call Auto-Reply System**

AutoCallResponder is an Android application that automatically sends SMS responses to missed calls based on user-defined availability modes. It is designed to handle real-time telephony events efficiently while operating within Android’s background execution constraints.

**Overview**

AutoCallResponder helps users communicate their availability without manual intervention.

When a call is missed, the app:

Detects the event in real time
Determines the active user mode (e.g., Busy, Driving, Sleeping)
Sends a predefined SMS response automatically
**Key Features**
- Automatic SMS replies to missed calls
- Multiple availability modes (Busy, Driving, Sleeping, Custom)
- Real-time call detection using Android telephony APIs
- Foreground service for continuous background monitoring
- Lightweight and efficient execution
- System Design

The application is built around Android’s telephony and background service architecture:

Incoming Call Event
        ↓
Call Log / Telephony Listener
        ↓
Missed Call Detection
        ↓
Mode Evaluation (User State)
        ↓
SMS Trigger via Android APIs

**Engineering Highlights**
1. Real-Time Event Handling
Utilizes Android telephony APIs to monitor call states
Detects missed calls without polling
2. Background Execution Constraints
Implements foreground services to ensure reliability
Works within Android’s restrictions on background processes
3. Automated Communication Logic
Mode-based response system for contextual replies
Minimizes user intervention
4. Device Compatibility Handling
Addressed OEM-specific restrictions on background services
Ensured consistent behavior across different Android devices
🔧 Tech Stack
Language: Java
UI: XML
Platform: Android SDK
APIs:
TelephonyManager
Broadcast Receivers
SMS Manager
**Core Functionality**
Missed call detection
Mode selection and management
Automated SMS dispatch
Background service lifecycle handling
**Use Cases**
- Driving or commuting
- Sleeping or unavailable
- In meetings or classes
- Situations where manual response is not possible
- Permissions Required

The app requires the following permissions:

READ_CALL_LOG
READ_PHONE_STATE
SEND_SMS
FOREGROUND_SERVICE
**Limitations**
Some devices (e.g., Xiaomi, OnePlus) may restrict background services
SMS permissions may require manual user approval
Behavior may vary across Android versions

**Future Improvements**
Smart replies using context (AI-based suggestions)
Contact-based custom responses
Integration with Do Not Disturb modes
Analytics on missed calls and responses

**Author**
Devanshu Kumar
B.Tech IT, SKIT Jaipur
