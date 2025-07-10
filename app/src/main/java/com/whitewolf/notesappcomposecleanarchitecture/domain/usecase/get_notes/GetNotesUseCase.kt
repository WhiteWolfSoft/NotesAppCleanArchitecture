package com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.get_notes

import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note
import com.whitewolf.notesappcomposecleanarchitecture.domain.repository.NoteRepository
import com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.NoteOrder
import com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNotesUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(
        noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
        query: String = "",
        includeProtected: Boolean = false
    ): Flow<List<Note>> {
        return repository.getNotes().map { notes ->
            // 1) Exclude or include protected notes
            val base = if (includeProtected) notes
            else notes.filter { !it.isProtected }

            // 2) search filter
            val filtered = if (query.isNotBlank()) {
                base.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
            } else base

            // 3) Lastly sorted
            when (noteOrder.orderType) {
                is OrderType.Ascending -> {
                    when (noteOrder) {
                        is NoteOrder.Title -> filtered.sortedBy { it.title.lowercase() }
                        is NoteOrder.Date -> filtered.sortedBy { it.timestamp }
                    }
                }

                is OrderType.Descending -> {
                    when (noteOrder) {
                        is NoteOrder.Title -> filtered.sortedByDescending { it.title.lowercase() }
                        is NoteOrder.Date -> filtered.sortedByDescending { it.timestamp }
                    }
                }

            }

        }
    }
}