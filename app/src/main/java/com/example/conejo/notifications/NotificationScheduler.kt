package com.example.conejo.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_NAME = "MochilaRecordatorioWork"

    fun scheduleDailyMochilaWork(context: Context) {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(8, 0)
        var targetDateTime = now.with(targetTime)

        // Si ya pasaron las 8 AM hoy, programamos para mañana
        if (now.isAfter(targetDateTime)) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, targetDateTime)

        val workRequest = PeriodicWorkRequestBuilder<MochilaWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
