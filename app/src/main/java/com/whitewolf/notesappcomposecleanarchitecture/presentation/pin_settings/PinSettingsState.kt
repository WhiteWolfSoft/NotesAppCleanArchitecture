package com.whitewolf.notesappcomposecleanarchitecture.presentation.pin_settings

data class PinSettingsState(
    val hasMasterPinSet: Boolean = false,
    val hasSecurityQuestionSet: Boolean = false,
    val oldPinInput: String = "",
    val newPinInput: String = "",
    val confirmPinInput: String = "",
    val securityQuestionInput: String = "",
    val securityAnswerInput: String = "",
    val oldPinError: Boolean = false,
    val pinError: Boolean = false,
    val confirmPinError: Boolean = false,
    val questionError: Boolean = false,
    val answerError: Boolean = false,
    val showResetPinDialog: Boolean = false,
    val currentSecurityQuestion: String? = null,
    val enteredSecurityAnswer: String = "",
    val resetPinError: Boolean = false,
    val isForceSetup: Boolean = false,
    val showSecurityQuestionForSetup: Boolean = false,
    val enteredSecurityAnswerForSetup: String = "",
    val securityAnswerForSetupError: Boolean = false,
    val isSecurityQuestionVerified: Boolean = false
)