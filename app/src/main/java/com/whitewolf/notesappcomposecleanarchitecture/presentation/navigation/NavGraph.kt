package com.whitewolf.notesappcomposecleanarchitecture.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.whitewolf.notesappcomposecleanarchitecture.presentation.add_note.AddNoteViewModel
import com.whitewolf.notesappcomposecleanarchitecture.presentation.add_note.views.AddNoteScreen
import com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.views.NoteListScreen
import com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.NoteViewModel
import com.whitewolf.notesappcomposecleanarchitecture.presentation.pin_settings.PinSettingsViewModel
import com.whitewolf.notesappcomposecleanarchitecture.presentation.pin_settings.views.PinSettingsScreen


@Composable
fun NoteNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route,
    ) {
        composable(route = Screen.NoteList.route) {
            val viewModel: NoteViewModel = hiltViewModel()
            NoteListScreen(
                viewModel = viewModel,
                onAddNoteClick = {
                    navController.navigate(Screen.AddEditNote.route)
                },
                onNoteClick = { noteId ->
                    navController.navigate("${Screen.AddEditNote.route}?noteId=$noteId")
                },
                onNavigateToPinSettings = { navController.navigate(Screen.PinSettingsScreen.route) }
            )
        }

        composable(
            route = "${Screen.AddEditNote.route}?noteId={noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val viewModel: AddNoteViewModel = hiltViewModel()
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1

            AddNoteScreen (
                viewModel = viewModel,
                noteId = noteId,
                onNoteAdded = { navController.popBackStack() },
                onNavigateToPinSettings = { navController.navigate(Screen.PinSettingsScreen.route) }

            )
        }

        composable(route = Screen.PinSettingsScreen.route) {
            val viewModel: PinSettingsViewModel = hiltViewModel()
            PinSettingsScreen(
                viewModel = viewModel,
                onSettingsSaved = { navController.popBackStack() }
            )
        }
    }
}