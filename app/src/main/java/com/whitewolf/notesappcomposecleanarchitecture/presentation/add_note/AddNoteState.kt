package com.whitewolf.notesappcomposecleanarchitecture.presentation.add_note

data class AddNoteState(
    val title: String = "",
    val content: String = "",
    val isProtected: Boolean = false,
    val isLoading: Boolean = false,
    val currentNoteId: Int? = null,
    val hasMasterPinSet: Boolean = false
)