package com.example.diaryapp

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.NoteDatabase
import com.example.diaryapp.data.NoteRepository
import com.example.diaryapp.model.NoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: NoteRepository

    var items: MutableState<List<NoteItem>> =
        mutableStateOf(emptyList())
        private set

    init {
        val database = NoteDatabase.getDatabase(application)
        repository = NoteRepository(
            noteDao = database.noteDao(),
            database = database
        )

        viewModelScope.launch {
            repository.allNotes.collect { notes ->
                items.value = notes
            }
        }
    }

    fun addNote(note: NoteItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: NoteItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(noteId)
        }
    }

    fun restoreNotes(notes: List<NoteItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNotes(notes)
        }
    }
}