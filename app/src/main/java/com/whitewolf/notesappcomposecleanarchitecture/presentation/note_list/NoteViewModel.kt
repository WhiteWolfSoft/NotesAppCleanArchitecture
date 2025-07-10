package com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitewolf.notesappcomposecleanarchitecture.data.preferences.AppPreferences
import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.NoteUseCases
import com.whitewolf.notesappcomposecleanarchitecture.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
class NoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    private val appPreferences: AppPreferences
) : ViewModel() {


    private val _eventFlow = Channel<UiEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    private val _scrollToTopChannel = Channel<Unit>(Channel.BUFFERED)

    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    private var recentlyDeletedNote: Note? = null

    private var getNotesJob: Job? = null


    init {
        viewModelScope.launch {
            appPreferences.hasMasterPin().collectLatest { hasPin ->
                _state.update { it.copy(hasMasterPinSet = hasPin) }
            }
        }
        getNotes()
    }

    private fun getNotes() {

        getNotesJob?.cancel()

        getNotesJob = viewModelScope.launch {
            noteUseCases.getNotes(
                noteOrder = _state.value.noteOrder,
                query = _state.value.searchQuery,
                includeProtected = _state.value.includeProtected
            ).collectLatest { list ->
                _state.update { it.copy(notes = list, isLoading = false) }
            }
        }
    }


    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        getNotes()
    }


    fun onIncludeProtectedChange(include: Boolean) {
        _state.update { it.copy(includeProtected = include) }
        getNotes()
    }

    fun onOrderChange(order: NoteOrder) {
        if (_state.value.noteOrder::class == order::class && _state.value.noteOrder.orderType == order.orderType) {
            return
        }
        _state.update { it.copy(noteOrder = order) }
        getNotes()
        viewModelScope.launch {
            _scrollToTopChannel.send(Unit)
        }
    }


    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteUseCases.deleteNote(note)
            recentlyDeletedNote = note
            _eventFlow.send(UiEvent.ShowSnackbar("Not silindi", "Geri Al"))
        }
    }


    fun restoreNote() {
        viewModelScope.launch {
            recentlyDeletedNote?.let {
                noteUseCases.addNote(it)
                recentlyDeletedNote = null
                _eventFlow.send(UiEvent.ShowSnackbar("Not geri alındı"))
            }
            getNotes()
        }
    }


    fun onNoteClickForPinCheck(noteId: Int) {
        viewModelScope.launch {
            if (_state.value.isInSelectionMode) {
                toggleNoteSelection(noteId)
                return@launch
            }

            val note = noteUseCases.getNote(noteId)
            if (note?.isProtected == true) {
                if (_state.value.hasMasterPinSet) {
                    _state.update {
                        it.copy(
                            showPinDialog = true,
                            selectedNoteIdForPin = noteId,
                            pinInput = "",
                            showPinError = false
                        )
                    }
                } else {
                    _eventFlow.send(UiEvent.NavigateToPinSettings)
                    _eventFlow.send(UiEvent.ShowSnackbar("Korumalı notu açmak için önce bir Ana PIN ayarlamalısın!"))
                    _state.update { it.copy(selectedNoteIdForPin = noteId) }
                }
            } else {
                _eventFlow.send(UiEvent.NavigateToNoteDetail(noteId))
            }
        }
    }


    fun onPinInputChange(input: String) {
        _state.update { it.copy(pinInput = input, showPinError = false) }
    }


    fun onPinConfirm() {
        viewModelScope.launch {
            val correctPin = appPreferences.getMasterPin().first()
            if (_state.value.pinInput == correctPin) {
                val noteIdToNavigate = _state.value.selectedNoteIdForPin
                onPinDialogDismiss()
                delay(200)
                noteIdToNavigate?.let { id ->
                    _eventFlow.send(UiEvent.NavigateToNoteDetail(id))
                }
            } else {
                _state.update { it.copy(showPinError = true) }
            }
        }
    }


    fun onPinDialogDismiss() {
        _state.update {
            it.copy(
                showPinDialog = false,
                pinInput = "",
                selectedNoteIdForPin = null,
                showPinError = false
            )
        }
    }


    fun toggleNoteSelection(noteId: Int) {
        _state.update { currentState ->
            val newSelectedIds = if (currentState.selectedNoteIds.contains(noteId)) {
                currentState.selectedNoteIds - noteId
            } else {
                currentState.selectedNoteIds + noteId
            }

            currentState.copy(
                selectedNoteIds = newSelectedIds,
                isInSelectionMode = newSelectedIds.isNotEmpty()
            )
        }
    }

    fun enterSelectionMode(noteId: Int) {
        _state.update { currentState ->
            if (!currentState.isInSelectionMode) {
                currentState.copy(
                    isInSelectionMode = true,
                    selectedNoteIds = setOf(noteId)
                )
            } else {
                toggleNoteSelection(noteId)
                currentState
            }
        }
    }


    fun exitSelectionMode() {
        _state.update {
            it.copy(
                isInSelectionMode = false,
                selectedNoteIds = emptySet(),
                showBulkDeleteConfirmDialog = false
            )
        }
    }

    fun toggleSelectAll() {
        _state.update { currentState ->
            val allNoteIds = currentState.notes.mapNotNull { it.id }.toSet()
            val newSelectedIds = if (currentState.selectedNoteIds.size == allNoteIds.size) {
                emptySet()
            } else {
                allNoteIds
            }
            currentState.copy(
                selectedNoteIds = newSelectedIds,
                isInSelectionMode = newSelectedIds.isNotEmpty()
            )
        }
    }


    fun deleteSelectedNotes() {
        if (_state.value.selectedNoteIds.isNotEmpty()) {
            onShowBulkDeleteConfirmDialog()
        } else {
            viewModelScope.launch {
                _eventFlow.send(UiEvent.ShowSnackbar("Silinecek not seçilmedi."))
            }
        }
    }


    fun onShowBulkDeleteConfirmDialog() {
        _state.update { it.copy(showBulkDeleteConfirmDialog = true) }
    }


    fun onDismissBulkDeleteConfirmDialog() {
        _state.update { it.copy(showBulkDeleteConfirmDialog = false) }
    }


    fun confirmDeleteSelectedNotes() {
        viewModelScope.launch {
            val selectedNotes =
                _state.value.notes.filter { _state.value.selectedNoteIds.contains(it.id) }
            if (selectedNotes.isNotEmpty()) {
                selectedNotes.forEach { note ->
                    noteUseCases.deleteNote(note)
                }

                _eventFlow.send(UiEvent.ShowSnackbar("${selectedNotes.size} not kalıcı olarak silindi."))
                onDismissBulkDeleteConfirmDialog()
                exitSelectionMode()
                getNotes()
            }
        }
    }

}