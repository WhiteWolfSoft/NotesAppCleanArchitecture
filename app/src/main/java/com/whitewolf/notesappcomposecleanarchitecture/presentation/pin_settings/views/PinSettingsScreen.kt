package com.whitewolf.notesappcomposecleanarchitecture.presentation.pin_settings.views

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.whitewolf.notesappcomposecleanarchitecture.presentation.pin_settings.PinSettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSettingsScreen(
    viewModel: PinSettingsViewModel = hiltViewModel(),
    onSettingsSaved: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is PinSettingsViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is PinSettingsViewModel.UiEvent.SettingsSaved -> {
                    onSettingsSaved()
                }

            }
        }
    }


    val shouldPreventBack = state.isForceSetup && !state.isSecurityQuestionVerified

    BackHandler(enabled = true) {
        if (shouldPreventBack) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Yeni bir PIN ayarlamak için önce güvenlik sorusunu doğrulamanız gerekmektedir.")
            }
        } else {
            viewModel.requestNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("PIN Ayarları") },
                navigationIcon = {
                    if (!shouldPreventBack) {
                        IconButton(
                            onClick = {
                                viewModel.requestNavigateBack()
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.isForceSetup && !state.isSecurityQuestionVerified) {
                Text(
                    text = "PIN'iniz sıfırlandı. Yeni PIN belirlemeden önce güvenlik sorunuzu doğrulamanız gerekmektedir.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Güvenlik sorunuz: ${state.currentSecurityQuestion ?: "Güvenlik sorusu yükleniyor..."}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = state.enteredSecurityAnswerForSetup,
                    onValueChange = viewModel::onSecurityAnswerForSetupChange,
                    label = { Text("Cevabınız") },
                    singleLine = true,
                    isError = state.securityAnswerForSetupError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                if (state.securityAnswerForSetupError) {
                    Text("Yanlış cevap!", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::onVerifySecurityAnswerForSetup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Doğrula")
                }

            } else {
                if (!state.hasMasterPinSet) {
                    Text(
                        text = "Lütfen korumalı notlarınız için bir Ana PIN ve güvenlik sorusu belirleyin.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = "Ana PIN'inizi değiştirmek için eski PIN'inizi girin. PIN'i unuttuysanız sıfırlayabilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (state.hasMasterPinSet) {
                    OutlinedTextField(
                        value = state.oldPinInput,
                        onValueChange = viewModel::onOldPinChange,
                        label = { Text("Eski PIN Kodu") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = state.oldPinError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.oldPinError) {
                        Text("Eski PIN hatalı veya boş", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = state.newPinInput,
                    onValueChange = viewModel::onNewPinChange,
                    label = { Text(if (state.hasMasterPinSet) "Yeni PIN Kodu" else "PIN Kodu") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = state.pinError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.pinError) {
                    Text("PIN boş bırakılamaz", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.confirmPinInput,
                    onValueChange = viewModel::onConfirmPinChange,
                    label = { Text("PIN'i Onayla") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = state.confirmPinError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.confirmPinError) {
                    Text("PIN'ler uyuşmuyor veya boş", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Güvenlik Sorusu (PIN'inizi unutursanız)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.securityQuestionInput,
                    onValueChange = viewModel::onSecurityQuestionChange,
                    label = { Text("Örn: İlk evcil hayvanınızın adı neydi?") },
                    singleLine = true,
                    isError = state.questionError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.questionError) {
                    Text("Güvenlik sorusu boş bırakılamaz", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.securityAnswerInput,
                    onValueChange = viewModel::onSecurityAnswerChange,
                    label = { Text("Cevap") },
                    singleLine = true,
                    isError = state.answerError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.answerError) {
                    Text("Cevap boş bırakılamaz", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::onSetPinClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.hasMasterPinSet) "PIN'i Güncelle" else "PIN'i Ayarla")
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (state.hasMasterPinSet) {
                    Button(
                        onClick = viewModel::onResetPinClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("PIN'i Sıfırla")
                    }
                }
            }
        }
    }

    if (state.showResetPinDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onResetDialogDismiss,
            title = { Text("PIN'i Sıfırla") },
            text = {
                Column {
                    Text("Güvenlik sorunuz: ${state.currentSecurityQuestion ?: "Yükleniyor..."}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.enteredSecurityAnswer,
                        onValueChange = viewModel::onResetDialogAnswerChange,
                        label = { Text("Cevabınız") },
                        singleLine = true,
                        isError = state.resetPinError,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    if (state.resetPinError) {
                        Text("Yanlış cevap!", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onResetPinConfirm) {
                    Text("Sıfırla")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onResetDialogDismiss) {
                    Text("İptal")
                }
            }
        )
    }
}