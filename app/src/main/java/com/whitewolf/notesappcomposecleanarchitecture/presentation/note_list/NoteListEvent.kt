package com.whitewolf.notesappcomposecleanarchitecture.presentation.note_list

sealed class NoteOrder(
    val orderType: OrderType
) {
    class Title(orderType: OrderType): NoteOrder(orderType)
    class Date(orderType: OrderType): NoteOrder(orderType)
}

sealed class OrderType {
    object Ascending: OrderType()
    object Descending: OrderType()
}