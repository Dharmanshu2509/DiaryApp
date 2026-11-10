package com.example.diaryapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.diaryapp.data.GoogleDriveBackupManager
import com.example.diaryapp.data.NoteBackupManager
import com.example.diaryapp.data.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        return try {
            // 1. Always create local backup first
            val database = NoteDatabase.getDatabase(context)
            val notes = database.noteDao().getAllNotes().first()

            val localResult = withContext(Dispatchers.IO) {
                NoteBackupManager.saveBackup(context, notes)
            }

            localResult.getOrThrow()

            Log.d("AutoBackup", "Local backup completed.")

            // 2. Read Google Drive settings
            val googlePreferences = context.getSharedPreferences(
                "google_account_settings",
                Context.MODE_PRIVATE
            )

// Check whether cloud backup is paused
            val cloudBackupPaused = googlePreferences.getBoolean(
                "cloud_backup_paused",
                false
            )

// If cloud backup is paused due to expired authorization,
// keep local backup running without showing another notification.
            if (cloudBackupPaused) {
                Log.d(
                    "AutoBackup",
                    "Cloud backup is paused. Local backup completed."
                )

                BackupNotificationManager.show(
                    context,
                    "Local Backup Completed",
                    "Your diary entries have been backed up successfully on your device. Reconnect your Google account to resume cloud backups.",
                    1003
                )

                return Result.success()
            }

// Read Google Drive token
            val token = googlePreferences.getString(
                "drive_access_token",
                null
            )

// No Google account connected: notify about local backup
            if (token.isNullOrBlank()) {

                Log.d(
                    "AutoBackup",
                    "Google account not connected. Local backup completed."
                )

                BackupNotificationManager.show(
                    context,
                    "Local Backup Completed",
                    "Your diary entries have been backed up successfully on your device. Connect your Google account to enable cloud backups.",
                    1003
                )

                return Result.success()
            }

            // 3. Try Google Drive backup
            try {
                val json = NoteBackupManager.createJson(notes)

                withContext(Dispatchers.IO) {
                    GoogleDriveBackupManager.uploadBackup(
                        accessToken = token,
                        json = json
                    )
                }

                Log.d("AutoBackup", "Cloud backup completed.")

                BackupNotificationManager.show(
                    context,
                    "Automatic Backup Completed",
                    "Your memories have been backed up locally and to Google Drive.",
                    1002
                )

            } catch (cloudError: Exception) {

                Log.e(
                    "AutoBackup",
                    "Cloud backup failed",
                    cloudError
                )

                if (GoogleDriveBackupManager.isAuthorizationError(cloudError)) {

                    // Preserve selected_google_email.
                    // Only remove expired access token.
                    googlePreferences.edit()
                        .remove("drive_access_token")
                        .putBoolean("cloud_backup_paused", true)
                        .apply()

                    Log.w(
                        "AutoBackup",
                        "Google token expired. Cloud backup paused; local backup remains active."
                    )

                    BackupNotificationManager.show(
                        context,
                        "Reconnect Google Account",
                        "Your local backups are continuing. Reconnect your Google account to resume cloud backups.",
                        1004
                    )

                    // IMPORTANT:
                    // Do not return failure. Periodic local backup must continue.
                    return Result.success()
                }

                // Other cloud errors should not stop local backup.
                Log.e(
                    "AutoBackup",
                    "Temporary cloud backup error. Will try again in the next scheduled run.",
                    cloudError
                )
            }

            Result.success()

        } catch (e: Exception) {

            // Local backup itself failed.
            Log.e("AutoBackup", "Local automatic backup failed", e)

            Result.retry()
        }
    }
}