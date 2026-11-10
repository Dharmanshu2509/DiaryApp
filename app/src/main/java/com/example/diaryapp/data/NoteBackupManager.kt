package com.example.diaryapp.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.diaryapp.model.NoteItem
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoteBackupManager {

    data class BackupFile(
        val uri: Uri,
        val fileName: String,
        val dateModified: Long,
        val sizeBytes: Long
    )

    private const val TAG = "DiaryBackup"
    private const val RELATIVE_PATH = "Download/Diary App/Backup/"

    fun createJson(notes: List<NoteItem>): String {
        val jsonArray = JSONArray()

        notes.forEach { note ->
            val noteObject = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("message", note.message)
                put("date", note.date)
                put("createdAt", note.createdAt)
                put("modifiedAt", note.modifiedAt)
            }

            jsonArray.put(noteObject)
        }

        return JSONObject().apply {
            put("app", "DiaryApp")
            put("version", 2)
            put("notesCount", notes.size)
            put("notes", jsonArray)
        }.toString(2)
    }

    fun saveBackup(
        context: Context,
        notes: List<NoteItem>
    ): Result<Int> = runCatching {

        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Public Download backup ke liye Android 10 ya newer required hai."
        }

        // Har backup ke liye unique filename generate karo.
        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd_HH_mm_ss",
            Locale.US
        ).format(Date())

        val nanoseconds = (System.nanoTime() % 1_000_000_000L)
            .toString()
            .padStart(9, '0')

        val fileName =
            "memories_reader_backup_${timestamp}_${nanoseconds}.json"

        // Notes ka JSON banao.
        val json = createJson(notes)

        // JSON ko Logcat mein print karo.
        // Long JSON ko chunks mein print karna zaroori hai.
        Log.d(TAG, "========== DIARY BACKUP START ==========")
        Log.d(TAG, "Filename: $fileName")
        Log.d(TAG, "Total notes: ${notes.size}")

        json.chunked(3000).forEachIndexed { index, chunk ->
            Log.d(TAG, "JSON[$index]: $chunk")
        }

        Log.d(TAG, "=========== DIARY BACKUP END ===========")

        val resolver = context.contentResolver

        // Nayi backup file create karo.
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, RELATIVE_PATH)
        }

        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: error("Backup file create nahi ho saki.")

        // JSON ko file mein save karo.
        val outputStream = resolver.openOutputStream(uri, "w")
            ?: error("Backup file write nahi ho saki.")

        outputStream.bufferedWriter().use { writer ->
            writer.write(json)
        }

        Log.d(TAG, "Backup saved: $RELATIVE_PATH$fileName")
        Log.d(TAG, "Backup notes count: ${notes.size}")

        notes.size
    }

    fun getBackupHistory(context: Context): List<BackupFile> {
        val backupFiles = mutableListOf<BackupFile>()

        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.DATE_MODIFIED,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.RELATIVE_PATH
        )

        val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"

        return try {
            val cursor = context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
                arrayOf("memories_reader_backup_%.json"),
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(
                    MediaStore.Downloads._ID
                )
                val nameColumn = it.getColumnIndexOrThrow(
                    MediaStore.Downloads.DISPLAY_NAME
                )
                val dateColumn = it.getColumnIndexOrThrow(
                    MediaStore.Downloads.DATE_MODIFIED
                )
                val sizeColumn = it.getColumnIndexOrThrow(
                    MediaStore.Downloads.SIZE
                )
                val pathColumn = it.getColumnIndexOrThrow(
                    MediaStore.Downloads.RELATIVE_PATH
                )

                while (it.moveToNext()) {
                    val fileName = it.getString(nameColumn) ?: continue
                    val path = it.getString(pathColumn) ?: ""

                    // Sirf Diary App ke backup folder ki files lo.
                    if (!path.trimEnd('/').equals(
                            RELATIVE_PATH.trimEnd('/'),
                            ignoreCase = true
                        )
                    ) {
                        continue
                    }

                    val id = it.getLong(idColumn)
                    val dateModified = it.getLong(dateColumn)
                    val sizeBytes = it.getLong(sizeColumn)

                    val uri = android.content.ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        id
                    )

                    backupFiles.add(
                        BackupFile(
                            uri = uri,
                            fileName = fileName,
                            dateModified = dateModified,
                            sizeBytes = sizeBytes
                        )
                    )
                }
            }

            Log.d(TAG, "Backup history loaded: ${backupFiles.size} files")
            backupFiles

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load backup history", e)
            emptyList()
        }
    }

}