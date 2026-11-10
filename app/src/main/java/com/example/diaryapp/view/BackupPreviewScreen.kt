package com.example.diaryapp.view

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diaryapp.model.NoteItem
import com.example.diaryapp.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupPreviewScreen(
    navController: NavController,
    viewModel: MainViewModel,
    uriString: String,
    fileName: String
) {
    val context = LocalContext.current

    var notes by remember { mutableStateOf<List<NoteItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uriString) {
        isLoading = true
        errorMessage = null

        try {
            notes = withContext(Dispatchers.IO) {
                val uri = Uri.parse(uriString)

                val jsonText = context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: error("Unable to read backup file.")

                val backupObject = JSONObject(jsonText)

                require(backupObject.optString("app") == "DiaryApp") {
                    "This is not a valid DiaryApp backup."
                }

                val jsonArray = backupObject.getJSONArray("notes")
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

                restoredNotes
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to load backup."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Backup Preview",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (!isLoading && errorMessage == null) {
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    Button(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Backup", fontFamily = FontFamily.Serif)
                    }
                }
            }
        }
    ) { innerPadding ->

        // Outer container: available screen space
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
            /*.padding(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )*/
        ) {

            // Rounded box: full available height
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        /*.verticalScroll(rememberScrollState())*/
                        /* .padding(16.dp)*/
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        )
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)

                        ) {
                            Text(
                                text = "Backup Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "File Name",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Serif
                            )

                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Serif
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Total Entries",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Serif
                            )

                            Text(
                                text = if (isLoading) "Loading..." else "${notes.size} Notes",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Diary Entries",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            errorMessage != null -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Text(
                                        text = errorMessage ?: "Unknown error",
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }

                            notes.isEmpty() -> {
                                Text(
                                    "This backup does not contain any diary entries.",
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            else -> {
                                notes.forEachIndexed { index, note ->
                                    Card(
                                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = note.date,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontFamily = FontFamily.Serif
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = note.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Serif,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = note.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontFamily = FontFamily.Serif,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }

            if (showRestoreDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showRestoreDialog = false
                    },
                    title = {
                        Text("Restore Backup?", fontFamily = FontFamily.Serif)
                    },
                    text = {
                        Text(
                            "Are you sure you want to restore this backup? \n\nYour existing diary data may be replaced.",
                            fontFamily = FontFamily.Serif
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showRestoreDialog = false
                                viewModel.restoreNotes(notes)
                                navController.popBackStack()
                            }
                        ) {
                            Text("Restore", fontFamily = FontFamily.Serif)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showRestoreDialog = false
                            }
                        ) {
                            Text("Cancel", fontFamily = FontFamily.Serif)
                        }
                    }
                )
            }
        }
    }
}