package com.example.diaryapp.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diaryapp.MainViewModel
import com.example.diaryapp.ui.theme.transparent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit

@Composable
fun ViewScreen(controller: NavController, viewModel: MainViewModel, index: Long) {
    val item = viewModel.items.value.find {
        it.id == index
    }

    // Print selected item's data in Logcat
    LaunchedEffect(item) {
        if (item != null) {
            Log.d("ViewScreen", "Item ID: ${item.id}")
            Log.d("ViewScreen", "Title: ${item.title}")
            Log.d("ViewScreen", "Date: ${item.date}")
            Log.d("ViewScreen", "Message: ${item.message}")
        } else {
            Log.e("ViewScreen", "Item not found for ID: $index")
        }
    }

    var showDropDown by remember {
        mutableStateOf(false)
    }

    Scaffold(
        Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
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
                        .padding(end = 10.dp))
                Text(
                    text = "View",
                    Modifier,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(Modifier.weight(weight = 1f))

                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            controller.navigate("edit/${item?.id ?: index}")
                        }
                        .padding(end = 20.dp, top = 5.dp)
                )

                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            viewModel.deleteNote(item?.id ?: index)
                            controller.navigate("home") {
                                popUpTo("home") {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                        .padding(end = 20.dp, top = 5.dp)
                )
            }
        },
        /*topBar = {
            Row(
                Modifier.padding(top = 20.dp, start = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            controller.navigate("home")
                        }
                        .padding(end = 10.dp)
                )
                Text(
                    text = "View",
                    Modifier,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(Modifier.weight(weight = 1f))
                Icon(
                    Icons.Filled.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 20.dp, top = 5.dp)
                )
            }
        },*/
    ) { innerPadding: PaddingValues ->

        // Outer container: available screen space
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
        ) {

            // Rounded box: full available height
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
            ) {

                // Only this content scrolls
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = 20.dp,
                            vertical = 10.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    SelectionContainer {
                        Column {
                            if (item != null) {
                                //date
                                Text(
                                    text = item.date,
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .fillMaxWidth()
                                        .background(transparent),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Serif
                                )
                                //title
                                Text(
                                    text = item.title,
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .fillMaxWidth()
                                        .background(transparent),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Serif
                                )
                                //msg
                                Text(
                                    text = item.message,
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .fillMaxWidth()
                                        .background(transparent)
                                        .wrapContentHeight(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Serif
                                )
                            } else {
                                Text(
                                    text = "Item not found",
                                    modifier = Modifier.padding(top = 20.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*    Column(
        Modifier
            .fillMaxSize()
            .background(transparent)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier.padding(top = 0.dp, start = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            controller.navigate("home") {
                                launchSingleTop = true
                            }
                        }
                        .padding(end = 10.dp)
                )
                Text(
                    text = "View",
                    Modifier,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(Modifier.weight(weight = 1f))
                Icon(
                    Icons.Filled.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 20.dp, top = 5.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(body4)
                .verticalScroll(rememberScrollState())
        ) {
            if (item != null) {
                //title
                Text(
                    text = item.title,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )
                //date
                Text(
                    text = item.date,
                    modifier = Modifier.padding(top = 5.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )
                //msg
                Text(
                    text = item.message,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif
                )
            } else {
                Text(
                    text = "Item not found",
                    modifier = Modifier.padding(top = 20.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }*/

/*        Text(
            item?.title?:"",
            Modifier.padding(top = 20.dp).fillMaxWidth(),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            item?.date?:"",
            Modifier.padding(top = 5.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            item?.message?:"",
            Modifier.padding(top = 5.dp).fillMaxWidth().wrapContentHeight(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 30.sp
        )*/
