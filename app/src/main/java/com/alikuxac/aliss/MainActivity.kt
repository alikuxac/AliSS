package com.alikuxac.aliss

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alikuxac.aliss.data.MediaSyncService
import com.alikuxac.aliss.data.S3Config
import com.alikuxac.aliss.data.S3ConfigManager
import com.alikuxac.aliss.databinding.ActivityMainBinding
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var configManager: S3ConfigManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            updateServiceState()
        } else {
            Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configManager = S3ConfigManager(this)
        loadConfiguration()

        binding.btnSave.setOnClickListener {
            saveConfiguration()
        }

        binding.btnScanNow.setOnClickListener {
            performManualScanAndUpload()
        }

        checkAndRequestPermissions()
    }

    private fun loadConfiguration() {
        val config = configManager.getConfig()
        binding.etEndpoint.setText(config.endpoint)
        binding.etBucket.setText(config.bucket)
        binding.etAccessKey.setText(config.accessKey)
        binding.etSecretKey.setText(config.secretKey)
        binding.etSyncFolder.setText(config.syncFolder)
        binding.switchWifiOnly.isChecked = config.isWifiOnly
        binding.switchService.isChecked = config.isServiceEnabled
    }

    private fun saveConfiguration() {
        val config = S3Config(
            endpoint = binding.etEndpoint.text.toString().trim(),
            bucket = binding.etBucket.text.toString().trim(),
            accessKey = binding.etAccessKey.text.toString().trim(),
            secretKey = binding.etSecretKey.text.toString().trim(),
            syncFolder = binding.etSyncFolder.text.toString().trim(),
            isWifiOnly = binding.switchWifiOnly.isChecked,
            isServiceEnabled = binding.switchService.isChecked
        )
        configManager.saveConfig(config)
        Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show()
        updateServiceState()
    }

    private fun updateServiceState() {
        val config = configManager.getConfig()
        val intent = Intent(this, MediaSyncService::class.java)
        if (config.isServiceEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } else {
            stopService(intent)
        }
    }

    private fun performManualScanAndUpload() {
        val config = configManager.getConfig()
        val folderPath = config.syncFolder
        if (folderPath.isEmpty()) {
            Toast.makeText(this, "Please configure an Optional Sync Folder Path first", Toast.LENGTH_SHORT).show()
            return
        }

        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) {
            Toast.makeText(this, "Configured folder path does not exist or is not a directory", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Manual scan & upload started...", Toast.LENGTH_SHORT).show()

        thread {
            val files = folder.listFiles { file -> 
                file.isFile && (file.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")) 
            }
            if (files.isNullOrEmpty()) {
                runOnUiThread {
                    Toast.makeText(this, "No images found in the configured folder", Toast.LENGTH_SHORT).show()
                }
                return@thread
            }

            var successCount = 0
            var failCount = 0

            for (file in files) {
                val success = uploadToS3Direct(file, file.name, config)
                if (success) successCount++ else failCount++
            }

            runOnUiThread {
                Toast.makeText(this, "Scan complete: Uploaded $successCount images ($failCount failed)", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadToS3Direct(file: File, fileName: String, config: S3Config): Boolean {
        if (config.endpoint.isEmpty() || config.bucket.isEmpty()) return false
        return try {
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
            connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_CREATED
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.DETECT_SCREEN_CAPTURE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        } else {
            updateServiceState()
        }
    }
}
