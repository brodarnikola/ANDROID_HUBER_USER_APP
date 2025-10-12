/*
 * Copyright © 2022 Sunbird. All rights reserved.
 *
 * Sunbird Secure Messaging
 *
 * Created by Cinnamon.
 */
package com.sunbird.ui.setup.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.App
//import hr.sil.android.schlauebox.cache.DataCache
//import hr.sil.android.schlauebox.cache.status.InstallationKeyHandler
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.UserStatus
import hr.sil.android.schlauebox.core.util.DeviceInfo
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.preferences.PreferenceStore
import hr.sil.android.schlauebox.util.AppUtil
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.util.backend.UserUtil.fcmTokenRequest
import hr.sil.android.schlauebox.util.backend.UserUtil.user
import hr.sil.android.schlauebox.util.backend.UserUtil.userGroup
import hr.sil.android.schlauebox.util.backend.UserUtil.userInvitedTempdata
import hr.sil.android.schlauebox.util.backend.UserUtil.userMemberships
import hr.sil.android.schlauebox.utils.BaseViewModel
import hr.sil.android.schlauebox.utils.NetworkResult
import hr.sil.android.schlauebox.utils.UiEvent
import hr.sil.android.schlauebox.utils.UiEvent.*
import hr.sil.android.schlauebox.utils.isEmailValid
import hr.sil.android.schlauebox.view.ui.MainActivity
import hr.sil.android.schlauebox.view.ui.intro.TCInvitedUserActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : BaseViewModel<ForgotPasswordUiState, ForgotPasswordEvent>() {

    val log = logger()

    override fun initialState(): ForgotPasswordUiState {
        return ForgotPasswordUiState()
    }

    override fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.OnForgotPasswordRequest -> {

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

//                viewModelScope.launch {
//                    _state.update { it.copy(loading = true) }
//                    login(email = event.email, password = event.password, context = event.context)
//                    _state.update { it.copy(loading = false) }
//                }
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

data class ForgotPasswordUiState(
    val loading: Boolean = false
)

sealed class ForgotPasswordEvent() {
    data class OnForgotPasswordRequest(
        val email: String,
        val context: Context,
        val activity: Activity
    ) : ForgotPasswordEvent()
}

sealed class ForgotPasswordUiEvent() : UiEvent {
    object NavigateToNextScreen : LoginScreenUiEvent()

    object NavigateBack : LoginScreenUiEvent()
}