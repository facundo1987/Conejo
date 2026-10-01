package com.example.conejo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.conejo.notifications.NotificationScheduler
import com.example.conejo.ui.CalendarScreen
import com.example.conejo.ui.theme.ConejoTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido, programamos las notificaciones
            NotificationScheduler.scheduleDailyMochilaWork(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkAndRequestNotifications()

        setContent {
            ConejoTheme {
                CalendarScreen()
            }
        }
    }

    private fun checkAndRequestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Ya tenemos permiso
                    NotificationScheduler.scheduleDailyMochilaWork(this)
                }
                else -> {
                    // Pedir permiso
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // En versiones anteriores a Android 13 no hace falta pedir permiso en runtime
            NotificationScheduler.scheduleDailyMochilaWork(this)
        }
    }
}
