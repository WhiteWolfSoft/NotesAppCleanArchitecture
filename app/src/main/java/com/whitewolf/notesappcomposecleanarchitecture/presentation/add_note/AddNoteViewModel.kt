package com.whitewolf.notesappcomposecleanarchitecture.presentation.add_note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitewolf.notesappcomposecleanarchitecture.data.preferences.AppPreferences
import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.NoteUseCases
import com.whitewolf.notesappcomposecleanarchitecture.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    private val appPreferences: AppPreferences
):ViewModel(){


    private val _eventFlow = Channel<UiEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    private val _state = MutableStateFlow(AddNoteState())
    val state: StateFlow<AddNoteState> = _state.asStateFlow()


    init {
        viewModelScope.launch {
            appPreferences.hasMasterPin().collectLatest { hasPin ->
                _state.update { it.copy(hasMasterPinSet = hasPin) }
            }
        }

    }

    fun checkMasterPinStatusOnce() {
        viewModelScope.launch {
            val hasPin = appPreferences.hasMasterPin().first()
            _state.update { it.copy(hasMasterPinSet = hasPin) }
        }
    }


    fun onNoteSaveComplete() {
        _state.update { AddNoteState() }
    }

    fun saveNote() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.title.isBlank() && currentState.content.isBlank()) {
                _eventFlow.send(UiEvent.ShowSnackbar("Başlık veya içerik boş olamaz!"))
                return@launch
            }

            if (currentState.isProtected) {
                val currentHasMasterPinSet = appPreferences.hasMasterPin().first()
                if (!currentHasMasterPinSet) { // Eğer PIN ayarlı değilse
                    _eventFlow.send(UiEvent.ShowSnackbar("Korumalı not eklemek için önce bir Ana PIN ayarlamalısın!"))
                    return@launch
                }
            }


            _state.update { it.copy(isLoading = true) }

            val updatedNote = Note(
                id = currentState.currentNoteId,
                title = currentState.title,
                content = currentState.content,
                isProtected = currentState.isProtected,
                timestamp = System.currentTimeMillis()
            )

            noteUseCases.addNote(updatedNote)

            _eventFlow.send(UiEvent.NoteSaved)
        }
    }

    fun onAddNoteProtectedChange(isProtected: Boolean) {

        _state.update { it.copy(isProtected = isProtected) }

        if (isProtected && !_state.value.hasMasterPinSet) {

            viewModelScope.launch {
                val currentHasMasterPinSet = appPreferences.hasMasterPin().first()
                if (!currentHasMasterPinSet) {
                    _eventFlow.send(UiEvent.ShowSnackbar("Korumalı not eklemek için önce bir Ana PIN ayarlamalısın!"))
                    _eventFlow.send(UiEvent.NavigateToPinSettings)
                }
            }

        }
    }

    fun onAddNoteContentChange(content: String) {
        _state.update { it.copy(content = content) }
    }

    fun onAddNoteTitleChange(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun loadNoteForEdit(noteId: Int?) {
        viewModelScope.launch {
            _state.update { it.copy(currentNoteId = noteId) }
            if (noteId != null && noteId != -1) {
                val note = noteUseCases.getNote(noteId)
                note?.let {
                    _state.update { addNoteState ->
                        addNoteState.copy(
                            title = it.title,
                            content = it.content,
                            isProtected = it.isProtected,
                        )
                    }
                }
            } else {
                _state.update { AddNoteState(currentNoteId = null) }
            }
        }
    }

}