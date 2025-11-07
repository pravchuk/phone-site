# Phone Site

A distributed web hosting solution that allows Android devices to serve websites directly, with automatic IP address management and reverse proxy configuration.

## Overview

Phone Site consists of two main components:

1. **Android App** - An HTTP server application that runs on Android devices to serve websites
2. **Server Scripts** - Python scripts for managing device IP addresses and updating reverse proxy configurations

The system enables Android devices to act as web servers, automatically registering their IPv6 addresses with a central server that updates Nginx reverse proxy configurations in real-time.

## Features

### Android App
- **Embedded HTTP Server**: Runs a lightweight HTTP server on port 5000
- **Customizable Content**: Edit and serve custom HTML content directly from the device
- **Automatic IP Registration**: Periodically pings a central server with the device's IPv6 address
- **RESTful API**: Provides endpoints for serving content and handling messages
- **Simple UI**: Easy-to-use interface for starting/stopping the server and editing content

### Server Scripts
- **IP Manager**: Flask server that receives device pings and manages IPv6 address mappings
- **Nginx Integration**: Automatically updates Nginx configuration files when device IPs change
- **Load Testing**: Utility script for testing server performance

## Project Structure

```
phone-site/
├── PhoneSite/                    # Android application
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/app/phonesite/manager/
│   │   │   │   ├── MainActivity.kt          # Main application activity
│   │   │   │   └── webserver/
│   │   │   │       └── WebServer.kt         # Web server implementation (commented out)
│   │   │   ├── res/                         # Android resources
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle
└── ServerScripts/                # Python server scripts
    ├── ipmanager.py              # IP address management server
    └── load_test.py              # Load testing utility
```

## Requirements

### Android App
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 33 (Android 13)
- **Kotlin**: 1.7.20
- **Gradle**: 7.3.1
- **Android Gradle Plugin**: 7.3.1

### Server Scripts
- **Python 3.x**
- **Flask** (for `ipmanager.py`)
- **aiohttp** (for `load_test.py`)
- **Nginx** (for reverse proxy configuration)

## Setup

### Android App

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd phone-site/PhoneSite
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `PhoneSite` directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

5. **Permissions**
   - The app requires `INTERNET` permission (already configured in `AndroidManifest.xml`)
   - Ensure the device has IPv6 connectivity

### Server Scripts

1. **Install Python dependencies**
   ```bash
   pip install flask aiohttp
   ```

2. **Configure Nginx**
   - Ensure Nginx is installed and configured
   - Update the `nginx_config_path` in `ipmanager.py` to match your Nginx configuration directory
   - The script expects Nginx configs in `/etc/nginx/sites-available/`

3. **Run the IP Manager server**
   ```bash
   cd ServerScripts
   python ipmanager.py
   ```
   The server will run on `[::]:999` (IPv6, port 999)

4. **Configure the Android app**
   - Update the server URL in `MainActivity.kt` (line 193) to match your server's address:
   ```kotlin
   val serverAddress = URL("http://[YOUR_SERVER_IPV6]:999/ping")
   ```

## Usage

### Android App

1. **Start the Server**
   - Launch the app on your Android device
   - Tap "Start Server"
   - Enter the website URL/name when prompted
   - The server will start on port 5000

2. **Edit Website Content**
   - Tap the "EDIT" button
   - Modify the HTML content in the dialog
   - Save changes

3. **Stop the Server**
   - Tap "Stop Server" to shut down the HTTP server

### API Endpoints

The Android app exposes the following endpoints:

- `GET /` or `GET /index` - Returns the configured HTML content
- `GET /messages` - Returns all messages (placeholder implementation)
- `POST /messages` - Accepts JSON message data (placeholder implementation)

### Server Scripts

**IP Manager (`ipmanager.py`)**
- Listens for ping requests from Android devices
- Tracks IPv6 addresses per website
- Updates Nginx configuration when IPs change
- Automatically reloads Nginx

**Load Test (`load_test.py`)**
- Sends multiple concurrent requests to a target URL
- Useful for testing server performance
- Configure target URL and number of requests in the script

## Configuration

### Android App

**Default Content**: The app includes a default "Techverse Conference" landing page. You can edit this through the UI or modify the `TECH_VERSE_LANDING_PAGE` variable in `MainActivity.kt`.

**Ping Interval**: Currently set to 10 seconds (configurable in `MainActivity.kt`, line 37).

**Server Port**: Default port is 5000 (configurable in `MainActivity.kt`, line 44).

### Server Scripts

**IP Manager Configuration**:
- Update `nginx_config_path` to match your Nginx configuration directory
- Modify the IPv6 address pattern matching if your Nginx config format differs
- Ensure the script has appropriate permissions to modify Nginx configs and reload the service

## Development

### Building

```bash
cd PhoneSite
./gradlew assembleDebug
```

### Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────┐
│  Android Device │─────────▶│  IP Manager      │─────────▶│   Nginx     │
│  (HTTP Server)  │  Ping    │  (Flask Server)  │  Config  │  (Reverse   │
│  Port: 5000     │  Every   │  Port: 999       │  Update  │   Proxy)    │
└─────────────────┘  10s     └──────────────────┘          └─────────────┘
       │
       │ Serves HTML
       ▼
┌─────────────────┐
│  Web Clients    │
└─────────────────┘
```

## Notes

- The `WebServer.kt` file is currently commented out; the server functionality is implemented directly in `MainActivity.kt`
- The app uses cleartext HTTP traffic (configured in `AndroidManifest.xml`)
- IPv6 connectivity is required for the IP registration system to work
- The message handling endpoints (`/messages`) are placeholder implementations

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]

## Author

[Add author information here]

