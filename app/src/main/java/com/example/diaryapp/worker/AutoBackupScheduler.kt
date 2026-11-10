package com.example.diaryapp.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AutoBackupScheduler {

    private const val WORK_NAME = "diary_automatic_backup"

    fun schedule(context: Context) {

        val request =
            PeriodicWorkRequestBuilder<AutoBackupWorker>(
                7,
                TimeUnit.DAYS
            ).build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun cancel(context: Context) {

        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(WORK_NAME)
    }
}