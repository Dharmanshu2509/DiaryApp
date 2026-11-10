package com.example.diaryapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.example.diaryapp.model.NoteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY modifiedAt  DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotes(notes: List<NoteItem>): List<Long>

    @Query("SELECT id FROM notes")
    suspend fun getAllNoteIds(): List<Long>

    @Update
    suspend fun updateNote(note: NoteItem)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)
}
