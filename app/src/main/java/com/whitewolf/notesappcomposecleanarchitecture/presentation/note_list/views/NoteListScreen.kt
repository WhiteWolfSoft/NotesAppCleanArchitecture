package com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.views

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note
import com.whitewolf.notesappcomposecleanarchitecture.util.UiEvent
import com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.NoteViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Int) -> Unit,
    onNavigateToPinSettings: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val expanded = remember { mutableStateOf(false) }
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed && event.actionLabel == "Geri Al") {
                        viewModel.restoreNote()
                    }
                }

                is UiEvent.NavigateToNoteDetail -> {
                    onNoteClick(event.noteId)
                }

                UiEvent.NoteSaved -> {}
                UiEvent.NavigateToPinSettings -> {
                    onNavigateToPinSettings()
                }
            }
        }
    }


    LaunchedEffect(state.noteOrder, state.searchQuery, state.includeProtected) {
        if (state.notes.isNotEmpty()) {
            lazyStaggeredGridState.scrollToItem(0)
        }
    }

    BackHandler(enabled = state.isInSelectionMode) {
        viewModel.exitSelectionMode()
    }


    Scaffold(

        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            Column {
                if (state.isInSelectionMode) {
                    TopAppBar(
                        title = { Text("${state.selectedNoteIds.size} Seçildi") },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.exitSelectionMode() }) {
                                Icon(Icons.Default.Close, contentDescription = "Seçimi İptal Et")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleSelectAll() }) {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Tümünü Seç / Seçimi Kaldır"
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteSelectedNotes() },
                                enabled = state.selectedNoteIds.isNotEmpty()
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Seçili Notları Sil"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                } else {
                    TopAppBar(
                        title = { Text("Notlarım") },
                        actions = {
                            IconButton(onClick = { onNavigateToPinSettings() }) {
                                Icon(Icons.Default.Settings, contentDescription = "PIN Ayarları")
                            }

                            OrderSection(
                                noteOrder = state.noteOrder,
                                onOrderChange = { viewModel.onOrderChange(it) },
                                expanded = expanded
                            )

                        }
                    )
                }

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                    },
                    label = { Text("Ara...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = state.includeProtected,
                        onCheckedChange = {
                            viewModel.onIncludeProtectedChange(it)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Korumalı Notları Dahil Et")
                }
            }

        },
        floatingActionButton = {
            if (!state.isInSelectionMode) {
                FloatingActionButton(onClick = onAddNoteClick) {
                    Icon(Icons.Default.Add, contentDescription = "Yeni Not")
                }
            }
        }

    ) { padding ->


        NoteList(
            notes = state.notes,
            onDelete = { note -> viewModel.deleteNote(note) },
            modifier = Modifier.padding(padding),
            lazyStaggeredGridState = lazyStaggeredGridState,
            isInSelectionMode = state.isInSelectionMode,
            selectedNoteIds = state.selectedNoteIds,
            onNoteLongClick = { noteId -> viewModel.enterSelectionMode(noteId) },
            onNoteItemClick = { noteId ->
                if (state.isInSelectionMode) {
                    viewModel.toggleNoteSelection(noteId)
                } else {
                    viewModel.onNoteClickForPinCheck(noteId)
                }
            },
            isLoading = state.isLoading
        )
    }

    if (state.showPinDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onPinDialogDismiss() },
            title = { Text("PIN Kodu Girin") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.pinInput,
                        onValueChange = { viewModel.onPinInputChange(it) },
                        label = { Text("Ana PIN Kodu") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    if (state.showPinError) {
                        Text("Hatalı PIN!", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onPinConfirm() }) {
                    Text("Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onPinDialogDismiss() }) {
                    Text("İptal")
                }
            }
        )
    }

    if (state.showBulkDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissBulkDeleteConfirmDialog() },
            title = { Text("Notları Silmeyi Onayla") },
            text = {
                Text(
                    "Seçili ${state.selectedNoteIds.size} notu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteSelectedNotes() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissBulkDeleteConfirmDialog() }) {
                    Text("İptal")
                }
            }
        )
    }
}


@Composable
fun NoteList(
    notes: List<Note>,
    onDelete: (Note) -> Unit,
    modifier: Modifier = Modifier,
    lazyStaggeredGridState: LazyStaggeredGridState,
    isInSelectionMode: Boolean,
    selectedNoteIds: Set<Int>,
    onNoteLongClick: (Int) -> Unit,
    onNoteItemClick: (Int) -> Unit,
    isLoading: Boolean
) {

    if (isLoading && notes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

        }
    } else if (!isLoading && notes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Henüz bir not eklemediniz.")
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = lazyStaggeredGridState,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = notes,
                key = { note -> note.id!! }
            ) { note ->
                val isSelected = selectedNoteIds.contains(note.id)
                NoteItem(
                    note = note,
                    onDelete = onDelete,
                    isNoteSelected = isSelected,
                    onLongClick = onNoteLongClick,
                    onClick = onNoteItemClick,
                    isInSelectionMode = isInSelectionMode
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: Note,
    onDelete: (Note) -> Unit,
    isNoteSelected: Boolean,
    onLongClick: (Int) -> Unit,
    onClick: (Int) -> Unit,
    isInSelectionMode: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = { note.id?.let { onClick(it) } },
                onLongClick = { note.id?.let { onLongClick(it) } }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 16.dp, end = 40.dp, bottom = 16.dp)
            ) {

                if (note.isProtected) {
                    Text(
                        "Korumalı Not",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\uD83D\uDD11 Bu not korumalıdır. Açmak için tıklayın.",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        note.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isInSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNoteSelected) MaterialTheme.colorScheme.primary
                            else Color.Gray.copy(alpha = 0.5f)
                        )
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    if (isNoteSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seçili",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = { onDelete(note) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Notu Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}