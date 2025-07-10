package com.whitewolf.notesappcomposecleanarchitecture.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    private val MASTER_PIN_KEY = stringPreferencesKey("master_pin_key")
    private val SECURITY_QUESTION_KEY = stringPreferencesKey("security_question_key")
    private val SECURITY_ANSWER_KEY = stringPreferencesKey("security_answer_key")
    private val FORCE_PIN_SETUP_AFTER_RESET_KEY = booleanPreferencesKey("force_pin_setup_after_reset")

    suspend fun saveMasterPin(pin: String) {
        dataStore.edit { preferences ->
            preferences[MASTER_PIN_KEY] = pin
        }
    }

    fun getMasterPin(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[MASTER_PIN_KEY]
        }
    }

    fun hasMasterPin(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences.contains(MASTER_PIN_KEY)
        }
    }

    suspend fun clearMasterPin() {
        dataStore.edit { preferences ->
            preferences.remove(MASTER_PIN_KEY)
            preferences[FORCE_PIN_SETUP_AFTER_RESET_KEY] = true
        }
    }

    suspend fun saveSecurityQuestionAndAnswer(question: String, answer: String) {
        dataStore.edit { preferences ->
            preferences[SECURITY_QUESTION_KEY] = question
            preferences[SECURITY_ANSWER_KEY] = answer
        }
    }

    fun getSecurityQuestion(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[SECURITY_QUESTION_KEY] ?: ""
        }
    }


    fun getSecurityAnswer(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[SECURITY_ANSWER_KEY] ?: ""
        }
    }

    fun hasSecurityQuestionSet(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences.contains(SECURITY_QUESTION_KEY) && preferences.contains(SECURITY_ANSWER_KEY)
        }
    }


    fun getForcePinSetupAfterReset(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[FORCE_PIN_SETUP_AFTER_RESET_KEY] ?: false
        }
    }

    suspend fun setForcePinSetupAfterReset(force: Boolean) {
        dataStore.edit { preferences ->
            preferences[FORCE_PIN_SETUP_AFTER_RESET_KEY] = force
        }
    }

}