package com.whitewolf.notesappcomposecleanarchitecture.data.repository

import com.whitewolf.notesappcomposecleanarchitecture.data.local.NoteDao
import com.whitewolf.notesappcomposecleanarchitecture.data.mapper.toNote
import com.whitewolf.notesappcomposecleanarchitecture.data.mapper.toNoteEntity
import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note
import com.whitewolf.notesappcomposecleanarchitecture.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return dao.getNotes().map { it.map { entity -> entity.toNote() } }
    }

    override suspend fun getNoteById(id: Int): Note? {
        return dao.getNoteById(id)?.toNote()
    }

    override suspend fun addNote(note: Note) {
        dao.insertNote(note.toNoteEntity())
    }

    override suspend fun updateNote(note: Note) {
        dao.updateNote(note.toNoteEntity())
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(note.toNoteEntity())
    }
}