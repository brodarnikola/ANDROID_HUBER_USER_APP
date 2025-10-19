/*
 * Copyright © 2022 Sunbird. All rights reserved.
 *
 * Sunbird Secure Messaging
 *
 * Created by Cinnamon.
 */
package com.sunbird.ui.setup.login

//import hr.sil.android.schlauebox.cache.DataCache
//import hr.sil.android.schlauebox.cache.status.InstallationKeyHandler
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.utils.BaseViewModel
import hr.sil.android.schlauebox.utils.UiEvent
import hr.sil.android.schlauebox.utils.UiEvent.ShowToast
import hr.sil.android.schlauebox.utils.isEmailValid
import hr.sil.android.schlauebox.view.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle)  : BaseViewModel<ForgotPasswordUpdateUiState, ForgotPasswordUpdateEvent>() {

    val log = logger()
    private var email: String = ""

    override fun initialState(): ForgotPasswordUpdateUiState {
        return ForgotPasswordUpdateUiState()
    }

    init {
        email = savedStateHandle.get<String>("emailAddress") ?: ""
    }

    override fun onEvent(event: ForgotPasswordUpdateEvent) {
        when (event) {
            is ForgotPasswordUpdateEvent.OnForgotPasswordUpdateRequest -> {

                viewModelScope.launch(Dispatchers.IO) {
                    _state.update { it.copy(loading = true) }
                    val response = UserUtil.passwordReset(
                        email,
                        event.pin,
                        event.password
                    )
                    _state.update { it.copy(loading = false) }

                    log.info("Response: ${response},   ")

                    if (response) {
                        sendUiEvent(ForgotPasswordUpdateUiEvent.NavigateToNextScreen)
                    } else {
                        sendUiEvent(
                            ShowToast(
                                "Please check your data",
                                Toast.LENGTH_SHORT
                            )
                        )
                    }
                }
            }

        }
    }

    fun getPasswordError(password: String, context: Context): String {
        var passwordError = ""
        if (password.isBlank()) {
            passwordError = "Password can not be empty"
        } else if ( password.length < 6) {
            passwordError = context.getString(R.string.edit_user_validation_password_min_6_characters)
        }

        return passwordError
    }

    fun getRepeatPasswordError(password: String, repeatPassword: String, context: Context): String {
        var passwordError = ""
        if (repeatPassword.isBlank()) {
            passwordError = "Password can not be empty"
        } else if ( password != repeatPassword) {
            passwordError = "Password needs to be the same"
        }

        return passwordError
    }

    fun getPinError(pin: String, context: Context): String {
        var pinError = ""
        if (pin.isBlank()) {
            pinError = "Pin can not be empty"
        }

        return pinError
    }

}

data class ForgotPasswordUpdateUiState(
    val loading: Boolean = false
)

sealed class ForgotPasswordUpdateEvent() {
    data class OnForgotPasswordUpdateRequest(
        val password: String,
        val pin: String,
        val context: Context,
        val activity: Activity
    ) : ForgotPasswordUpdateEvent()
}

sealed class ForgotPasswordUpdateUiEvent() : UiEvent {
    object NavigateToNextScreen : ForgotPasswordUpdateUiEvent()

    object NavigateBack : ForgotPasswordUpdateUiEvent()
}