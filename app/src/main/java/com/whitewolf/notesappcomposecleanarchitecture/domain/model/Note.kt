package com.whitewolf.notesappcomposecleanarchitecture.domain.model

data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isProtected: Boolean = false,
    )