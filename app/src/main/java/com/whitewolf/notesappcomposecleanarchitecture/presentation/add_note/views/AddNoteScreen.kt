package com.whitewolf.notesappcomposecleanarchitecture.presentation.add_note.views


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.whitewolf.notesappcomposecleanarchitecture.R
import com.whitewolf.notesappcomposecleanarchitecture.presentation.add_note.AddNoteViewModel
import com.whitewolf.notesappcomposecleanarchitecture.util.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel,
    noteId: Int?,
    onNoteAdded: () -> Unit,
    onNavigateToPinSettings: () -> Unit
) {


    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkMasterPinStatusOnce()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(noteId) {
        viewModel.loadNoteForEdit(noteId)
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.NoteSaved -> {
                    delay(1050)
                    onNoteAdded()
                    viewModel.onNoteSaveComplete()
                }
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel
                    )
                }
                is UiEvent.NavigateToPinSettings -> {
                    onNavigateToPinSettings()
                }

                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "Yeni Not" else "Notu Düzenle") }
            )
        },
        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    if (!state.isLoading){
                        viewModel.saveNote()
                    }
                },
            ) {
                Icon(Icons.Default.Check, contentDescription = "Kaydet")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ✅ Başlık Alanı
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onAddNoteTitleChange(it) },
                label = { Text("Başlık") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value =  state.content,
                onValueChange = { viewModel.onAddNoteContentChange(it) },
                label = { Text("İçerik") },
                modifier = Modifier.fillMaxWidth()

            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Korumalı Not", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = state.isProtected,
                    onCheckedChange = { viewModel.onAddNoteProtectedChange(it) }
                )

            }


            if (state.isProtected && !state.hasMasterPinSet) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Bilgi",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Korumalı not eklemek için önce bir Ana PIN ayarlamalısın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (state.isLoading) {
                LoadingAnimation()
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.notedonelottie)
    )

    val progress by animateLottieCompositionAsState(composition = composition)

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(350.dp)
    )
}