# CVE-2026-58882: Android Zero-Click Telephony DoS (SMS of Death)

**Disclaimer:** This repository contains a Proof of Concept (PoC) for a zero-click Denial of Service (DoS) vulnerability in the Android Framework. This tool is intended for security research, CTF environments, and testing your own devices. Do not use this against devices you do not own or have explicit permission to test.

## 📖 Overview

**CVE-2026-58882** is a zero-click telephony denial-of-service vulnerability affecting unpatched Android devices (Android 12 through Android 16). 

By sending a specially crafted WAP Push data SMS to port `2948`, an attacker can force an uncaught `NegativeArraySizeException` within the `com.android.phone` process. This instantly kills the telephony service, dropping active calls (including emergency calls) and severing cellular connectivity for 1–2 seconds.

Sending multiple payloads in rapid succession triggers a race condition in the SMS SQLite database, resulting in a persistent bootloop that may require a factory reset to resolve.

## ⚙️ Technical Details

*   **Vulnerable Component:** `WapPushOverSms.decodeWapPdu()`
*   **Vulnerability Type:** Integer Underflow / Unhandled Exception (DoS)
*   **Impact:** Process Crash, Call Drop, Permanent Bootloop (if spammed)
*   **Affected OS:** Android 12, 13, 14, 15, 16 (Pre-October 2026 patch)

### The Root Cause
The vulnerability resides in how the Android framework parses incoming WAP Push PDUs (Protocol Data Units). The parser reads a header length value as a `long` (e.g., `0x80000000`, which equals `2,147,483,648`), but then casts it to a signed 32-bit `int` without proper bounds checking. 

The value overflows into a negative integer (`-2,147,483,648`). The framework then attempts to allocate a byte array of that exact negative size, throwing an uncaught `java.lang.NegativeArraySizeException` and terminating the `com.android.phone` process.

## 💥 The Payload

The attack is executed via a silent data SMS sent directly to the WAP Push port. The user receives no notification, and the message is never saved to the standard SMS inbox.



**Compiling the APK**
Clone the repository:

bash
git clone https://github.com/YOUR_USERNAME/smsofdeath.git
cd smsofdeath
Create a local.properties file and point it to your SDK:

properties
sdk.dir=/path/to/your/Android/Sdk
Build the debug APK:

bash
./gradlew assembleDebug
The APK will be located at:

text
app/build/outputs/apk/debug/app-debug.apk


> [!WARNING]  
> **Mitigation & Patching**
> 
> To protect a device:
> *   Update to the October 2026 (or later) security patch level.
> *   Carriers can mitigate this by blocking incoming data SMS to port `2948` at the network level.
