package com.whitewolf.notesappcomposecleanarchitecture.presentation.pin_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitewolf.notesappcomposecleanarchitecture.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinSettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(PinSettingsState())
    val state: StateFlow<PinSettingsState> = _state.asStateFlow()

    private val _eventFlow = Channel<UiEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    private var _isNavigating = false

    init {
        viewModelScope.launch {
            val preferencesFlow = combine(
                appPreferences.hasMasterPin(),
                appPreferences.hasSecurityQuestionSet(),
                appPreferences.getForcePinSetupAfterReset()
            ) { hasPin, hasQuestion, isForceSetup ->
                Triple(hasPin, hasQuestion, isForceSetup)
            }
                .distinctUntilChanged()

            preferencesFlow.collect { (hasPin, hasQuestion, isForceSetup) ->
                _state.update { currentState ->
                    val showSecurityQuestionForSetup = !hasPin && isForceSetup && !currentState.isSecurityQuestionVerified
                    currentState.copy(
                        hasMasterPinSet = hasPin,
                        hasSecurityQuestionSet = hasQuestion,
                        isForceSetup = isForceSetup,
                        showSecurityQuestionForSetup = showSecurityQuestionForSetup
                    )
                }


                if (!hasPin && isForceSetup && !_state.value.isSecurityQuestionVerified) {
                    val question = appPreferences.getSecurityQuestion().first()
                    _state.update { it.copy(currentSecurityQuestion = question) }
                }

            }
        }

        _state.update {
            it.copy(
                oldPinInput = "",
                newPinInput = "",
                confirmPinInput = "",
                securityQuestionInput = "",
                securityAnswerInput = "",
                pinError = false,
                confirmPinError = false,
                questionError = false,
                answerError = false,
                currentSecurityQuestion = null
            )
        }
    }



    fun onOldPinChange(input: String) {
        _state.update { it.copy(oldPinInput = input, oldPinError = false) }
    }

    fun onNewPinChange(input: String) {
        _state.update { it.copy(newPinInput = input, pinError = false, confirmPinError = false) }
    }

    fun onConfirmPinChange(input: String) {
        _state.update { it.copy(confirmPinInput = input, confirmPinError = false) }
    }

    fun onSecurityQuestionChange(input: String) {
        _state.update { it.copy(securityQuestionInput = input, questionError = false) }
    }

    fun onSecurityAnswerChange(input: String) {
        _state.update { it.copy(securityAnswerInput = input, answerError = false) }
    }

    fun onSecurityAnswerForSetupChange(input: String) {
        _state.update { it.copy(enteredSecurityAnswerForSetup = input, securityAnswerForSetupError = false) }
    }

    fun onVerifySecurityAnswerForSetup() {

        viewModelScope.launch {
            val correctAnwer = appPreferences.getSecurityAnswer().first().trim()
            val enteredAnswer = _state.value.enteredSecurityAnswerForSetup.trim()

            if (enteredAnswer == correctAnwer && correctAnwer.isNotBlank()) {
                _state.update {
                    it.copy(
                        isSecurityQuestionVerified = true,
                        showSecurityQuestionForSetup = false,
                        enteredSecurityAnswerForSetup = ""
                    )
                }
                _eventFlow.send(UiEvent.ShowSnackbar("Güvenlik sorusu başarıyla doğrulandı. Yeni PIN'inizi belirleyebilirsiniz."))
            } else {
                _state.update { it.copy(securityAnswerForSetupError = true) }
                _eventFlow.send(UiEvent.ShowSnackbar("Yanlış güvenlik cevabı veya güvenlik sorusu ayarlı değil!"))
            }
        }
    }


    fun onSetPinClick() {
        viewModelScope.launch {
            if (_state.value.isForceSetup && !_state.value.isSecurityQuestionVerified) {
                _eventFlow.send(UiEvent.ShowSnackbar("Yeni PIN ayarlamak için önce güvenlik sorusunu doğrulamanız gerekmektedir."))
                return@launch
            }

            val hasExistingPin = _state.value.hasMasterPinSet
            val oldPin = _state.value.oldPinInput
            val newPin = _state.value.newPinInput
            val confirmPin = _state.value.confirmPinInput
            val question = _state.value.securityQuestionInput
            val answer = _state.value.securityAnswerInput

            var hasError = false

            if (hasExistingPin) {
                val correctOldPin = appPreferences.getMasterPin().first()
                if (oldPin.isBlank() || oldPin != correctOldPin) {
                    _state.update { it.copy(oldPinError = true) }
                    _eventFlow.send(UiEvent.ShowSnackbar("Eski PIN hatalı veya boş!"))
                    hasError = true
                }
            }

            if (newPin.isBlank()) {
                _state.update { it.copy(pinError = true) }
                hasError = true
            }
            if (confirmPin.isBlank() || newPin != confirmPin) {
                _state.update { it.copy(confirmPinError = true) }
                hasError = true
            }
            if (question.isBlank()) {
                _state.update { it.copy(questionError = true) }
                hasError = true
            }
            if (answer.isBlank()) {
                _state.update { it.copy(answerError = true) }
                hasError = true
            }

            if (hasError) {
                if (!hasExistingPin && !_state.value.isForceSetup) {
                    _eventFlow.send(UiEvent.ShowSnackbar("Lütfen tüm alanları doldurun ve PIN'leri eşleştirin."))
                } else if (_state.value.isForceSetup && _state.value.isSecurityQuestionVerified) {
                    _eventFlow.send(UiEvent.ShowSnackbar("Lütfen tüm alanları doldurun ve PIN'leri eşleştirin."))
                }
                return@launch
            }

            appPreferences.saveMasterPin(newPin)
            appPreferences.saveSecurityQuestionAndAnswer(question, answer)
            appPreferences.setForcePinSetupAfterReset(false)

            _state.update {
                it.copy(
                    oldPinInput = "",
                    newPinInput = "",
                    confirmPinInput = "",
                    securityQuestionInput = "",
                    securityAnswerInput = "",
                    pinError = false,
                    confirmPinError = false,
                    questionError = false,
                    answerError = false,
                    isSecurityQuestionVerified = false,
                    isForceSetup = false,
                    currentSecurityQuestion = null
                )
            }
            _eventFlow.send(UiEvent.ShowSnackbar("PIN ve güvenlik sorusu başarıyla güncellendi!"))
            if (!_isNavigating) {
                _isNavigating = true
                _eventFlow.send(UiEvent.SettingsSaved)
            }
        }
    }

    fun onResetPinClick() {
        viewModelScope.launch {
            val hasQuestion = appPreferences.hasSecurityQuestionSet().first()
            if (!hasQuestion) {
                _eventFlow.send(UiEvent.ShowSnackbar("PIN'i sıfırlamak için önce bir güvenlik sorusu belirlemelisiniz!"))
                return@launch
            }
            _state.update { it.copy(showResetPinDialog = true, enteredSecurityAnswer = "", resetPinError = false) }
            val question = appPreferences.getSecurityQuestion().first()
            _state.update { it.copy(currentSecurityQuestion = question) }
        }
    }

    fun onResetDialogAnswerChange(input: String) {
        _state.update { it.copy(enteredSecurityAnswer = input, resetPinError = false) }
    }

    fun onResetPinConfirm() {

        viewModelScope.launch {
            val correctAnwer = appPreferences.getSecurityAnswer().first().trim()
            val enteredAnswer = _state.value.enteredSecurityAnswer.trim()
            if (enteredAnswer == correctAnwer && correctAnwer.isNotBlank()){
                appPreferences.clearMasterPin()

                _state.update {
                    it.copy(
                        showResetPinDialog = false,
                        enteredSecurityAnswer = "",
                        oldPinInput = "",
                        newPinInput = "",
                        confirmPinInput = "",
                        securityQuestionInput = "",
                        securityAnswerInput = "",
                        isSecurityQuestionVerified = false,
                        currentSecurityQuestion = null
                    )
                }
                _eventFlow.send(UiEvent.ShowSnackbar("PIN başarıyla sıfırlandı. Yeni PIN'inizi belirlemek için güvenlik sorunuzu doğrulayın."))
            } else {
                _state.update { it.copy(resetPinError = true) }
                _eventFlow.send(UiEvent.ShowSnackbar("Yanlış cevap veya güvenlik sorusu ayarlı değil!"))
            }
        }
    }


    fun onResetDialogDismiss() {
        _state.update { it.copy(showResetPinDialog = false, enteredSecurityAnswer = "", resetPinError = false) }
    }

    fun requestNavigateBack() {
        if (!_isNavigating) {
            _isNavigating = true
            viewModelScope.launch {
                _eventFlow.send(UiEvent.SettingsSaved)
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SettingsSaved : UiEvent()
    }
}