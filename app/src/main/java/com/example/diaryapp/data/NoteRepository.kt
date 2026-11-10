
package com.example.diaryapp.data

import androidx.room.withTransaction
import com.example.diaryapp.model.NoteItem
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val database: NoteDatabase
) {
    val allNotes: Flow<List<NoteItem>> =
        noteDao.getAllNotes()

    suspend fun insertNote(note: NoteItem) {
        noteDao.insertNote(note)
    }

    suspend fun insertNotes(notes: List<NoteItem>) {
        if (notes.isEmpty()) return

        database.withTransaction {
            val existingIds = noteDao.getAllNoteIds().toMutableSet()
            val restoredNotes = mutableListOf<NoteItem>()

            for (note in notes) {
                var newId = note.id

                if (newId <= 0L || newId in existingIds) {
                    newId = generateUniqueId(existingIds)
                }

                existingIds.add(newId)
                restoredNotes.add(note.copy(id = newId))
            }

            noteDao.insertNotes(restoredNotes)
        }
    }

    private fun generateUniqueId(existingIds: Set<Long>): Long {
        var newId = System.currentTimeMillis()

        while (newId <= 0L || newId in existingIds) {
            newId++
        }

        return newId
    }

    suspend fun updateNote(note: NoteItem) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
    }
}
