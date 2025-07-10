package com.whitewolf.notesappcomposecleanarchitecture.domain.usecase

import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.add_note.AddNoteUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.delete_note.DeleteNoteUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.get_note.GetNoteUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.get_notes.GetNotesUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.update_note.UpdateNoteUseCase

data class NoteUseCases(
    val addNote: AddNoteUseCase,
    val deleteNote: DeleteNoteUseCase,
    val updateNote: UpdateNoteUseCase,
    val getNote: GetNoteUseCase,
    val getNotes: GetNotesUseCase
)