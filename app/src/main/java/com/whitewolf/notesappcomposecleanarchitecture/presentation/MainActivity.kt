package com.whitewolf.notesappcomposecleanarchitecture.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.whitewolf.notesappcomposecleanarchitecture.presentation.navigation.NoteNavGraph
import com.whitewolf.notesappcomposecleanarchitecture.presentation.theme.ui.NotesAppComposeCleanArchitectureTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

           NotesAppComposeCleanArchitectureTheme {
               val navController = rememberNavController()
               NoteNavGraph(navController = navController)
           }

        }
    }
}