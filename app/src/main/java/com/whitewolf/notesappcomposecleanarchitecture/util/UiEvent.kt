package com.whitewolf.notesappcomposecleanarchitecture.util

sealed class UiEvent {

    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEvent()
    data class NavigateToNoteDetail(val noteId: Int) : UiEvent()
    object NoteSaved : UiEvent()
    object NavigateToPinSettings : UiEvent()
}