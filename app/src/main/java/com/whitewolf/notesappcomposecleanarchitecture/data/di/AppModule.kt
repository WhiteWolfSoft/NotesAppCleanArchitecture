package com.whitewolf.notesappcomposecleanarchitecture.data.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.whitewolf.notesappcomposecleanarchitecture.data.local.NoteDatabase
import com.whitewolf.notesappcomposecleanarchitecture.data.preferences.AppPreferences
import com.whitewolf.notesappcomposecleanarchitecture.data.repository.NoteRepositoryImpl
import com.whitewolf.notesappcomposecleanarchitecture.domain.repository.NoteRepository
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.NoteUseCases
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.add_note.AddNoteUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.delete_note.DeleteNoteUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.get_note.GetNoteUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.get_notes.GetNotesUseCase
import com.whitewolf.notesappcomposecleanarchitecture.domain.usecase.update_note.UpdateNoteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(app: Application): NoteDatabase {
        return Room.databaseBuilder(
            app,
            NoteDatabase::class.java,
            "note_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(db: NoteDatabase) = db.noteDao()

    @Provides
    @Singleton
    fun provideNoteRepository(db: NoteDatabase): NoteRepository {
        return NoteRepositoryImpl(db.noteDao())
    }

    @Provides
    @Singleton
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases {
        return NoteUseCases(
            addNote = AddNoteUseCase(repository),
            deleteNote = DeleteNoteUseCase(repository),
            getNote = GetNoteUseCase(repository),
            getNotes = GetNotesUseCase(repository),
            updateNote = UpdateNoteUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

}