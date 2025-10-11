/*
 * Copyright © 2022 Sunbird. All rights reserved.
 *
 * Sunbird Secure Messaging
 *
 * Created by Cinnamon.
 */
package com.sunbird.ui.setup.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme as Material3
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hr.sil.android.schlauebox.compose.view.ui.theme.IsAppInDarkTheme

import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.SignUpOnboardingSections
import hr.sil.android.schlauebox.compose.view.ui.components.AppDialog
import hr.sil.android.schlauebox.compose.view.ui.components.NewDesignButton
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTypography
import hr.sil.android.schlauebox.compose.view.ui.theme.DarkModeTransparent
import hr.sil.android.schlauebox.utils.UiEvent
import hr.sil.android.schlauebox.view.ui.MainActivity
import hr.sil.android.schlauebox.view.ui.intro.TCInvitedUserActivity

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    nextScreen: (route: String) -> Unit = {},
    navigateUp: (route: String) -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val activity = LocalContext.current as Activity

    // Properties
    val imageLogo = painterResource(id = R.drawable.img_onboarding_pickup)
    val imagePerson = painterResource(id = R.drawable.img_onboarding_key)
    val imageCheck = painterResource(id = R.drawable.ic_register_email)
    val imageInfo = painterResource(id = R.drawable.ic_register_email)
    val imageLock = painterResource(id = R.drawable.img_onboarding_pickup)
    val imageVisibilityOn = painterResource(id = R.drawable.ic_password)
    val imageVisibilityOff = painterResource(id = R.drawable.ic_password)

    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val isValid by remember {
        mutableStateOf(false)
    }
    var errorMessageEmail by remember {
        mutableStateOf<String?>(null)
    }
    var errorMessagePassword by remember {
        mutableStateOf<String?>(null)
    }

    val emailLabelStyle = remember {
        mutableStateOf(AppTypography.labelLarge)
    }

    val passwordLabelStyle = remember {
        mutableStateOf(AppTypography.bodyLarge)
    }

    // used to prepopulate email address if user registered on Register screen
    LaunchedEffect(key1 = Unit) {
//        val userEmail = viewModel.getUserEmail()
//        if (userEmail.isNotEmpty()) {
//            email = userEmail
//            errorMessageEmail = ""
//        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, event.toastLength).show()
                }

                is LoginScreenUiEvent.NavigateToTCInvitedUserActivityScreen -> {
                    val startIntent = Intent(context, TCInvitedUserActivity::class.java)
                    startIntent.putExtra("email", email)
                    startIntent.putExtra("password", password)
                    startIntent.putExtra("goToMainActivity", true)
                    context.startActivity(startIntent)
                    activity.finish()
                }

                is LoginScreenUiEvent.NavigateToMainActivityScreen -> {
                    val startIntent = Intent(context, MainActivity::class.java)
                    context.startActivity(startIntent)
                    activity.finish()
                }

                is LoginScreenUiEvent.NavigateBack -> {
                    navigateUp(SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route)
                }

                is LoginScreenUiEvent.NavigateToForgotPasswordScreen -> {
                    navigateUp(SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route)
                }
            }
        }
    }


//    SunbirdSurface(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3.colorScheme.background)
    ) {
        //region StickyHeaderView
//            HeaderNavigationBar(
//                //onBackBtnClick = { navigateUp(SignUpOnboardingSections.REGISTRATION.route) }
//            )
        //endregion
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //region HeaderView
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
            ) {
                Image(
                    painter = imageLogo,
                    contentDescription = null,
                    modifier = Modifier
                        .width(90.dp)
                        .padding(top = 32.dp)
                        .semantics { contentDescription = "appLogo" }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.login_title),
                    style = AppTypography.headlineMedium,
                    color = Material3.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .semantics { contentDescription = "registrationTitle" }
                )
            }
            //endregion
            Spacer(modifier = Modifier.height(76.dp))
            //region EmailTextField
            TextField(
                value = email,
                placeholder = {
                    Text(
                        text = stringResource(R.string.app_generic_email),
                        color = Material3.colorScheme.onSurfaceVariant,
                        style = passwordLabelStyle.value
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Material3.colorScheme.onSurface,
                    focusedBorderColor = Material3.colorScheme.primary,
                    unfocusedBorderColor = Material3.colorScheme.outline,
                    cursorColor = Material3.colorScheme.primary,
                    backgroundColor = DarkModeTransparent
                ),
                onValueChange = {
                    email = it
                    val checkErrorMessageEmail = viewModel.getEmailError(it, context)
                    errorMessageEmail =
                        if (checkErrorMessageEmail !== "") checkErrorMessageEmail else ""
                },
                modifier = Modifier
                    .semantics {
                        contentDescription = "emailTextFieldLoginScreen"
                    }
                    .onFocusChanged {
                        if (it.isFocused) {
                            emailLabelStyle.value = AppTypography.bodySmall
                        } else {
                            emailLabelStyle.value = AppTypography.bodyLarge
                        }
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

                maxLines = 1,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    if (errorMessageEmail != null && errorMessageEmail !== "") {
                        Icon(
                            painter = imageInfo,
                            contentDescription = null,
                            tint = Material3.colorScheme.error,
                            modifier = Modifier
                                .width(25.dp)
                                .semantics {
                                    contentDescription = "loginExclamationMark"
                                }
                        )
                    } else if (errorMessageEmail != null && email.contains("@")) {
                        Icon(
                            painter = imageCheck,
                            contentDescription = null,
                            modifier = Modifier
                                .width(25.dp)
                                .semantics { contentDescription = "loginCheckMark" }
                        )
                    }
                    else {
                        Icon(
                            painter = imageInfo,
                            contentDescription = null,
                            modifier = Modifier
                                .width(25.dp)
                                .semantics {
                                    contentDescription = "loginExclamationMark"
                                }
                        )
                    }
                }
            )
//            OutlinedTextField(
//                value = email,
//                onValueChange = {
//                    email = it
//                    val checkErrorMessageEmail = viewModel.getEmailError(it, context)
//                    errorMessageEmail =
//                        if (checkErrorMessageEmail !== "") checkErrorMessageEmail else ""
//                },
//                label = {
//                    Text(
//                        text = stringResource(R.string.login_title),
//                        color = Material3.colorScheme.onSurfaceVariant,
//                        style = emailLabelStyle.value
//                    )
//                },
//                modifier = Modifier
//                    .semantics {
//                        contentDescription = "emailTextFieldLoginScreen"
//                    }
//                    .onFocusChanged {
//                        if (it.isFocused) {
//                            emailLabelStyle.value = AppTypography.bodySmall
//                        } else {
//                            emailLabelStyle.value = AppTypography.bodyLarge
//                        }
//                    }
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                shape = RoundedCornerShape(50.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    textColor = Material3.colorScheme.onSurface,
//                    focusedBorderColor =
//                        if (errorMessageEmail != null && errorMessageEmail !== "") Material3.colorScheme.error
//                        else Material3.colorScheme.primary,
//                    unfocusedBorderColor = Material3.colorScheme.outline,
//                    cursorColor = Material3.colorScheme.primary
//                ),
//                maxLines = 1,
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Email,
//                    imeAction = ImeAction.Next
//                ),
//                leadingIcon = {
//                    Icon(
//                        painter = imagePerson,
//                        tint = Material3.colorScheme.onSurfaceVariant,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .width(25.dp)
//                    )
//                },
//                trailingIcon = {
//                    if (errorMessageEmail != null && errorMessageEmail !== "") {
//                        Icon(
//                            painter = imageInfo,
//                            contentDescription = null,
//                            tint = Material3.colorScheme.error,
//                            modifier = Modifier
//                                .width(25.dp)
//                                .semantics {
//                                    contentDescription = "loginExclamationMark"
//                                }
//                        )
//                    } else if (errorMessageEmail != null && email.contains("@")) {
//                        Icon(
//                            painter = imageCheck,
//                            contentDescription = null,
//                            tint = Material3.colorScheme.secondary,
//                            modifier = Modifier
//                                .width(25.dp)
//                                .semantics { contentDescription = "loginCheckMark" }
//                        )
//                    }
//                },
//            )
            //endregion
            Spacer(modifier = Modifier.height(40.dp))
            //region PasswordTextField
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    val checkErrorMessage = viewModel.getPasswordError(it, context)
                    errorMessagePassword = if (checkErrorMessage !== "") {
                        checkErrorMessage
                    } else {
                        ""
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.login_title),
                        color = Material3.colorScheme.onSurfaceVariant,
                        style = passwordLabelStyle.value
                    )
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier
                    .semantics {
                        contentDescription = "passwordTextFieldLoginScreen"
                    }
                    .onFocusChanged {
                        if (it.isFocused) {
                            passwordLabelStyle.value = AppTypography.bodySmall
                        } else {
                            passwordLabelStyle.value = AppTypography.bodyLarge
                        }
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(50.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Material3.colorScheme.onSurface,
                    focusedBorderColor = Material3.colorScheme.primary,
                    unfocusedBorderColor = Material3.colorScheme.outline,
                    cursorColor = Material3.colorScheme.primary
                ),
                trailingIcon = {
                    val visibilityImage = if (passwordVisible)
                        imageVisibilityOn else imageVisibilityOff
                    IconButton(onClick = {
                        passwordVisible = !passwordVisible
                    }) {
                        Icon(
                            painter = visibilityImage,
                            tint = Material3.colorScheme.onSurfaceVariant,
                            contentDescription = null,
                            modifier = Modifier.width(25.dp)
                        )
                    }
                },
            )
            //endregion
            Spacer(modifier = Modifier.height(34.dp))
            //region ForgotPasswordButton
            TextButton(
                modifier = Modifier
                    .semantics {
                        contentDescription = "forgotPasswordButtonLoginScreen"
                    },
                onClick = {
                    viewModel.onEvent(LoginScreenEvent.OnForgottenPassword)
//                        nextScreen(SignUpOnboardingSections.FORGOT_PASSWORD.route)
                }
            ) {
                Text(
                    text = stringResource(R.string.forgot_password_title),
                    style = AppTypography.labelLarge,
                    color = Material3.colorScheme.primary
                )
            }
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Material3.colorScheme.onSurfaceVariant,
                    strokeWidth = 3.dp
                )
                //AppDialog(onDismiss = {})
            }
            //endregion
            Spacer(modifier = Modifier.heightIn(min = 30.dp))
            //region SignInButton
            NewDesignButton(
                modifier = Modifier.semantics {
                    contentDescription = "signInButtonLoginScreen"
                },
                title = stringResource(R.string.login_title),
                onClick = {
                    val emailValidation = viewModel.getEmailError(email, context)
                    val passwordValidation = viewModel.getPasswordError(password, context)

                    if (emailValidation.isNotBlank() || passwordValidation.isNotBlank()) {
                        errorMessageEmail = emailValidation.ifBlank { "" }
                        errorMessagePassword = passwordValidation.ifBlank { "" }
                    } else {
                        viewModel.onEvent(
                            LoginScreenEvent.OnLogin(
                                email = email,
                                password = password,
                                context = context
                            )
                        )
                    }
                },
                enabled = errorMessageEmail == "" && errorMessagePassword == "",
            )
            //endregion
        }
    }
//    }

//    when (val res = viewModel.state.value) {
//        is Resource.Initial -> LoginScreen(viewModel, modifier, nextScreen, navigateUp)
//        is Resource.Loading -> {
//            LoginScreen(viewModel, modifier, nextScreen, navigateUp)
//            DialogOnlyWithSpinnerInCenter()
//        }
//
//        is Resource.Success -> {
//            nextScreen(SignUpOnboardingSections.NOTIFICATIONS_PERMISSION_LOGIN.route)
//        }
//
//        is Resource.FirebaseFailureException -> {
//            Timber.d(" LOGIN EXCEPTION: ${res.error}, error: ${res.error}")
//            if (res.error == "Register") {
//                navigateUp(SignUpOnboardingSections.CONNECT_I_MESSAGES.route)
//            } else {
//                val sematincsTagError = when {
//                    res.error?.contains("The password is invalid or the") == true -> "InvalidPasswordErrorMsg"
//                    else -> "UserIDErrorMsg"
//                }
//                LoginScreen(viewModel, modifier, nextScreen, navigateUp)
//                DisplaySnackBar(uiResource = viewModel.state.value, snackBarMessage = res.error, sematincsTagError = sematincsTagError)
//            }
//        }
//
//        else -> {}
//    }
}
