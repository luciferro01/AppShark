# AppShark

AppShark is a Jetpack Compose-based Android application that provides in-depth insights into installed apps on a device. It retrieves app metadata, permissions, activities, services, receivers, and native libraries, making it a useful tool for developers and enthusiasts.

## 🚀 Features
- Retrieve detailed app information (package name, version, SDK versions, etc.)
- Fetch app icons efficiently
- Identify technologies used in the app (Flutter, React Native, Unity, etc.)
- List activities, services, broadcast receivers, and content providers
- Fetch granted and denied permissions
- Detect native libraries used by the app

## 📸 Screenshots
<table>
  <tr>
    <td><img src="https://i.postimg.cc/pVjYNqLv/Whats-App-Image-2025-02-11-at-11-50-01.jpg" alt="Home Screen" width="200"/></td>
    <td><img src="https://i.postimg.cc/KjtnnvHj/Whats-App-Image-2025-02-11-at-11-50-00.jpg" alt="App Details" width="200"/></td>
    <td><img src="https://i.postimg.cc/LXctYZvG/Whats-App-Image-2025-02-11-at-11-50-00-1.jpg" alt="App Insights" width="200"/></td>
  </tr>
</table>

## 🛠️ Tech Stack
- **Jetpack Compose** - Modern UI toolkit
- **Kotlin** - Primary programming language
- **Coroutines & Flow** - Asynchronous programming
- **Retrofit** (if API calls are needed)

## 🏗️ Installation
1. Clone the repository:
   ```sh
   git clone https://github.com/luciferro01/appshark.git
   ```
2. Open in Android Studio
3. Build and run on an Android device or emulator

## 🔑 Building the Release APK
1. Set up a keystore for signing (see [release guide](https://developer.android.com/studio/publish/app-signing))
2. Run the following command:
   ```sh
   ./gradlew assembleRelease
   ```
3. Find the APK in `app/build/outputs/apk/release/`

## 📥 Download & Check Out
🔗 **Download the latest release:** [GitHub Releases](https://github.com/luciferro01/appshark/releases)

## 🔍 Known Issues & Improvements
- 🚨 **Performance optimizations** needed for large app lists
- 📌 **Language detection** still requires improvement
- 🛠️ **Better error handling** for package manager calls

## 🤝 Contributing
Pull requests and feature suggestions are welcome! Feel free to open an issue or submit a PR.

## 📜 License
This project is licensed under the MIT License. See the `LICENSE` file for more details.

---

🚀 Built in less than a day as a curiosity-driven project! Still a work in progress with known performance issues but fun to explore. 😃