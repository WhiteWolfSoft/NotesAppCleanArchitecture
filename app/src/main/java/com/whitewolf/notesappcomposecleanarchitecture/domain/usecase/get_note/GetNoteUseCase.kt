package com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.get_note

import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note
import com.whitewolf.notesappcomposecleanarchitecture.domain.repository.NoteRepository

class GetNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Note? {
        return repository.getNoteById(id)
    }
}