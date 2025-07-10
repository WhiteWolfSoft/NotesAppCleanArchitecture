package com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.unit.dp
import com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.NoteOrder
import com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.OrderType

@Composable
fun OrderSection(
    noteOrder: NoteOrder,
    onOrderChange: (NoteOrder) -> Unit,
    expanded: MutableState<Boolean>
) {
    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
        IconButton(onClick = { expanded.value = !expanded.value }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text("Başlık A-Z")
                        if (noteOrder is NoteOrder.Title && noteOrder.orderType == OrderType.Ascending) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Seçili",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                    }

                    }
                },
                onClick = {
                    onOrderChange(NoteOrder.Title(OrderType.Ascending))
                    expanded.value = false
                }
            )

            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Başlık Z-A")
                        if (noteOrder is NoteOrder.Title && noteOrder.orderType == OrderType.Descending) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Seçili",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                onClick = {
                    onOrderChange(NoteOrder.Title(OrderType.Descending))
                    expanded.value = false
                }
            )

            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tarih (En Yeni)")
                        if (noteOrder is NoteOrder.Date && noteOrder.orderType == OrderType.Descending) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Seçili",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                onClick = {
                    onOrderChange(NoteOrder.Date(OrderType.Descending))
                    expanded.value = false
                }
            )

            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tarih (En Eski)")
                        if (noteOrder is NoteOrder.Date && noteOrder.orderType == OrderType.Ascending) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Seçili",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                onClick = {
                    onOrderChange(NoteOrder.Date(OrderType.Ascending))
                    expanded.value = false
                }
            )
        }
    }
}