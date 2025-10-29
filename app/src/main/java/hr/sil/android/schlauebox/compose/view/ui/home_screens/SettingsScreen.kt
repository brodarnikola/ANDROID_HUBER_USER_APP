package hr.sil.android.schlauebox.compose.view.ui.home_screens

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.main_activity.MainActivity
import hr.sil.android.schlauebox.core.remote.model.RLanguage
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.MainActivity1
import hr.sil.android.schlauebox.view.ui.dialog.LogoutDialog

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val appVersionValue = stringResource(R.string.nav_settings_app_version, stringResource(R.string.app_version))

    LaunchedEffect(Unit) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            //viewModel.setAppVersion("Version: ${packageInfo.versionName}")
            viewModel.setAppVersion(appVersionValue)
        } catch (e: PackageManager.NameNotFoundException) {
            viewModel.setAppVersion("Version: Unknown")
        }
    }

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorPinkishGray))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.nav_settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.colorBlack)
                )

                Image(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    modifier = Modifier.clickable { showLogoutDialog = true }
                )
            }

            Text(
                text = stringResource(R.string.nav_settings_user_details).uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.colorBlack),
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                letterSpacing = 0.17.sp
            )

            CustomTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                placeholder = stringResource(R.string.app_generic_name),
                icon = R.drawable.ic_register_name
            )

            Spacer(modifier = Modifier.height(5.dp))

            GroupNameSection(
                groupNameRow1 = uiState.groupNameRow1,
                groupNameRow2 = uiState.groupNameRow2,
                onRow1Changed = { viewModel.onGroupNameRow1Changed(it) },
                onRow2Changed = { viewModel.onGroupNameRow2Changed(it) }
            )

            Spacer(modifier = Modifier.height(5.dp))

            CustomTextField(
                value = uiState.email,
                onValueChange = {},
                placeholder = stringResource(R.string.app_generic_email),
                icon = R.drawable.ic_register_email,
                enabled = false
            )

            Spacer(modifier = Modifier.height(5.dp))

            CustomTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChanged(it) },
                placeholder = stringResource(R.string.app_generic_address),
                icon = R.drawable.ic_register_address
            )

            Spacer(modifier = Modifier.height(5.dp))

            CustomTextField(
                value = uiState.phone,
                onValueChange = { viewModel.onPhoneChanged(it) },
                placeholder = stringResource(R.string.app_generic_phone),
                icon = R.drawable.ic_register_phone
            )

            CheckboxRow(
                text = stringResource(R.string.register_reduced_mobility),
                checked = uiState.reducedMobility,
                onCheckedChange = { viewModel.onReducedMobilityChanged(it) }
            )

            Text(
                text = stringResource(R.string.app_generic_notifications).uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.colorBlack),
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                letterSpacing = 0.17.sp
            )

            CheckboxRow(
                text = stringResource(R.string.app_generic_push_notifications),
                checked = uiState.pushNotifications,
                onCheckedChange = { viewModel.onPushNotificationsChanged(it) }
            )

            CheckboxRow(
                text = stringResource(R.string.app_generic_email),
                checked = uiState.emailNotifications,
                onCheckedChange = { viewModel.onEmailNotificationsChanged(it) }
            )

            Text(
                text = stringResource(R.string.nav_settings_language).uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.colorBlack),
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                letterSpacing = 0.17.sp
            )

            LanguageDropdown(
                languages = uiState.availableLanguages,
                selectedLanguage = uiState.selectedLanguage,
                onLanguageSelected = { viewModel.onLanguageSelected(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.nav_settings_change_password).uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.colorBlack),
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                letterSpacing = 0.17.sp
            )

            PasswordTextField(
                value = uiState.oldPassword,
                onValueChange = { viewModel.onOldPasswordChanged(it) },
                placeholder = stringResource(R.string.nav_settings_old_password),
                errorMessage = uiState.oldPasswordError
            )

            Spacer(modifier = Modifier.height(5.dp))

            PasswordTextField(
                value = uiState.newPassword,
                onValueChange = { viewModel.onNewPasswordChanged(it) },
                placeholder = stringResource(R.string.nav_settings_new_password),
                errorMessage = uiState.newPasswordError
            )

            Spacer(modifier = Modifier.height(5.dp))

            PasswordTextField(
                value = uiState.retypePassword,
                onValueChange = { viewModel.onRetypePasswordChanged(it) },
                placeholder = stringResource(R.string.nav_settings_retype_new),
                errorMessage = uiState.retypePasswordError
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.saveSettings(
                        onSuccess = {
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            (context as? Activity)?.finish()
                            context.startActivity(intent)
                        },
                        onError = { error ->
                            errorMessage = error
                        }
                    )
                },
                enabled = uiState.isSaveEnabled && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorPrimary),
                    disabledContainerColor = colorResource(R.color.colorGray)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.app_generic_submit),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = uiState.appVersion,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.colorGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    UserUtil.logout()
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                    showLogoutDialog = false
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Error") },
                text = { Text(errorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.colorGray),
                    shape = RoundedCornerShape(4.dp)
                )
                .background(colorResource(R.color.colorWhite))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = colorResource(R.color.colorBlack)
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorResource(R.color.colorGray)
                            )
                        }
                        innerTextField()
                    }
                )

                Icon(
                    painter = painterResource(R.drawable.ic_password),
                    contentDescription = null,
                    tint = colorResource(R.color.colorGray)
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.colorDarkAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: Int,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = colorResource(R.color.colorGray),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                if (enabled) colorResource(R.color.colorWhite)
                else colorResource(R.color.colorPinkishGray)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = colorResource(R.color.colorBlack)
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.colorGray)
                        )
                    }
                    innerTextField()
                }
            )

            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colorResource(R.color.colorGray)
            )
        }
    }
}

@Composable
private fun GroupNameSection(
    groupNameRow1: String,
    groupNameRow2: String,
    onRow1Changed: (String) -> Unit,
    onRow2Changed: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = colorResource(R.color.colorGray),
                shape = RoundedCornerShape(4.dp)
            )
            .background(colorResource(R.color.colorWhite))
            .padding(4.dp)
    ) {
        Text(
            text = stringResource(R.string.app_generic_group),
            style = MaterialTheme.typography.labelSmall,
            color = colorResource(R.color.colorBlack),
            modifier = Modifier.padding(start = 5.dp, top = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(colorResource(R.color.colorGray))
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = groupNameRow1,
                        onValueChange = onRow1Changed,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = colorResource(R.color.colorBlack)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${groupNameRow1.length}/15",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(R.color.colorBlack)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = groupNameRow2,
                        onValueChange = onRow2Changed,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = colorResource(R.color.colorBlack)
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = groupNameRow1.length >= 15 || groupNameRow2.isNotEmpty()
                    )
                    Text(
                        text = "${groupNameRow2.length}/15",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(R.color.colorBlack)
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(R.color.colorWhite))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.colorBlack),
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = colorResource(R.color.colorPrimary),
                uncheckedColor = colorResource(R.color.colorGray)
            )
        )
    }

    Spacer(modifier = Modifier.height(3.dp))
}

@Composable
private fun LanguageDropdown(
    languages: List<RLanguage>,
    selectedLanguage: RLanguage?,
    onLanguageSelected: (RLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.colorGray),
                    shape = RoundedCornerShape(4.dp)
                )
                .background(colorResource(R.color.colorWhite))
                .padding(top = 1.dp, bottom = 15.dp)
        ) {
            Text(
                text = stringResource(R.string.nav_settings_language),
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.colorBlack),
                modifier = Modifier.padding(start = 15.dp, top = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 5.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedLanguage?.name ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.colorBlack)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = colorResource(R.color.colorBlack)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.Transparent)
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = language.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onLanguageSelected(language)
                            expanded = false
                        },
                        modifier = Modifier.background(Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.are_you_sure_logout),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
//            Text(
//                text = stringResource(R.string.logout_confirmation_message),
//                style = MaterialTheme.typography.bodyMedium
//            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.app_generic_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.app_generic_cancel))
            }
        }
    )
}