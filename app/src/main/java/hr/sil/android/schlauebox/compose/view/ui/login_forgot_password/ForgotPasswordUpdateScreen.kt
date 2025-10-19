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
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.signuponboarding_activity.SignUpOnboardingSections
import hr.sil.android.schlauebox.compose.view.ui.components.NewDesignButton
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTypography
import hr.sil.android.schlauebox.compose.view.ui.theme.DarkModeTransparent
import hr.sil.android.schlauebox.compose.view.ui.theme.White
import hr.sil.android.schlauebox.utils.UiEvent
import androidx.compose.material3.MaterialTheme as Material3

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordUpdateScreen(
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordUpdateViewModel,
    nextScreen: (route: String) -> Unit = {},
    navigateUp: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val activity = LocalContext.current as Activity

    // Properties
    val imageCheck = painterResource(id = R.drawable.ic_password)
    val imageInfo = painterResource(id = R.drawable.ic_password)

    var password = rememberSaveable {
        mutableStateOf("")
    }
    var repeatPassword = rememberSaveable {
        mutableStateOf("")
    }
    var pin = rememberSaveable {
        mutableStateOf("")
    }
    var isButtonEnabled = rememberSaveable {
        mutableStateOf(true)
    }

    val errorMessagePassword = rememberSaveable { mutableStateOf<String?>(null) }
    val errorMessageRepeatPassword = rememberSaveable { mutableStateOf<String?>(null) }
    val errorMessagePin = rememberSaveable { mutableStateOf<String?>(null) }

    val passwordLabelStyle = remember {
        mutableStateOf(AppTypography.labelLarge)
    }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    isButtonEnabled.value = true
                    Toast.makeText(context, event.message, event.toastLength).show()
                }

                ForgotPasswordUpdateUiEvent.NavigateBack -> {
                    navigateUp()
                }

                ForgotPasswordUpdateUiEvent.NavigateToNextScreen -> {
                    nextScreen(SignUpOnboardingSections.LOGIN_SCREEN.route)
                }
            }
        }
    }

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        val (mainContent, bottomButton) = createRefs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                //.background(Material3.colorScheme.background)
                .constrainAs(mainContent) {
                    top.linkTo(parent.top)
                    bottom.linkTo(bottomButton.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                    height = Dimension.fillToConstraints
                }
        ) {
            //endregion
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TopAppBar(
                    title = {
                        Text(
                            text = "Please enter your new password",
                            fontSize = 20.sp,
                            style = AppTypography.bodyLarge,
                            modifier = Modifier
                                .padding(horizontal = 20.dp),
                            color = colorResource(R.color.colorWhite),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = ""
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(R.color.colorPrimary),
                        titleContentColor = colorResource(R.color.colorWhite),
                        navigationIconContentColor = colorResource(R.color.colorBlack)
                    )
                )
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    text = stringResource(R.string.reset_password_description_title),
                    fontSize = 16.sp,
                    style = AppTypography.bodyLarge,
                    color = colorResource(R.color.colorBlack),
                )
                //endregion
                Spacer(modifier = Modifier.height(5.dp))
                //region EmailTextField

                CreateTextField(
                    valueState = password,
                    errorMessageState = errorMessagePassword,
                    labelText = stringResource(R.string.reset_password_description_title),
                    imageInfo = imageInfo,
                    imageCheck = imageCheck,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    onValidate = { input, ctx -> viewModel.getPasswordError(input, ctx) },
                    labelStyleState = passwordLabelStyle
                )

                Spacer(modifier = Modifier.height(10.dp))

                CreateTextField(
                    valueState = repeatPassword,
                    errorMessageState = errorMessageRepeatPassword,
                    labelText = "Repeat password",
                    imageInfo = imageInfo,
                    imageCheck = imageCheck,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    onValidate = { input, ctx -> viewModel.getRepeatPasswordError(password.value, input, ctx) },
                    labelStyleState = passwordLabelStyle
                )

                Spacer(modifier = Modifier.height(10.dp))

                CreateTextField(
                    valueState = pin,
                    errorMessageState = errorMessagePin,
                    labelText = "Enter pin",
                    imageInfo = imageInfo,
                    imageCheck = imageCheck,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    onValidate = { input, ctx -> viewModel.getPinError(input, ctx) },
                    labelStyleState = passwordLabelStyle
                )

                //endregion
                Spacer(modifier = Modifier.height(40.dp))

                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Material3.colorScheme.onSurfaceVariant,
                        strokeWidth = 3.dp
                    )
                }
                Spacer(modifier = Modifier.heightIn(min = 30.dp))

            }
        }

        //region SignInButton
        NewDesignButton(
            modifier = Modifier
                .constrainAs(bottomButton) {
                    top.linkTo(mainContent.bottom)
                    bottom.linkTo(parent.bottom, margin = 40.dp)
                    start.linkTo(parent.start, margin = 24.dp)
                    end.linkTo(parent.end, margin = 24.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .semantics {
                    contentDescription = "signInButtonLoginScreen"
                },
            title = stringResource(R.string.forgot_password_send),
            onClick = {
                val emailValidation = viewModel.getPasswordError(password.value, context)
                val repeatPassswordError = viewModel.getRepeatPasswordError(password.value, repeatPassword.value, context)
                val pinError = viewModel.getPinError(pin.value, context)


                if (emailValidation.isBlank() && repeatPassswordError.isBlank() && pinError.isBlank() ) {
                    isButtonEnabled.value = false
                    viewModel.onEvent(ForgotPasswordUpdateEvent.OnForgotPasswordUpdateRequest(password.value, pin.value, context, activity))
                }
            },
            enabled = isButtonEnabled.value,
        )
    }
}

@Composable
fun CreateTextField(
    valueState: MutableState<String>,
    errorMessageState: MutableState<String?>,
    labelText: String,
    imageInfo: Painter,
    imageCheck: Painter,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onValidate: (String, Context) -> String,
    labelStyleState: MutableState<TextStyle>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    TextField(
        value = valueState.value,
        placeholder = {
            Text(
                text = labelText,
                color = Material3.colorScheme.onSurfaceVariant,
                style = labelStyleState.value
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Material3.colorScheme.onSurface,
            focusedBorderColor = colorResource(R.color.colorPrimary),
            unfocusedBorderColor = Material3.colorScheme.outline,
            cursorColor = colorResource(R.color.colorPrimary),
            backgroundColor = DarkModeTransparent
        ),
        onValueChange = {
            valueState.value = it
            val validationMessage = onValidate(it, context)
            errorMessageState.value = if (validationMessage.isNotEmpty()) validationMessage else ""
        },
        modifier = modifier
            .semantics { contentDescription = "textField_$labelText" }
            .onFocusChanged {
                if (it.isFocused) {
                    labelStyleState.value = AppTypography.bodySmall
                } else {
                    labelStyleState.value = AppTypography.bodyLarge
                }
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        trailingIcon = {
            when {
                !errorMessageState.value.isNullOrBlank() -> {
                    Icon(
                        painter = imageInfo,
                        contentDescription = null,
                        tint = Material3.colorScheme.error,
                        modifier = Modifier.width(25.dp)
                    )
                }

                valueState.value.contains("@") -> {
                    Icon(
                        painter = imageCheck,
                        contentDescription = null,
                        modifier = Modifier.width(25.dp)
                    )
                }

                else -> {
                    Icon(
                        painter = imageInfo,
                        contentDescription = null,
                        modifier = Modifier.width(25.dp)
                    )
                }
            }
        }
    )
}
