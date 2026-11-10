package com.example.diaryapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diaryapp.MainViewModel
import com.example.diaryapp.model.NoteItem
import java.text.SimpleDateFormat
import java.util.Locale

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(controller: NavController, viewModel: MainViewModel) {
    var title by remember { mutableStateOf(value = "") }
    var message by remember { mutableStateOf(value = "") }
    var formattedDate by remember { mutableStateOf(value = "") }
    val state = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(value = false) }
    var dateTimeFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formattedDate = state.selectedDateMillis?.let {
        dateTimeFormat.format(it)
    } ?: dateTimeFormat.format(System.currentTimeMillis())

    val context = LocalContext.current

    Scaffold(
        Modifier,
        topBar = {
            Row(
                Modifier.padding(top = 20.dp, start = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            controller.navigate("home")
                        }
                        .padding(end = 10.dp)
                )
                Text(
                    text = "Add Note",
                    Modifier,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }
        },
        bottomBar = {
            Box(
                Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        val cleanTitle = title.trim()
                        val cleanMessage = message.trim()
                        val currentTime = System.currentTimeMillis()

                        when {
                            cleanTitle.isEmpty() -> {
                                Toast.makeText(
                                    context,
                                    "Please enter a title.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            cleanMessage.isEmpty() -> {
                                Toast.makeText(
                                    context,
                                    "Please enter your thoughts.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                viewModel.addNote(
                                    NoteItem(
                                        id = System.currentTimeMillis(),
                                        title = cleanTitle,
                                        message = cleanMessage,
                                        date = formattedDate,
                                        createdAt = currentTime,
                                        modifiedAt = currentTime
                                    )
                                )

                                title = ""
                                message = ""

                                controller.navigate("home") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
            ) {
                Text(
                    text = "Save",
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                },
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = formattedDate,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                /*.clickable {
                    showDatePicker = true
                    state.selectedDateMillis = null
                },*/
            )
            /*if (showDatePicker && state.selectedDateMillis == null) {
                DatePicker(state, Modifier.padding(all = 16.dp))
            }*/
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                placeholder = {
                    Text(
                        text = "Set a Title",
                        Modifier,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Serif
                    )
                },
                textStyle = TextStyle(fontFamily = FontFamily.Serif)
            )
            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                },
                Modifier
                    .fillMaxWidth()
                    .heightIn(350.dp, 500.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                placeholder = {
                    Text(
                        text = "Enter Your Thoughts",
                        Modifier,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Serif
                    )
                },
                textStyle = TextStyle(fontFamily = FontFamily.Serif)
            )
        }
    }
}