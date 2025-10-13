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
import androidx.lifecycle.viewModelScope
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

class ForgotPasswordUpdateViewModel()  : BaseViewModel<ForgotPasswordUpdateUiState, ForgotPasswordUpdateEvent>() {

    val log = logger()

    override fun initialState(): ForgotPasswordUpdateUiState {
        return ForgotPasswordUpdateUiState()
    }

    override fun onEvent(event: ForgotPasswordUpdateEvent) {
        when (event) {
            is ForgotPasswordUpdateEvent.OnForgotPasswordUpdateRequest -> {

                viewModelScope.launch(Dispatchers.IO) {
                    _state.update { it.copy(loading = true) }
                    val response = UserUtil.passwordRecovery(
                        event.email
                    )
                    _state.update { it.copy(loading = false) }

                    log.info("Response code: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")

                    if (response.isSuccessful) {
//                        sendUiEvent(ForgotPasswordUiEvent.NavigateToNextScreen)
                        val startIntent = Intent(event.context, MainActivity::class.java)
                        event.context.startActivity(startIntent)
                        event.activity.finish()
                    } else {
                        sendUiEvent(
                            ShowToast(
                                "Email doesn't exist in the system",
                                Toast.LENGTH_SHORT
                            )
                        )
                    }
                }
            }

        }
    }

    fun getEmailError(email: String, context: Context): String {
        var emailError = ""
        if (email.isBlank()) {
            emailError = context.getString(R.string.forgot_password_error)
        } else if (!email.isEmailValid()) {
            emailError = context.getString(R.string.pickup_parcel_email_error)
        }

        return emailError
    }

}

data class ForgotPasswordUpdateUiState(
    val loading: Boolean = false
)

sealed class ForgotPasswordUpdateEvent() {
    data class OnForgotPasswordUpdateRequest(
        val email: String,
        val context: Context,
        val activity: Activity
    ) : ForgotPasswordUpdateEvent()
}

sealed class ForgotPasswordUpdateUiEvent() : UiEvent {
    object NavigateToNextScreen : ForgotPasswordUpdateUiEvent()

    object NavigateBack : ForgotPasswordUpdateUiEvent()
}