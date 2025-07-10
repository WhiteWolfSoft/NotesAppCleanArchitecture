package com.whitewolf.notesappcomposecleanarchitecture.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isProtected: Boolean = false,
)