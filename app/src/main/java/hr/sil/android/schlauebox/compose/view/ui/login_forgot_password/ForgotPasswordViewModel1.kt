///*
// * Copyright © 2022 Sunbird. All rights reserved.
// *
// * Sunbird Secure Messaging
// *
// * Created by Cinnamon.
// */
//package com.sunbird.ui.setup.login
//
////import hr.sil.android.schlauebox.cache.DataCache
////import hr.sil.android.schlauebox.cache.status.InstallationKeyHandler
//import android.app.Activity
//import android.content.Context
//import android.widget.Toast
//import androidx.lifecycle.viewModelScope
//import hr.sil.android.schlauebox.R
//import hr.sil.android.schlauebox.compose.view.ui.signuponboarding_activity.SignUpOnboardingSections
//import hr.sil.android.schlauebox.core.util.logger
//import hr.sil.android.schlauebox.util.backend.UserUtil
//import hr.sil.android.schlauebox.utils.SecondBaseViewModel
//import hr.sil.android.schlauebox.utils.UiEvent
//import hr.sil.android.schlauebox.utils.UiEvent.*
//import hr.sil.android.schlauebox.utils.isEmailValid
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//
//class ForgotPasswordViewModel1()  : SecondBaseViewModel<ForgotPasswordContract.ForgotPasswordUiState1, ForgotPasswordContract.ForgotPasswordEvent1>() {
//
//    val log = logger()
//
//    var effects = Channel<ForgotPasswordContract.Effect>(UNLIMITED)
//        private set
//
//    override fun initialState(): ForgotPasswordContract.ForgotPasswordUiState1 {
//        return ForgotPasswordContract.ForgotPasswordUiState1()
//    }
//
//    override fun onEvent(event: ForgotPasswordContract.ForgotPasswordEvent1) {
//        when(event) {
//            is ForgotPasswordContract.ForgotPasswordEvent1.OnForgotPasswordRequest -> {
//                viewModelScope.launch {
//                    _state.value { it.copy(loading = true) }
//                    val response = UserUtil.passwordRecovery(
//                        event.email
//                    )
//                    _state.value { it.copy(loading = false) }
//
//                    log.info("Response code: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")
//
//                    if (response.isSuccessful) {
//                        log.info("Response code 22: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")
//
//                        effects.send(ForgotPasswordContract.Effect.DataWasLoaded(message = "Success.. Displayed github repositories"))
//                        //sendUiEvent(ForgotPasswordUiEvent.NavigateToNextScreen)
//                        sendUiEvent(Navigate( SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route))
////                        val startIntent = Intent(event.context, MainActivity::class.java)
////                        event.context.startActivity(startIntent)
////                        event.activity.finish()
//                    } else {
//                        sendUiEvent(
//                            ShowToast(
//                                "Email doesn't exist in the system",
//                                Toast.LENGTH_SHORT
//                            )
//                        )
//                    }
//
//                }
//            }
//        }
//    }
//
//
////    override fun onEvent(event: ForgotPasswordEvent) {
////        when (event) {
////            is ForgotPasswordEvent.OnForgotPasswordRequest -> {
////
////                viewModelScope.launch {
////                    _state.update { it.copy(loading = true) }
////                    val response = UserUtil.passwordRecovery(
////                        event.email
////                    )
////                    _state.update { it.copy(loading = false) }
////
////                    log.info("Response code: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")
////
////                    if (response.isSuccessful) {
////                        log.info("Response code 22: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")
////                        //sendUiEvent(ForgotPasswordUiEvent.NavigateToNextScreen)
////                        sendUiEvent(Navigate( SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route))
//////                        val startIntent = Intent(event.context, MainActivity::class.java)
//////                        event.context.startActivity(startIntent)
//////                        event.activity.finish()
////                    } else {
////                        sendUiEvent(
////                            ShowToast(
////                                "Email doesn't exist in the system",
////                                Toast.LENGTH_SHORT
////                            )
////                        )
////                    }
////
////                }
////
//////                viewModelScope.launch {
//////                    _state.update { it.copy(loading = true) }
//////                    login(email = event.email, password = event.password, context = event.context)
//////                    _state.update { it.copy(loading = false) }
//////                }
////            }
////
////        }
////    }
//
//    fun getEmailError(email: String, context: Context): String {
//        var emailError = ""
//        if (email.isBlank()) {
//            emailError = context.getString(R.string.forgot_password_error)
//        } else if (!email.isEmailValid()) {
//            emailError = context.getString(R.string.pickup_parcel_email_error)
//        }
//
//        return emailError
//    }
//
//}
//
//class ForgotPasswordContract {
//
//    data class ForgotPasswordUiState1(
//        val loading: Boolean = false
//    )
//
//    sealed class ForgotPasswordEvent1 {
//        data class OnForgotPasswordRequest(
//            val email: String,
//            val context: Context,
//            val activity: Activity
//        ) : ForgotPasswordEvent1()
//    }
//
//    sealed class ForgotPasswordUiEvent1() : UiEvent {
//        object NavigateToNextScreen : ForgotPasswordUiEvent1()
//
//        object NavigateBack : ForgotPasswordUiEvent1()
//    }
//
//}