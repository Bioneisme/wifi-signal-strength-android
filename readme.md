# WiFi Signal Strength

- [Navigation](#navigation)
    - [Features](#features)
    - [Usage](#usage)
    - [Screenshots](#screenshots)
    - [Installation](#prerequisites)


## Features:
* Scan WiFi networks and display their signal strength
* Emit beeping sounds to indicate signal strength
* Filter WiFi networks by signal strength and other parameters
* Display a heat map of WiFi networks in your area
* Allow users to customize the app settings and preferences
* User authentication and authorization features
* Record WiFi network data in the app's database and display it in user history
* Backend server written in Node.js using Postgresql
* Frontend admin panel for managing WiFi networks, users, and statistics

## Usage:
1. Open the app and allow it to access your device location
2. The app will start scanning for nearby WiFi networks
3. As you move around, the app will emit beeping sounds to indicate the signal strength of the WiFi networks in your area
4. Use the filters to narrow down the results and view a heat map of WiFi networks
5. Customize the app settings and preferences in the user settings menu
6. View your scan history to see previously scanned WiFi networks

## Screenshots:


## Getting Started
### Prerequisites
* Android Studio SDK
* [Server](https://github.com/Bioneisme/wifi-signal-strength-backend)

### Installing
* Get the latest snapshot
```bash
git clone https://github.com/Bioneisme/wifi-signal-strength-android.git
```
* Change directory
``` bash
cd wifi-signal-strength-android
```
* Wait for the Gradle build to complete and the project to fully load in Android Studio
* Change the Base_URL in the config folder of the backend server to your own
* Launch the application


*Backend - https://github.com/Bioneisme/wifi-signal-strength-backend*
