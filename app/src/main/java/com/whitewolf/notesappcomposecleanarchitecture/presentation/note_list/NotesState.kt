package com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list

import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note

data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val searchQuery: String = "",
    val includeProtected: Boolean = false,
    val showPinDialog: Boolean = false,
    val pinInput: String = "",
    val selectedNoteIdForPin: Int? = null,
    val showPinError: Boolean = false,
    val hasMasterPinSet: Boolean = false,
    val selectedNoteIds: Set<Int> = emptySet(),
    val isInSelectionMode: Boolean = false,
    val showBulkDeleteConfirmDialog: Boolean = false,
    val isLoading: Boolean = true

)