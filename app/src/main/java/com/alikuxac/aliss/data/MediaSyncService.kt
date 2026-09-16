package com.alikuxac.aliss.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MediaSyncService : Service() {

    private lateinit var configManager: S3ConfigManager
    private var contentObserver: ContentObserver? = null

    override fun onCreate() {
        super.onCreate()
        configManager = S3ConfigManager(this)
        startForegroundService()
        registerMediaObserver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config = configManager.getConfig()
        if (!config.isServiceEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "media_sync_channel"
        val channelName = "Media Sync Service"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("AliSS Sync Active")
            .setContentText("Monitoring MediaStore for new screenshots...")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun registerMediaObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri?.let { handleNewMedia(it) }
            }
        }
        
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver!!
        )
    }

    private fun isNetworkAllowed(config: S3Config): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return if (config.isWifiOnly) {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }
    }

    private fun handleNewMedia(uri: Uri) {
        thread {
            try {
                val config = configManager.getConfig()
                if (!config.isServiceEnabled) return@thread
                
                if (!isNetworkAllowed(config)) {
                    Log.d("MediaSyncService", "Sync skipped: Network condition not met (Wi-Fi only restriction checked).")
                    return@thread
                }

                val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME)
                contentResolver.query(uri, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        val fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                        Log.d("MediaSyncService", "New media detected: $filePath, name: $fileName")
                        
                        if (config.endpoint.isNotEmpty() && config.bucket.isNotEmpty()) {
                            uploadToS3(File(filePath), fileName, config)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MediaSyncService", "Error processing new media", e)
            }
        }
    }

    private fun uploadToS3(file: File, fileName: String, config: S3Config) {
        if (!file.exists()) return
        try {
            val urlString = "${config.endpoint.trimEnd('/')}/${config.bucket}/$fileName"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "image/png")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            file.inputStream().use { input ->
                connection.outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            val responseCode = connection.responseCode
            Log.d("MediaSyncService", "S3 Upload response code: $responseCode")
        } catch (e: Exception) {
            Log.e("MediaSyncService", "Failed to upload to S3", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
