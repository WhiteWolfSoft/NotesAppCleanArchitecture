package com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.update_note

import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note
import com.whitewolf.notesappcomposecleanarchitecture.domain.repository.NoteRepository

class UpdateNoteUseCase(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(note: Note) {
        repository.updateNote(note)
    }

}