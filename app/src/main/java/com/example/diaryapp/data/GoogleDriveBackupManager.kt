
package com.example.diaryapp.data

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

import com.google.api.client.googleapis.json.GoogleJsonResponseException

data class CloudBackup(
    val id: String,
    val name: String,
    val modifiedTime: String?,
    val size: Long?
)

object GoogleDriveBackupManager {

    private const val APP_NAME = "DiaryApp"
    private const val MIME_JSON = "application/json"

    fun isAuthorizationError(exception: Throwable): Boolean {
        var current: Throwable? = exception

        while (current != null) {
            if (
                current is GoogleJsonResponseException &&
                current.statusCode == 401
            ) {
                return true
            }

            current = current.cause
        }

        return false
    }

    private fun driveService(accessToken: String): Drive {
        val credential = GoogleCredential()
            .setAccessToken(accessToken)

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(APP_NAME)
            .build()
    }

    /**
     * Upload a new JSON backup into the hidden appDataFolder.
     */
    suspend fun uploadBackup(
        accessToken: String,
        json: String
    ): CloudBackup = withContext(Dispatchers.IO) {

        require(json.isNotBlank()) {
            "Backup JSON is empty."
        }

        val drive = driveService(accessToken)

        val timestamp = System.currentTimeMillis()
        val metadata = File().apply {
            name = "diary_backup_$timestamp.json"
            mimeType = MIME_JSON
            parents = listOf("appDataFolder")
        }

        val media = ByteArrayContent.fromString(
            MIME_JSON,
            json
        )

        val uploaded = drive.files()
            .create(metadata, media)
            .setFields("id,name,modifiedTime,size")
            .execute()

        CloudBackup(
            id = uploaded.id
                ?: throw IOException("Google Drive did not return a file ID."),
            name = uploaded.name ?: "Diary backup",
            modifiedTime = uploaded.modifiedTime?.toStringRfc3339(),
            size = uploaded.size?.toLong()
        )
    }

    /**
     * Return backups stored in this app's hidden appDataFolder.
     */
    suspend fun listBackups(
        accessToken: String
    ): List<CloudBackup> = withContext(Dispatchers.IO) {

        val drive = driveService(accessToken)
        val result = mutableListOf<CloudBackup>()
        var pageToken: String? = null

        do {
            val response = drive.files()
                .list()
                .setSpaces("appDataFolder")
                .setQ("trashed = false")
                .setPageSize(100)
                .setOrderBy("modifiedTime desc")
                .setFields(
                    "nextPageToken,files(id,name,modifiedTime,size)"
                )
                .setPageToken(pageToken)
                .execute()

            response.files.orEmpty().forEach { file ->
                if (!file.id.isNullOrBlank()) {
                    result += CloudBackup(
                        id = file.id,
                        name = file.name ?: "Diary backup",
                        modifiedTime = file.modifiedTime?.toStringRfc3339(),
                        size = file.size?.toLong()
                    )
                }
            }

            pageToken = response.nextPageToken
        } while (pageToken != null)

        result
    }

    /**
     * Download a selected backup as a JSON string.
     */
    suspend fun downloadBackup(
        accessToken: String,
        fileId: String
    ): String = withContext(Dispatchers.IO) {

        val drive = driveService(accessToken)

        drive.files()
            .get(fileId)
            .executeMediaAsInputStream()
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    /**
     * Permanently delete the selected cloud backup.
     */
    suspend fun deleteBackup(
        accessToken: String,
        fileId: String
    ) = withContext(Dispatchers.IO) {

        driveService(accessToken)
            .files()
            .delete(fileId)
            .execute()
    }
}
