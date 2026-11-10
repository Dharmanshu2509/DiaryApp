package com.example.diaryapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diaryapp.MainViewModel

import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import com.example.diaryapp.R

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.example.diaryapp.ui.theme.AppThemeMode
import com.example.diaryapp.ui.theme.ThemePreferences

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import com.example.diaryapp.data.NoteBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

import com.example.diaryapp.model.NoteItem

import android.content.Intent
import android.provider.DocumentsContract

import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.*
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.example.diaryapp.data.GoogleDriveBackupManager
import com.example.diaryapp.data.CloudBackup
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.diaryapp.worker.AutoBackupScheduler

import androidx.compose.foundation.layout.fillMaxHeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.net.HttpURLConnection
import java.net.URL

import android.accounts.Account
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonColors

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.diaryapp.ui.theme.body3

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showRestoreSourceDialog by remember {
        mutableStateOf(false)
    }

    var showBackupOptionsDialog by remember {
        mutableStateOf(false)
    }

    var showConnectForBackupDialog by remember {
        mutableStateOf(false)
    }

    var pendingCloudBackup by remember {
        mutableStateOf(false)
    }

    var localBackups by remember {
        mutableStateOf<List<NoteBackupManager.BackupFile>>(emptyList())
    }

    var localHistoryLoading by remember {
        mutableStateOf(false)
    }

    /*    var selectedAppearance by remember {
            mutableStateOf("System default")
        }*/

    val totalNotes = viewModel.items.value.size

    val selectedAppearance = ThemePreferences.themeMode.value.label

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val notes = mutableListOf<NoteItem>()

    val googlePreferences = remember {
        context.getSharedPreferences("google_account_settings", Context.MODE_PRIVATE)
    }

    var selectedGoogleEmail by remember {
        mutableStateOf(
            googlePreferences.getString("selected_google_email", null)
        )
    }

    var accountSelectionMode by remember {
        mutableStateOf("ADD")
    }

    var selectedHistoryTab by remember {
        mutableStateOf(0)
    }

// Backup file save karne ke liye
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val notes = viewModel.items.value

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

                        val backupObject = JSONObject().apply {
                            put("app", "DiaryApp")
                            put("version", 2)
                            put("notes", jsonArray)
                        }

                        context.contentResolver
                            .openOutputStream(uri)
                            ?.bufferedWriter()
                            ?.use { writer ->
                                writer.write(backupObject.toString(2))
                            }
                            ?: error("Can't open backup file.")

                        notes.size
                    }
                }

                result.onSuccess { count ->
                    Toast.makeText(
                        context,
                        "Backup saved of $count notes.",
                        Toast.LENGTH_LONG
                    ).show()
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        "Backup failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

// Backup file select karne ke liye
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val jsonText = context.contentResolver
                            .openInputStream(uri)
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: error("Unable to read the backup file.")

                        val backupObject = JSONObject(jsonText)

                        require(backupObject.optString("app") == "DiaryApp") {
                            "This is not a valid DiaryApp backup file."
                        }

                        val jsonArray = backupObject.getJSONArray("notes")
                        val notes = mutableListOf<com.example.diaryapp.model.NoteItem>()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)

                            notes.add(
                                com.example.diaryapp.model.NoteItem(
                                    id = item.getLong("id"),
                                    title = item.getString("title"),
                                    message = item.getString("message"),
                                    date = item.getString("date"),
                                    createdAt = item.getLong("createdAt"),
                                    modifiedAt = item.getLong("modifiedAt")
                                )
                            )
                        }

                        notes
                    }
                }

                result.onSuccess { notes ->
                    viewModel.restoreNotes(notes)

                    Toast.makeText(
                        context,
                        "${notes.size} notes imported successfully for restoration.",
                        Toast.LENGTH_LONG
                    ).show()
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        "Restore failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    val localBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data

            if (uri != null) {
                // Selected JSON file ko yahan process kar sakte hain.
                // Restore functionality hum agle step mein connect karenge.
                Toast.makeText(
                    context,
                    "Backup file selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    var driveAccessToken by remember {
        mutableStateOf(
            googlePreferences.getString("drive_access_token", null)
        )
    }

    var isDriveAuthExpired by remember {
        mutableStateOf(false)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_RESUME) {

                selectedGoogleEmail = googlePreferences.getString(
                    "selected_google_email",
                    null
                )

                driveAccessToken = googlePreferences.getString(
                    "drive_access_token",
                    null
                )
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var cloudBackups by remember {
        mutableStateOf<List<CloudBackup>>(emptyList())
    }

    var showCloudBackups by remember {
        mutableStateOf(false)
    }

    var cloudLoading by remember {
        mutableStateOf(false)
    }

    var cloudUploading by remember {
        mutableStateOf(false)
    }

    suspend fun getGoogleEmail(token: String): String {
        return withContext(Dispatchers.IO) {
            val connection = URL(
                "https://www.googleapis.com/oauth2/v3/userinfo"
            ).openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $token"
                )
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val responseCode = connection.responseCode

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    error("Unable to fetch Google account information. HTTP $responseCode")
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val email = JSONObject(response).optString("email")

                require(email.isNotBlank()) {
                    "Google email address was not returned."
                }

                email
            } finally {
                connection.disconnect()
            }
        }
    }

    fun saveGoogleAccount(token: String) {
        coroutineScope.launch {
            try {
                val email = getGoogleEmail(token)

                // Change Account में वही पुराना account select नहीं होने देंगे.
                if (
                    accountSelectionMode == "CHANGE" &&
                    email.equals(selectedGoogleEmail, ignoreCase = true)
                ) {
                    Toast.makeText(
                        context,
                        "Please select a different Google account.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                driveAccessToken = token
                selectedGoogleEmail = email
                isDriveAuthExpired = false
                cloudBackups = emptyList()

                googlePreferences.edit()
                    .putString("drive_access_token", token)
                    .putString("selected_google_email", email)
                    .putBoolean("cloud_backup_paused", false)
                    .apply()

// If automatic backup is already enabled,
// ensure periodic local + cloud backup is scheduled.
                val settingsPreferences = context.getSharedPreferences(
                    "diary_settings",
                    Context.MODE_PRIVATE
                )

                if (settingsPreferences.getBoolean("auto_backup_enabled", false)) {
                    AutoBackupScheduler.schedule(context)
                }

                Toast.makeText(
                    context,
                    "Google account connected: $email",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Account information failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val driveAuthorizationLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode == android.app.Activity.RESULT_OK) {

                try {

                    val authorizationResult =
                        Identity.getAuthorizationClient(context)
                            .getAuthorizationResultFromIntent(result.data)

                    val token = authorizationResult.accessToken

                    if (token.isNullOrBlank()) {

                        Toast.makeText(
                            context,
                            "Google Drive access token nahi mila.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        saveGoogleAccount(token)
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        context,
                        "Authorization failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {

                Toast.makeText(
                    context,
                    "Google account selection cancelled.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun requestDriveAccess() {

        val request = AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(
                    Scope(DriveScopes.DRIVE_APPDATA),
                    Scope("openid"),
                    Scope("email"),
                    Scope("profile")
                )
            )
            .setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
            .build()

        Identity.getAuthorizationClient(context)
            .authorize(request)
            .addOnSuccessListener { authorizationResult ->

                if (authorizationResult.hasResolution()) {

                    val pendingIntent = authorizationResult.pendingIntent

                    if (pendingIntent != null) {

                        driveAuthorizationLauncher.launch(
                            IntentSenderRequest.Builder(
                                pendingIntent.intentSender
                            ).build()
                        )

                    } else {

                        Toast.makeText(
                            context,
                            "Google account selection unavailable.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {

                    val token = authorizationResult.accessToken

                    if (!token.isNullOrBlank()) {

                        saveGoogleAccount(token)

                    } else {

                        Toast.makeText(
                            context,
                            "Google Drive access token nahi mila.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    context,
                    "Google authorization failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun handleDriveAuthError(
        error: Throwable,
        operation: String
    ): Boolean {

        Log.e(
            "DiaryDriveAuth",
            "$operation failed due to Google Drive authorization.",
            error
        )

        if (!GoogleDriveBackupManager.isAuthorizationError(error)) {
            return false
        }

        isDriveAuthExpired = true
        cloudBackups = emptyList()
        showCloudBackups = false

        Log.w(
            "DiaryDriveAuth",
            "Google Drive access token expired or unauthorized. Reconnection required."
        )

        Toast.makeText(
            context,
            "Google Drive session expired. Please reconnect your account.",
            Toast.LENGTH_LONG
        ).show()

        return true
    }

    fun removeGoogleAccount() {

        val email = selectedGoogleEmail

        if (email.isNullOrBlank()) {

            Toast.makeText(
                context,
                "No Google account connected.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        coroutineScope.launch {

            try {

                val revokeRequest = RevokeAccessRequest.builder()
                    .setAccount(
                        Account(email, "com.google")
                    )
                    .setScopes(
                        listOf(
                            Scope(DriveScopes.DRIVE_APPDATA),
                            Scope("openid"),
                            Scope("email"),
                            Scope("profile")
                        )
                    )
                    .build()

                Identity.getAuthorizationClient(context)
                    .revokeAccess(revokeRequest)
                    .addOnSuccessListener {

                        // Clear app's Google account state
                        driveAccessToken = null
                        selectedGoogleEmail = null

                        cloudBackups = emptyList()
                        showCloudBackups = false

                        googlePreferences.edit()
                            .remove("selected_google_email")
                            .remove("drive_access_token")
                            .apply()

                        Toast.makeText(
                            context,
                            "Google account disconnected from DiaryApp.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { error ->

                        Toast.makeText(
                            context,
                            "Unable to revoke Google access: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Unable to remove Google account: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun backupToGoogleDrive() {
        val token = driveAccessToken

        if (token.isNullOrBlank()) {
            Toast.makeText(
                context,
                "No Google account connected.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        coroutineScope.launch {
            cloudUploading = true

            try {
                val currentNotes = viewModel.items.value.toList()
                val json = NoteBackupManager.createJson(currentNotes)

                val uploaded = GoogleDriveBackupManager.uploadBackup(
                    token,
                    json
                )

                Toast.makeText(
                    context,
                    "Cloud backup successful: ${uploaded.name}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {

                val isAuthError = handleDriveAuthError(
                    error = e,
                    operation = "Cloud backup"
                )

                if (!isAuthError) {
                    Log.e("DiaryCloudBackup", "Cloud backup failed.", e)

                    Toast.makeText(
                        context,
                        "Cloud backup could not be completed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } finally {
                cloudUploading = false
            }
        }
    }

    fun createLocalBackup() {
        val currentNotes = viewModel.items.value.toList()

        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                NoteBackupManager.saveBackup(
                    context = context,
                    notes = currentNotes
                )
            }

            result.onSuccess { count ->
                Toast.makeText(
                    context,
                    "Local backup saved: $count notes.",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "Local backup failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun createLocalAndCloudBackup() {
        val currentNotes = viewModel.items.value.toList()

        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                NoteBackupManager.saveBackup(
                    context = context,
                    notes = currentNotes
                )
            }

            result.onSuccess { count ->
                Toast.makeText(
                    context,
                    "Local backup saved: $count notes.",
                    Toast.LENGTH_SHORT
                ).show()

                // Local backup ke baad Cloud Backup
                backupToGoogleDrive()

            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "Local backup failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun loadCloudBackups(openManageDialog: Boolean = true) {
        if (cloudLoading) return

        val token = driveAccessToken

        if (token.isNullOrBlank() || isDriveAuthExpired) {
            if (openManageDialog) {
                showCloudBackups = true
            }
            return
        }

        coroutineScope.launch {
            cloudLoading = true

            try {
                cloudBackups = GoogleDriveBackupManager.listBackups(token)

                if (openManageDialog) {
                    showCloudBackups = true
                }

                if (cloudBackups.isEmpty()) {
                    Toast.makeText(
                        context,
                        "No backups found on Google Drive.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                val isAuthError = handleDriveAuthError(
                    error = e,
                    operation = "Load cloud backup list"
                )

                if (!isAuthError) {
                    Log.e("DiaryCloudBackup", "Unable to load cloud backups.", e)

                    Toast.makeText(
                        context,
                        "Unable to load cloud backups. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()

                    if (openManageDialog) {
                        showCloudBackups = true
                    }
                } else if (openManageDialog) {
                    showCloudBackups = true
                }

            } finally {
                cloudLoading = false
            }
        }
    }

    fun loadCloudBackupsForHistory() {
        if (cloudLoading) return

        val token = driveAccessToken

        if (token.isNullOrBlank() || isDriveAuthExpired) {
            cloudBackups = emptyList()
            return
        }

        coroutineScope.launch {
            cloudLoading = true

            try {
                cloudBackups = GoogleDriveBackupManager.listBackups(token)

            } catch (e: Exception) {

                val isAuthError = handleDriveAuthError(
                    error = e,
                    operation = "Load backup history"
                )

                if (!isAuthError) {
                    Log.e("DiaryCloudBackup", "Unable to load backup history.", e)

                    Toast.makeText(
                        context,
                        "Unable to load cloud backup history. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } finally {
                cloudLoading = false
            }
        }
    }

    fun restoreCloudBackup(backup: CloudBackup) {
        val token = driveAccessToken

        if (token.isNullOrBlank()) {
            Toast.makeText(
                context,
                "No Google account connected.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        coroutineScope.launch {
            cloudLoading = true

            try {
                val json = withContext(Dispatchers.IO) {
                    GoogleDriveBackupManager.downloadBackup(
                        accessToken = token,
                        fileId = backup.id
                    )
                }

                val root = JSONObject(json)

                require(root.optString("app") == "DiaryApp") {
                    "This is not a valid DiaryApp backup file."
                }

                val jsonArray = root.getJSONArray("notes")
                val restoredNotes = mutableListOf<NoteItem>()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)

                    restoredNotes.add(
                        NoteItem(
                            id = item.getLong("id"),
                            title = item.getString("title"),
                            message = item.getString("message"),
                            date = item.getString("date"),
                            createdAt = item.getLong("createdAt"),
                            modifiedAt = item.getLong("modifiedAt")
                        )
                    )
                }

                viewModel.restoreNotes(restoredNotes)

                Toast.makeText(
                    context,
                    "${restoredNotes.size} notes restored successfully.",
                    Toast.LENGTH_LONG
                ).show()

                showCloudBackups = false

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Cloud restore failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                cloudLoading = false
            }
        }
    }

    fun deleteCloudBackup(backup: CloudBackup) {
        val token = driveAccessToken

        if (token.isNullOrBlank()) {
            Toast.makeText(
                context,
                "No Google account connected.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        coroutineScope.launch {
            cloudLoading = true

            try {
                GoogleDriveBackupManager.deleteBackup(
                    accessToken = token,
                    fileId = backup.id
                )

                cloudBackups = cloudBackups.filterNot {
                    it.id == backup.id
                }

                Toast.makeText(
                    context,
                    "Cloud backup deleted successfully.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Delete failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                cloudLoading = false
            }
        }
    }

    LaunchedEffect(
        driveAccessToken,
        pendingCloudBackup,
        isDriveAuthExpired
    ) {
        if (
            !driveAccessToken.isNullOrBlank() &&
            !isDriveAuthExpired &&
            pendingCloudBackup
        ) {
            pendingCloudBackup = false
            createLocalAndCloudBackup()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ProfileBottomBar(
                onHomeClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                onAddClick = {
                    navController.navigate("add") {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 16.dp),
            /*.padding(horizontal = 18.dp, vertical = 16.dp),*/
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.android_logo_4_1
                            ),
                            contentDescription = "diaryApp Icon",
                            modifier = Modifier.size(54.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = "DiaryApp - Memories Recorder",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = "Your personal memories",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // Profile heading
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Profile & Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )

                Text(
                    text = "Manage your notes, backups and app preferences.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )
            }


            // Overview cards: icons intentionally removed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewCard(
                    title = totalNotes.toString(),
                    subtitle = "Total Notes",
                    modifier = Modifier.weight(1f)
                )

                OverviewCard(
                    title = "Protected",
                    subtitle = "Local-first backup",
                    modifier = Modifier.weight(1f)
                )
            }

            SectionHeading(
                icon = Icons.Filled.Person,
                title = "Google Account"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Selected Google Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = selectedGoogleEmail ?: "No Google account connected",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    if (selectedGoogleEmail.isNullOrBlank()) {

                        // First time: only Add Account
                        TextButton(
                            onClick = {
                                accountSelectionMode = "ADD"
                                requestDriveAccess()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Account")
                        }

                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {

                            if (driveAccessToken.isNullOrBlank() || isDriveAuthExpired) {
                                TextButton(
                                    onClick = {
                                        accountSelectionMode = "RECONNECT"
                                        requestDriveAccess()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Reconnect Google Account")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        accountSelectionMode = "CHANGE"
                                        requestDriveAccess()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Change Account")
                                }

                                TextButton(
                                    onClick = {
                                        removeGoogleAccount()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Remove Account", color = body3)
                                }
                            }
                        }
                    }
                }
            }

            // Backup & Restore
            SectionHeading(
                icon = Icons.Filled.Restore,
                title = "Backup & Restore"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Automatic Backup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Automatic Backup",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Automatic Backup",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Serif
                            )

                            Text(
                                /* text = "Every 15 minutes · Testing",*/
                                text = "Every 7 days",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Serif
                            )
                        }
                        //val context = LocalContext.current

                        val preferences = remember {
                            context.getSharedPreferences("diary_settings", Context.MODE_PRIVATE)
                        }

                        var autoBackupEnabled by remember {
                            mutableStateOf(
                                preferences.getBoolean("auto_backup_enabled", false)
                            )
                        }

                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { enabled ->

                                autoBackupEnabled = enabled

                                preferences.edit()
                                    .putBoolean("auto_backup_enabled", enabled)
                                    .apply()

                                if (enabled) {

                                    // Local backup must work even without Google token.
                                    AutoBackupScheduler.schedule(context)

                                    if (driveAccessToken.isNullOrBlank() || isDriveAuthExpired) {

                                        Toast.makeText(
                                            context,
                                            "Automatic local backup enabled. Reconnect Google account for cloud backup.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                    } else {

                                        Toast.makeText(
                                            context,
                                            "Automatic local and cloud backup enabled.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                } else {

                                    // User manually turns OFF automatic backup.
                                    AutoBackupScheduler.cancel(context)

                                    Toast.makeText(
                                        context,
                                        "Automatic Backup disabled.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }

                    Text(
                        text = "Your notes are backed up automatically to local storage and cloud storage.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif
                    )

                    // Backup button without an icon

                    // Backup Now: Local + Google Drive
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBackupOptionsDialog = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Backup Now",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )
                    }


                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    BackupLocationRow(
                        icon = Icons.Filled.PhoneAndroid,
                        title = "Local Backup",
                        description = "Download/Diary App/Backup/",
                        status = "Local storage",
                        onClick = {
                            val initialUri = DocumentsContract.buildDocumentUri(
                                "com.android.externalstorage.documents",
                                "primary:Download/Diary App/Backup"
                            )

                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                                putExtra(
                                    DocumentsContract.EXTRA_INITIAL_URI,
                                    initialUri
                                )
                            }

                            localBackupLauncher.launch(intent)
                        }
                    )

                    BackupLocationRow(
                        icon = Icons.Filled.Cloud,
                        title = "Google Drive Backup",
                        description = "Hidden appDataFolder",
                        status = if (cloudUploading) "Working..." else "Cloud backup",
                        onClick = {
                            backupToGoogleDrive()
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    ActionRow(
                        icon = Icons.Filled.Restore,
                        title = "Restore Backup",
                        description = "Restore from phone or Google Drive",
                        onClick = {
                            showRestoreSourceDialog = true
                        }
                    )

                    ActionRow(
                        icon = Icons.Filled.History,
                        title = "Backup History",
                        description = "View backup files and sync status",
                        onClick = {
                            showHistoryDialog = true
                            selectedHistoryTab = 0
                            localHistoryLoading = true

                            coroutineScope.launch {
                                try {
                                    localBackups = withContext(Dispatchers.IO) {
                                        NoteBackupManager.getBackupHistory(context)
                                    }
                                } finally {
                                    localHistoryLoading = false
                                }
                            }

                            if (!driveAccessToken.isNullOrBlank() && !isDriveAuthExpired) {
                                loadCloudBackups(openManageDialog = false)
                            }
                        }
                    )

                    ActionRow(
                        icon = Icons.Filled.Delete,
                        title = "Manage Cloud Backups",
                        description = "Manage or delete testing backups",
                        onClick = {
                            loadCloudBackups()
                        }
                    )
                }
            }

            // App Settings: Appearance only
            SectionHeading(
                icon = Icons.Filled.Palette,
                title = "App Settings"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    ActionRow(
                        icon = Icons.Filled.Palette,
                        title = "Appearance",
                        description = selectedAppearance,
                        onClick = {
                            showAppearanceDialog = true
                        }
                    )
                }
            }

            // About
            SectionHeading(
                icon = Icons.Filled.Info,
                title = "About"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    ActionRow(
                        icon = Icons.Filled.Info,
                        title = "About Diary App",
                        description = "App information",
                        onClick = {
                            showAboutDialog = true
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    ActionRow(
                        icon = Icons.Filled.Lock,
                        title = "Privacy & Data",
                        description = "Understand how your data is stored",
                        onClick = {
                            showPrivacyDialog = true
                        }
                    )

                    Text(
                        text = "Version 1.0.0",
                        modifier = Modifier.padding(vertical = 14.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }

// UI-only dialogs for now

// Backup Options Dialog
    if (showBackupOptionsDialog) {
        AlertDialog(
            onDismissRequest = {
                showBackupOptionsDialog = false
            },
            title = {
                Text("Choose Backup Type")
            },
            text = {
                Text("Where would you like to save your backup?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackupOptionsDialog = false
                        createLocalBackup()
                    }
                ) {
                    Text("Local Backup")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBackupOptionsDialog = false

                        if (driveAccessToken.isNullOrBlank()) {
                            showConnectForBackupDialog = true
                        } else {
                            createLocalAndCloudBackup()
                        }
                    }
                ) {
                    Text("Local And Cloud Backup")
                }
            }
        )
    }

// Google Account Confirmation Dialog
    if (showConnectForBackupDialog) {
        AlertDialog(
            onDismissRequest = {
                showConnectForBackupDialog = false
            },
            title = {
                Text("Connect Google Account")
            },
            text = {
                Text(
                    "Google account is not connected. " +
                            "Do you want to connect your account and create " +
                            "Local + Cloud Backup?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConnectForBackupDialog = false
                        pendingCloudBackup = true
                        accountSelectionMode = "ADD"
                        requestDriveAccess()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConnectForBackupDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }

    if (showHistoryDialog) {

        AlertDialog(
            onDismissRequest = {
                showHistoryDialog = false
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,

            title = {
                Text(
                    text = "Backup History",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp
                )
            },

            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    BackupSegmentedControl(
                        options = listOf(
                            "Local Backup",
                            "Cloud Backup"
                        ),
                        selectedIndex = selectedHistoryTab,
                        onOptionSelected = { index ->
                            selectedHistoryTab = index

                            if (index == 1) {
                                loadCloudBackupsForHistory()
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .verticalScroll(rememberScrollState()),

                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        if (selectedHistoryTab == 0) {

                            when {
                                localHistoryLoading -> {
                                    Text("Loading local backups...")
                                }

                                localBackups.isEmpty() -> {
                                    Text("No local backups found.")
                                }

                                else -> {

                                    Text(
                                        "${localBackups.size} local backup(s) found",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )

                                    localBackups.forEach { backup ->

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.surface
                                            ),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 1.dp
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement =
                                                    Arrangement.spacedBy(5.dp)
                                            ) {

                                                Text(
                                                    backup.fileName,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )

                                                val formattedDate =
                                                    if (backup.dateModified > 0L) {
                                                        SimpleDateFormat(
                                                            "dd MMM yyyy, hh:mm a",
                                                            Locale("en", "IN")
                                                        ).format(
                                                            Date(
                                                                backup.dateModified * 1000L
                                                            )
                                                        )
                                                    } else {
                                                        "Date unavailable"
                                                    }

                                                Text(
                                                    "Modified: $formattedDate",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Text(
                                                    "Size: ${
                                                        String.format(
                                                            Locale.getDefault(),
                                                            "%.2f",
                                                            backup.sizeBytes / 1024.0
                                                        )
                                                    } KB",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                TextButton(
                                                    /*                                                    onClick = {
                                                                                                            try {
                                                                                                                val intent =
                                                                                                                    Intent(Intent.ACTION_VIEW).apply {
                                                                                                                        setDataAndType(
                                                                                                                            backup.uri,*/
                                                    // "*/*"
                                                    /*"application/json"*/
                                                    /*   )

                                                       addFlags(
                                                           Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                       )
                                                   }

                                               val chooser = Intent.createChooser(
                                                   intent,
                                                   "Open Backup With"
                                               )

                                               context.startActivity(chooser)

                                           } catch (e: Exception) {
                                               Toast.makeText(
                                                   context,
                                                   "Unable to open backup: ${e.message}",
                                                   Toast.LENGTH_LONG
                                               ).show()
                                           }
                                       }*/

                                                    onClick = {
                                                        showHistoryDialog = false

                                                        navController.navigate(
                                                            "backup_preview/" +
                                                                    "${Uri.encode(backup.uri.toString())}/" +
                                                                    Uri.encode(backup.fileName)
                                                        )
                                                    }
                                                ) {
                                                    Text("Open Backup")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } else {

                            when {
                                cloudLoading -> {
                                    Text("Loading cloud backups...")
                                }

                                driveAccessToken.isNullOrBlank() || isDriveAuthExpired -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Your Google Drive session has expired. Reconnect your account to view cloud backups.",
                                            textAlign = TextAlign.Center
                                        )

                                        TextButton(
                                            onClick = {
                                                accountSelectionMode = "RECONNECT"
                                                requestDriveAccess()
                                            }
                                        ) {
                                            Text("Reconnect Google Account")
                                        }
                                    }
                                }

                                cloudBackups.isEmpty() -> {
                                    Text("No cloud backups found.")
                                }

                                else -> {

                                    Text(
                                        "${cloudBackups.size} cloud backup(s) found",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )

                                    cloudBackups.forEach { backup ->

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.surface
                                            ),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 0.dp
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement =
                                                    Arrangement.spacedBy(5.dp)
                                            ) {

                                                Text(
                                                    backup.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )

                                                Text(
                                                    "Modified: ${
                                                        formatCloudDateIST(
                                                            backup.modifiedTime
                                                        )
                                                    }",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                TextButton(
                                                    onClick = {
                                                        restoreCloudBackup(backup)
                                                    },
                                                    enabled = !cloudLoading
                                                ) {
                                                    Text("Restore")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showHistoryDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }


    if (showCloudDialog) {
        FeatureDialog(
            title = "Manage Cloud Backups",
            message = buildAnnotatedString {
                append("Cloud backup management will be implemented later.")
            },
            onDismiss = { showCloudDialog = false }
        )
    }

    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = {
                showAppearanceDialog = false
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = {
                Text(
                    text = "Appearance",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf(
                        "Light",
                        "Dark",
                        "System default"
                    ).forEach { appearance ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ThemePreferences.setThemeMode(
                                        context = context,
                                        mode = AppThemeMode.fromLabel(appearance)
                                    )
                                    showAppearanceDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = appearance,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Serif
                            )

                            if (selectedAppearance == appearance) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAppearanceDialog = false
                    }
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    if (showAboutDialog) {
        FeatureDialog(
            title = "About Diary App",
            message = buildAnnotatedString {
                append("DiaryApp is your personal space to record and preserve your memories.\n\n")

                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Features:")
                }

                append(
                    """

                • Create and manage personal notes.
                • Edit and view saved memories.
                • Create local backups.
                • Back up and restore notes using Google Drive.
                • Manage app appearance and preferences.

                Your memories, organized in one place.
                """.trimIndent()
                )
            },
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showPrivacyDialog) {
        FeatureDialog(
            title = "Privacy & Data",
            message = buildAnnotatedString {
                append("DiaryApp stores your notes in the app's local database.\n\n")

                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Local Backup:")
                }
                append("\nYou can create backup files on your device and restore them when needed.\n\n")

                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Google Drive Backup:")
                }
                append("\nIf you connect a Google account, the app can use Google Drive App Data storage for cloud backups.\n\n")

                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Account Management:")
                }
                append("\nYou can disconnect your Google account from DiaryApp using Remove Account.\n\n")

                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Data Deletion:")
                }
                append("\nUninstalling the app may delete its locally stored data. Cloud backups may remain in Google Drive until you delete them.\n\n")

                append("Your notes are not uploaded to Google Drive unless you use the cloud backup feature.")
            },
            onDismiss = { showPrivacyDialog = false }
        )
    }


    /*
        if (showCloudBackups) {
            AlertDialog(
                onDismissRequest = {
                    showCloudBackups = false
                },
                title = {
                    Text(text = "Google Drive Backups")
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (cloudBackups.isEmpty()) {
                            Text(text = "No cloud backups found.")
                        } else {
                            for (backup in cloudBackups) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = backup.name,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text(
                                            text = "Modified: ${formatCloudDateIST(backup.modifiedTime)}"
                                        )

                                        Row {
                                            TextButton(
                                                onClick = {
                                                    restoreCloudBackup(backup)
                                                },
                                                enabled = !cloudLoading
                                            ) {
                                                Text("Restore")
                                            }

                                            TextButton(
                                                onClick = {
                                                    deleteCloudBackup(backup)
                                                },
                                                enabled = !cloudLoading
                                            ) {
                                                Text("Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCloudBackups = false
                        }
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    */

    if (showCloudBackups) {

        AlertDialog(
            onDismissRequest = {
                showCloudBackups = false
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,

            title = {
                Text(
                    text = "Manage Cloud Backups",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp
                )
            },

            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Same segmented-control appearance
                    BackupSegmentedControl(
                        options = listOf("Cloud Backup"),
                        selectedIndex = 0,
                        onOptionSelected = {}
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .verticalScroll(rememberScrollState()),

                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        when {
                            cloudLoading -> {
                                Text("Loading cloud backups...")
                            }

                            driveAccessToken.isNullOrBlank() || isDriveAuthExpired -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Your Google Drive session has expired. Reconnect your account to manage cloud backups.",
                                        textAlign = TextAlign.Center
                                    )

                                    TextButton(
                                        onClick = {
                                            accountSelectionMode = "RECONNECT"
                                            requestDriveAccess()
                                        }
                                    ) {
                                        Text("Reconnect Google Account")
                                    }
                                }
                            }

                            cloudBackups.isEmpty() -> {
                                Text("No cloud backups found.")
                            }

                            else -> {

                                Text(
                                    "${cloudBackups.size} cloud backup(s) found",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )

                                cloudBackups.forEach { backup ->

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor =
                                                MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 0.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement =
                                                Arrangement.spacedBy(5.dp)
                                        ) {

                                            Text(
                                                backup.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )

                                            Text(
                                                "Modified: ${
                                                    formatCloudDateIST(
                                                        backup.modifiedTime
                                                    )
                                                }",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(8.dp)
                                            ) {

                                                TextButton(
                                                    onClick = {
                                                        restoreCloudBackup(backup)
                                                    },
                                                    enabled = !cloudLoading
                                                ) {
                                                    Text("Restore")
                                                }

                                                TextButton(
                                                    onClick = {
                                                        deleteCloudBackup(backup)
                                                    },
                                                    enabled = !cloudLoading
                                                ) {
                                                    Text("Delete")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showCloudBackups = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showRestoreSourceDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestoreSourceDialog = false
            },
            title = {
                Text("Restore Backup")
            },
            text = {
                Column {
                    Text(
                        text = "Choose backup source",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    TextButton(
                        onClick = {
                            showRestoreSourceDialog = false
                            restoreLauncher.launch(
                                arrayOf("application/json", "text/json")
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Local Backup")
                    }

                    TextButton(
                        onClick = {
                            showRestoreSourceDialog = false
                            loadCloudBackups()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cloud Backup")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreSourceDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BackupSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->

            val selected = selectedIndex == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable {
                        onOptionSelected(index)
                    }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
private fun SectionHeading(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun BackupLocationRow(
    icon: ImageVector,
    title: String,
    description: String,
    status: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )
            }

            if (onClick != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Open $title",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = status,
            modifier = Modifier.padding(
                start = 36.dp,
                top = 5.dp
            ),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun FeatureDialog(
    title: String,
    message: AnnotatedString,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                fontFamily = FontFamily.Serif
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
            }
        }
    )
}

@Composable
private fun ProfileBottomBar(
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomItem(
                    title = "Home",
                    icon = Icons.Filled.Home,
                    onClick = onHomeClick
                )

                BottomItem(
                    title = "Add",
                    icon = Icons.Filled.Add,
                    onClick = onAddClick
                )

                BottomItem(
                    title = "Profile",
                    icon = Icons.Filled.Person,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun BottomItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable {
            onClick()
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .size(32.dp)
                .padding(horizontal = 0.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 5.dp),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.W600
        )
    }
}

private fun formatCloudDateIST(dateString: String?): String {

    if (dateString.isNullOrBlank()) {
        return "Date unavailable"
    }

    return try {

        val instant = try {
            Instant.parse(dateString)
        } catch (e: Exception) {
            OffsetDateTime.parse(dateString).toInstant()
        }

        val formatter = DateTimeFormatter.ofPattern(
            "dd MMM yyyy, hh:mm a",
            Locale("en", "IN")
        )

        formatter.format(
            instant.atZone(ZoneId.of("Asia/Kolkata"))
        ) + " IST"

    } catch (e: Exception) {
        "Date unavailable"
    }
}