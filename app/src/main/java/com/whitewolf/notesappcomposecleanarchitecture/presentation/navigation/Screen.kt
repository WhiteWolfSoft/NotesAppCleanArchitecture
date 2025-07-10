package com.whitewolf.notesappcomposecleanarchitecture.presentation.navigation

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object AddEditNote : Screen("add_edit_note")
    object PinSettingsScreen : Screen ("pin_settings_screen")
}