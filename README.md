# AliSS (Ali Screenshot Sync)

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A.svg)
![S3](https://img.shields.io/badge/Storage-S3--Compatible-orange.svg)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

AliSS is a lightweight Android utility designed to automatically synchronize your screenshots and images to any S3-compatible storage provider (like MinIO, AWS S3, Cloudflare R2, etc.).

## 🚀 Features

- **Real-time Monitoring**: Automatically detects new screenshots and images added to the system MediaStore.
- **Background Sync**: Uses a foreground service to ensure reliable synchronization even when the app is in the background.
- **S3 Compatible**: Supports any S3-compatible API endpoint.
- **Manual Scan**: Trigger a manual scan and upload for a specific directory.
- **Smart Connectivity**: Option to restrict uploads to Wi-Fi only to save mobile data.
- **Broad Format Support**: Syncs JPG, PNG, and WebP images.
- **Modern Android Support**: Compatible with the latest Android permissions, including visual media selection and notification requirements.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: Android Material Components & XML Layouts
- **Architecture**: Clean Architecture principles with Repository pattern
- **Image Loading**: [Glide](https://github.com/bumptech/glide)
- **Networking**: Standard `HttpURLConnection` for lightweight S3 PUT operations
- **Navigation**: Jetpack Navigation Component

## 📋 Todo List

- [ ] Implement `WorkManager` for more robust background task scheduling.
- [ ] Add support for multiple S3 buckets/configurations.
- [ ] Implement a sync history log in the UI.
- [ ] Add image compression options before uploading.
- [ ] Support for video synchronization.
- [ ] Improve error handling and automatic retry logic.
- [ ] Material You (Dynamic Color) support.

## ⚙️ Configuration

1. Open the app and enter your S3 credentials:
   - **Endpoint**: Your S3 API URL (e.g., `https://s3.amazonaws.com`).
   - **Bucket**: The target bucket name.
   - **Access Key / Secret Key**: Your S3 credentials.
2. Specify an **Optional Sync Folder Path** for manual scans.
3. Toggle **Enable Service** to start monitoring for new media.
4. Click **Save Configuration**.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
