package com.example.conejo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.conejo.logic.RutinaLogic
import java.time.LocalDate

class MochilaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hoy = LocalDate.now()
        // Por ahora pasamos lista vacía de excepciones. 
        // En el futuro esto vendría de una base de datos.
        val resumen = RutinaLogic.obtenerResumenRutina(hoy, emptyList())

        val body = buildString {
            if (resumen.mensajeAlerta != null) {
                append("⚠️ ${resumen.mensajeAlerta} ")
            }
            val itemsACargar = resumen.checklistMochila.filter { it.debeEstar }
            if (itemsACargar.isNotEmpty()) {
                append("Llevar: ")
                itemsACargar.forEachIndexed { index, item ->
                    append(item.nombre)
                    if (index < itemsACargar.size - 1) append(", ")
                }
            }
        }.trim()

        if (body.isNotEmpty()) {
            showNotification(body)
        }

        return Result.success()
    }

    private fun showNotification(content: String) {
        val channelId = "mochila_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Mochila",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones diarias para revisar la mochila"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🐰 Conejo: ¿Qué llevar hoy?")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
