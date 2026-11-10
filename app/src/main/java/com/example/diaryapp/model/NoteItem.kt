package com.example.diaryapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey
    val id: Long,
    val title: String,
    val message: String,
    val date: String,
    val createdAt: Long,
    val modifiedAt: Long
)