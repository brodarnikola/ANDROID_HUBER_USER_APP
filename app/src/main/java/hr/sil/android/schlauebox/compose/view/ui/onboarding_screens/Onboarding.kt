package hr.sil.android.schlauebox.compose.view.ui.onboarding_screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.SignUpOnboardingDestinations
import hr.sil.android.schlauebox.compose.view.ui.SignUpOnboardingSections
import hr.sil.android.schlauebox.compose.view.ui.theme.Black
import hr.sil.android.schlauebox.compose.view.ui.theme.Primary60
import hr.sil.android.schlauebox.util.SettingsHelper
import androidx.compose.material3.MaterialTheme as Material3


@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPager(
    modifier: Modifier,
    nextScreen: (route: String) -> Unit,
) {
    val pagerState = rememberPagerState()
    val slideStringTitle = remember { mutableStateOf("") }
    val slideStringDesc = remember { mutableStateOf("") }
    val slideImage = remember { mutableIntStateOf(-1) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(hr.sil.android.schlauebox.compose.view.ui.theme.White)
    ) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxSize()
                .background(hr.sil.android.schlauebox.compose.view.ui.theme.White)
        ) {

            val (mainContent, bottomButton) = createRefs()
            HorizontalPager(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .constrainAs(mainContent) {
                        top.linkTo(parent.top)
                        bottom.linkTo(bottomButton.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                        height = Dimension.fillToConstraints
                    },
                count = 4,
                state = pagerState
            ) { page ->
                when (page) {
                    0 -> slideStringTitle.value = stringResource(R.string.intro_welcome_slide_title)
                    1 -> slideStringTitle.value = stringResource(R.string.intro_pickup_slide_title)
                    2 -> slideStringTitle.value =
                        stringResource(R.string.intro_key_sharing_slide_title)

                    3 -> slideStringTitle.value =
                        stringResource(R.string.intro_welcome_slide_content)
                }

                when (page) {
                    0 -> slideStringDesc.value =
                        stringResource(R.string.intro_welcome_slide_content)

                    1 -> slideStringDesc.value = stringResource(R.string.intro_pickup_slide_content)
                    2 -> slideStringDesc.value =
                        stringResource(R.string.intro_key_sharing_slide_content)

                    3 -> slideStringDesc.value =
                        stringResource(R.string.intro_welcome_slide_content)
                }

                when (page) {
                    0 -> slideImage.intValue = R.drawable.img_onboarding_start
                    1 -> slideImage.intValue = R.drawable.img_onboarding_pickup
                    2 -> slideImage.intValue = R.drawable.img_onboarding_key
                    3 -> slideImage.intValue = R.drawable.img_onboarding_start
                }


                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {

                        Image(
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds,
                            painter = painterResource(id = slideImage.intValue),
                            contentDescription = null,
                        )
                        Image(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 240.dp),
                            painter = painterResource(id = R.drawable.schlauebox_logo_invert),
                            contentDescription = null,
                        )
                    }

                    if (page != 3) {
                        Spacer(modifier = Modifier.heightIn(min = 40.dp))
                        androidx.compose.material3.Text(
                            text = slideStringTitle.value,
                            color = Black, // Material3.colorScheme.onSurface, // onboarding screens - default color
                            style = Material3.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Spacer(modifier = Modifier.heightIn(min = 16.dp))
                        androidx.compose.material3.Text(
                            text = slideStringDesc.value,
                            style = Material3.typography.bodyLarge,
                            color = Black, // Material3.colorScheme.onSurfaceVariant, // onboarding screens - default color
                            textAlign = TextAlign.Start, // TextAlign.Start,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.heightIn(min = 40.dp))
                        FilledTonalButton(
                            onClick = {
                                SettingsHelper.firstRun = false
                                nextScreen(SignUpOnboardingSections.LOGIN_SCREEN.route)
                            },
                            modifier = Modifier
                                .heightIn(min = 20.dp)
                                .padding(horizontal = 50.dp)
                                .fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                containerColor = Primary60, // Material3.colorScheme.primary,
                                contentColor = Material3.colorScheme.onPrimary,
                                disabledContainerColor = Material3.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        ) {
                            androidx.compose.material3.Text(
                                text = "Go to next screen",
                                color = White,
                                style = Material3.typography.labelLarge,
                                modifier = Modifier
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            DotsIndicator(
                modifier = Modifier.constrainAs(bottomButton) {
                    top.linkTo(mainContent.bottom)
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 24.dp)
                    end.linkTo(parent.end, margin = 24.dp)
                    width = Dimension.fillToConstraints
                    //height = Dimension.wrapContent
                },
                totalDots = 4,
                selectedIndex = pagerState.currentPage,
                selectedColor = colorResource(id = R.color.colorPrimary),
                unSelectedColor = colorResource(id = R.color.colorGray)
            )

        }
    }
}

@Composable
fun DotsIndicator(
    modifier: Modifier,
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color,
    unSelectedColor: Color,
) {

    LazyRow(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
//        modifier = Modifier
//            .fillMaxWidth()
//            .wrapContentHeight()
    ) {
        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(unSelectedColor)
                )
            }

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
fun LoginScreen1(
    modifier: Modifier = Modifier,
    emailValue: String = "",
    passwordValue: String = "",
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onShowPasswordClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    isLoading: Boolean = false,
    passwordVisible: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // equivalent to ?actionBarSize
        ) {
            val (emailSection, passwordSection, forgotPassword, progressBar, loginButton) = createRefs()

            // Email Section
            ConstraintLayout(
                modifier = Modifier
                    .constrainAs(emailSection) {
                        top.linkTo(parent.top, margin = 16.dp)
                        start.linkTo(parent.start, margin = 20.dp)
                        end.linkTo(parent.end, margin = 20.dp)
                        width = Dimension.fillToConstraints
                    }
            ) {
                val (emailField, emailIcon) = createRefs()

                OutlinedTextField(
                    value = emailValue,
                    onValueChange = onEmailChange,
                    modifier = Modifier
                        .constrainAs(emailField) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        },
                    placeholder = { Text("Email") },
                    singleLine = true,
                    textStyle = Material3.typography.bodyLarge.copy(fontSize = 14.sp),
                    //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_register_email),
                    contentDescription = null,
                    modifier = Modifier
                        .constrainAs(emailIcon) {
                            top.linkTo(emailField.top)
                            bottom.linkTo(emailField.bottom, margin = 20.dp)
                            end.linkTo(emailField.end, margin = 5.dp)
                        }
                )
            }

            // Password Section
            Column(
                modifier = Modifier
                    .constrainAs(passwordSection) {
                        top.linkTo(emailSection.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(bottom = 5.dp)
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    val (passwordField, passwordIcon) = createRefs()

                    OutlinedTextField(
                        value = passwordValue,
                        onValueChange = onPasswordChange,
                        modifier = Modifier
                            .constrainAs(passwordField) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            },
                        placeholder = { Text("Password") },
                        singleLine = true,
                        textStyle = Material3.typography.bodyLarge.copy(fontSize = 14.sp),
//                        visualTransformation = if (passwordVisible)
//                            VisualTransformation.None
//                        else
//                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_password),
                        contentDescription = null,
                        modifier = Modifier
                            .constrainAs(passwordIcon) {
                                top.linkTo(passwordField.top)
                                bottom.linkTo(passwordField.bottom, margin = 20.dp)
                                end.linkTo(passwordField.end, margin = 5.dp)
                            }
                    )
                }

                Text(
                    text = "Show Password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)
                        .clickable { onShowPasswordClick() },
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    style = Material3.typography.bodyMedium
                )
            }

            // Forgot Password
            Text(
                text = "Forgot Password?",
                modifier = Modifier
                    .constrainAs(forgotPassword) {
                        top.linkTo(passwordSection.bottom, margin = 20.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth()
                    .clickable { onForgotPasswordClick() },
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                style = Material3.typography.bodyMedium
            )

            // Progress Bar
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .constrainAs(progressBar) {
                            top.linkTo(forgotPassword.bottom, margin = 10.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
            }

            // Login Button
            FilledTonalButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .width(220.dp)
                    .height(40.dp)
                    .constrainAs(loginButton) {
                        bottom.linkTo(parent.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
//                colors = ButtonDefaults.filledTonalButtonColors(
//                    containerColor = Primary60,
//                    contentColor = White
//                ),
                shape = RoundedCornerShape(20.dp) // equivalent to rounded_button drawable
            ) {
                Text(
                    text = "Sign In",
                    color = White,
                    fontSize = 16.sp,
                    style = Material3.typography.labelLarge
                )
            }
        }
    }
}

/*@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onShowPasswordClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(hr.sil.android.schlauebox.compose.view.ui.theme.White)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // ?actionBarSize equivalent
        ) {
            val (emailSection, passwordSection, forgotPassword, progressBar, loginButton) = createRefs()

            // === EMAIL FIELD ===
            Row(
                modifier = Modifier
                    .constrainAs(emailSection) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text(text = stringResource(id = R.string.app_generic_email)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp),
                    singleLine = true
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_register_email),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // === PASSWORD FIELD + SHOW PASSWORD ===
            Column(
                modifier = Modifier
                    .constrainAs(passwordSection) {
                        top.linkTo(emailSection.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text(text = stringResource(id = R.string.app_generic_password)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 5.dp),
                        singleLine = true,
                        //visualTransformation = PasswordVisualTransformation()
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_password),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = stringResource(id = R.string.intro_register_show_password),
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onShowPasswordClick() }
                )
            }

            // === FORGOT PASSWORD ===
            Text(
                text = stringResource(id = R.string.forgot_password_title),
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .constrainAs(forgotPassword) {
                        top.linkTo(passwordSection.bottom, margin = 20.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .clickable { onForgotPasswordClick() }
            )

            // === PROGRESS BAR ===
            CircularProgressIndicator(
                modifier = Modifier
                    .constrainAs(progressBar) {
                        top.linkTo(forgotPassword.bottom, margin = 10.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(40.dp)
                    .alpha(0f) // visibility="gone"
            )

            // === LOGIN BUTTON ===
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .constrainAs(loginButton) {
                        bottom.linkTo(parent.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .width(220.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    //containerColor = Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(id = R.string.app_generic_sign_in),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}*/


@Composable
fun FirstOnboardingScreen(
    modifier: Modifier = Modifier,
    nextScreen: (route: String) -> Unit = {},
    titleText: String,
    descriptionText: String,
    textAlign: TextAlign = TextAlign.Center,
    buttonText: String = "",
    pageNumber: String? = null,
    firstImage: Int,
    secondeImage: Int? = null,
    nextScreenRoute: String
) {

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(hr.sil.android.schlauebox.compose.view.ui.theme.White)
    ) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxSize()
                .background(hr.sil.android.schlauebox.compose.view.ui.theme.White)
        ) {
            val (mainContent, bottomButton) = createRefs()

            Column(
                Modifier
                    .constrainAs(mainContent) {
                        top.linkTo(parent.top)
                        bottom.linkTo(bottomButton.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                        height = Dimension.fillToConstraints
                    }
                    //.padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                        painter = painterResource(id = firstImage),
                        contentDescription = null,
//                    contentScale = ContentScale.FillBounds
                    )
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 240.dp),
                        painter = painterResource(id = R.drawable.schlauebox_logo_invert),
                        contentDescription = null,
//                    contentScale = ContentScale.FillBounds
                    )
                }
                Spacer(modifier = Modifier.heightIn(min = 40.dp))
                androidx.compose.material3.Text(
                    text = titleText,
                    color = Black, // Material3.colorScheme.onSurface, // onboarding screens - default color
                    style = Material3.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Spacer(modifier = Modifier.heightIn(min = 16.dp))
                androidx.compose.material3.Text(
                    text = descriptionText,
                    style = Material3.typography.bodyLarge,
                    color = Black, // Material3.colorScheme.onSurfaceVariant, // onboarding screens - default color
                    textAlign = textAlign, // TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .constrainAs(bottomButton) {
                        top.linkTo(mainContent.bottom)
                        bottom.linkTo(parent.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 24.dp)
                        end.linkTo(parent.end, margin = 24.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                    .semantics {
                        contentDescription = "onboardingButtonGetStarted"
                    },
            ) {

                if (pageNumber != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = pageNumber,
                            style = Material3.typography.labelLarge,
                            color = Black, // Material3.colorScheme.onPrimary, // onboarding screens - default color
                            textAlign = TextAlign.Start
                        )
                        androidx.compose.material3.Text(
                            text = " / 5",
                            style = Material3.typography.labelLarge,
                            color = Black, // onboarding screens - default color
                            textAlign = TextAlign.Start
                        )
                    }
                }
                Spacer(modifier = Modifier.heightIn(min = 10.dp))
                FilledTonalButton(
                    onClick = {
                        nextScreen(nextScreenRoute)
                    },
                    modifier = Modifier
                        .heightIn(min = 20.dp)
                        .fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = Primary60, // Material3.colorScheme.primary,
                        contentColor = Material3.colorScheme.onPrimary,
                        disabledContainerColor = Material3.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    androidx.compose.material3.Text(
                        text = buttonText,
                        color = White,
                        style = Material3.typography.labelLarge,
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun SecondOnboardingScreen(
    modifier: Modifier = Modifier,
    nextScreen: (route: String) -> Unit = {},
    titleText: String,
    descriptionText: String,
    textAlign: TextAlign = TextAlign.Center,
    buttonText: String = "",
    pageNumber: String? = null,
    firstImage: Int,
    secondeImage: Int? = null,
    nextScreenRoute: String
) {

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(hr.sil.android.schlauebox.compose.view.ui.theme.White)
    ) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxSize()
                .background(hr.sil.android.schlauebox.compose.view.ui.theme.White)
        ) {
            val (mainContent, bottomButton) = createRefs()

            Column(
                Modifier
                    .constrainAs(mainContent) {
                        top.linkTo(parent.top)
                        bottom.linkTo(bottomButton.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                        height = Dimension.fillToConstraints
                    }
                    //.padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                        painter = painterResource(id = firstImage),
                        contentDescription = null,
//                    contentScale = ContentScale.FillBounds
                    )
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 240.dp),
                        painter = painterResource(id = R.drawable.schlauebox_logo_invert),
                        contentDescription = null,
//                    contentScale = ContentScale.FillBounds
                    )
                }
                Spacer(modifier = Modifier.heightIn(min = 40.dp))
                androidx.compose.material3.Text(
                    text = titleText,
                    color = Black, // Material3.colorScheme.onSurface, // onboarding screens - default color
                    style = Material3.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Spacer(modifier = Modifier.heightIn(min = 16.dp))
                androidx.compose.material3.Text(
                    text = descriptionText,
                    style = Material3.typography.bodyLarge,
                    color = Black, // Material3.colorScheme.onSurfaceVariant, // onboarding screens - default color
                    textAlign = textAlign, // TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .constrainAs(bottomButton) {
                        top.linkTo(mainContent.bottom)
                        bottom.linkTo(parent.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 24.dp)
                        end.linkTo(parent.end, margin = 24.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                    .semantics {
                        contentDescription = "onboardingButtonGetStarted"
                    },
            ) {

                if (pageNumber != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = pageNumber,
                            style = Material3.typography.labelLarge,
                            color = Black, // Material3.colorScheme.onPrimary, // onboarding screens - default color
                            textAlign = TextAlign.Start
                        )
                        androidx.compose.material3.Text(
                            text = " / 5",
                            style = Material3.typography.labelLarge,
                            color = Black, // onboarding screens - default color
                            textAlign = TextAlign.Start
                        )
                    }
                }
                Spacer(modifier = Modifier.heightIn(min = 10.dp))
                FilledTonalButton(
                    onClick = {
                        nextScreen(nextScreenRoute)
                    },
                    modifier = Modifier
                        .heightIn(min = 20.dp)
                        .fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = Primary60, // Material3.colorScheme.primary,
                        contentColor = Material3.colorScheme.onPrimary,
                        disabledContainerColor = Material3.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    androidx.compose.material3.Text(
                        text = buttonText,
                        color = White,
                        style = Material3.typography.labelLarge,
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}
