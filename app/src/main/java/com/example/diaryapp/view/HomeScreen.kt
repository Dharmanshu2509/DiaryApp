package com.example.diaryapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diaryapp.MainViewModel
import com.example.diaryapp.ui.theme.body2
import com.example.diaryapp.ui.theme.body3

import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun HomeScreen(controller: NavController, viewModel: MainViewModel) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xff1271CA), Color(0xff5D21DD)
        )
    )

    val gradient2 = Brush.linearGradient(
        colors = listOf(
            body3, body2
        )
    )

    val focusManager = LocalFocusManager.current

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val filteredItems = viewModel.items.value.filter { item ->
        item.title.contains(searchQuery, ignoreCase = true) ||
                item.message.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        /*        topBar = {
                    Text(
                        "Memories Recorder",
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 20.dp),
                        style = TextStyle(
                            fontSize = 32.sp, brush = gradient2, fontFamily = FontFamily.Serif
                        ),
                        textAlign = TextAlign.Center,
                    )
                },*/
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .pointerInput(isSearchVisible) {
                        detectTapGestures {
                            if (isSearchVisible) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                isSearchVisible = false
                                searchQuery = ""
                            }
                        }
                    }
            ) {
                Text(
                    text = "Memories Recorder",
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        fontSize = 32.sp,
                        brush = gradient2,
                        fontFamily = FontFamily.Serif
                    ),
                    textAlign = TextAlign.Center
                )
                if (!isSearchVisible) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search memories",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                            .clickable {
                                isSearchVisible = true
                            },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        bottomBar = {
            val items = listOf(
                Items("Home", Icons.Filled.Home),
                Items("Add", Icons.Filled.Add),
                Items("Profile", Icons.Filled.Person)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp)
            ) {
                Row(
                    Modifier
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
                    items.forEachIndexed { index, item ->
                        BottomItem(item) {
                            when (index) {
                                /*                       0 -> controller.navigate("home") {
                                                           launchSingleTop = true
                                                       }*/

                                1 -> controller.navigate("add") {
                                    launchSingleTop = true
                                }

                                2 -> controller.navigate("profile") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }
        },

        ) { innerPadding ->

        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .pointerInput(isSearchVisible) {
                    detectTapGestures {
                        if (isSearchVisible) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            isSearchVisible = false
                            searchQuery = ""
                        }
                    }
                },
            /*                .pointerInput(Unit) {
                                detectTapGestures {
                                    focusManager.clearFocus()
                                }
                            },*/
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            if (isSearchVisible) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text("Search memories...")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.clickable {
                                        searchQuery = ""
                                    }
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            /*            item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                placeholder = {
                                    Text("Search memories...")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search"
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Clear search",
                                            modifier = Modifier.clickable {
                                                searchQuery = ""
                                            }
                                        )
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(15.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }*/

            items(
                items = filteredItems,
                key = { it.id }
            ) { item ->
                SwipeToDeleteItem(
                    title = item.title,
                    message = item.message,
                    date = item.date,
                    onClick = {
                        controller.navigate("view/${item.id}")
                    },
                    onDelete = {
                        viewModel.deleteNote(item.id)
                    }
                )
            }
            /*            itemsIndexed(viewModel.items.value) { index, item ->
                            ListItem(
                                headlineContent = {
                                    Text(item.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Serif)
                                },
                                supportingContent = {
                                    Text(item.message, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, maxLines = 2,)
                                },
                                overlineContent = {
                                    Text(item.date, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif)
                                },
                                trailingContent = {
                                    Icon(
                                        Icons.Filled.MoreVert, contentDescription = null, Modifier.clickable {
                                            showDropDown = item.id
                                        }, tint = MaterialTheme.colorScheme.primary)
                                    DropdownMenu(
                                        expanded = showDropDown == item.id, onDismissRequest = { showDropDown = 0L }) {
                                        DropdownMenuItem(text = {
                                            Text("Edit", fontFamily = FontFamily.Serif)
                                        }, onClick = {
                                            controller.navigate("edit/${item.id}")
                                            showDropDown = 0L
                                        })
                                        DropdownMenuItem(text = {
                                            Text("Delete", fontFamily = FontFamily.Serif)
                                        }, onClick = {
                                            viewModel.deleteNote(item.id)
                                            showDropDown = 0L
                                        })
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .clickable{
                                        controller.navigate("view/${item.id}")
                                    }
                                    .background(body, RoundedCornerShape(size = 15.dp)),
                                colors = ListItemDefaults.colors(
                                    containerColor = transparent
                                )
                            )
                        }*/
        }
    }
}

@Composable
private fun SwipeToDeleteItem(
    title: String,
    message: String,
    date: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var itemWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Red background ke upar Delete icon aur text
        Row(
            modifier = Modifier
                .align(
                    if (offsetX > 0f) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    }
                )
                .padding(
                    start = if (offsetX > 0f) 20.dp else 0.dp,
                    end = if (offsetX < 0f) 20.dp else 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete note",
                tint = body3,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = "Delete",
                color = body3,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }

        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif
                )
            },
            supportingContent = {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 2
                )
            },
            overlineContent = {
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged {
                    itemWidth = it.width.toFloat()
                }
                .offset {
                    IntOffset(offsetX.roundToInt(), 0)
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(itemWidth) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            offsetX = (offsetX + dragAmount)
                                .coerceIn(-itemWidth, itemWidth)
                        },
                        onDragEnd = {
                            if (
                                itemWidth > 0f &&
                                kotlin.math.abs(offsetX) >= itemWidth * 0.4f
                            ) {
                                onDelete()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        }
                    )
                }
                .clickable {
                    if (offsetX == 0f) {
                        onClick()
                    } else {
                        offsetX = 0f
                    }
                },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

data class Items(
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomItem(items: Items, onclick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onclick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = items.icon,
            contentDescription = items.title,
            modifier = Modifier
                .size(32.dp)
                .padding(horizontal = 0.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = items.title,
            modifier = Modifier.padding(horizontal = 5.dp),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.W600
        )
    }
}
