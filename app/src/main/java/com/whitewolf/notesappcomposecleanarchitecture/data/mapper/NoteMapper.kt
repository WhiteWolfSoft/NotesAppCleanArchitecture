package com.whitewolf.notesappcomposecleanarchitecture.data.mapper

import com.whitewolf.notesappcomposecleanarchitecture.data.local.NoteEntity
import com.whitewolf.notesappcomposecleanarchitecture.domain.model.Note

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        isProtected = isProtected,

    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        isProtected = isProtected,
    )
}