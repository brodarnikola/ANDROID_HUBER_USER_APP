package hr.sil.android.schlauebox.compose.view.ui.access_sharing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RGroupInfo
import hr.sil.android.schlauebox.core.remote.model.RUserAccessRole
import hr.sil.android.schlauebox.view.ui.home.activities.AccessSharingActivity

@SuppressLint("Range")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AccessSharingAddUserScreen(
    macAddress: String,
    nameOfDevice: String,
    viewModel: AccessSharingAddUserViewModel = viewModel(),
    navigateToAccessSharingActivity: ( macAddress: String, nameOfDevice: String) -> Unit = { _, _ -> },

    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    val emailPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                    )
                    context.contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                            if (emailIndex >= 0) {
                                val email = cursor.getString(emailIndex)
                                if (!email.isNullOrBlank()) {
                                    viewModel.onEmailFromContact(email)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(macAddress) {
        viewModel.loadGroupMemberships(macAddress)
    }

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onNavigateToLogin()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_three),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        HorizontalDivider(
            color = colorResource(R.color.colorPinkishGray),
            thickness = 1.dp
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                //.background(colorResource(R.color.colorPinkishGray))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.access_sharing_new_user_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(R.color.colorBlack),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    EmailTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        placeholder = stringResource(R.string.app_generic_email),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                if (contactsPermission.status.isGranted) {
                                    val intent = Intent(Intent.ACTION_PICK).apply {
                                        type = ContactsContract.CommonDataKinds.Email.CONTENT_TYPE
                                    }
                                    emailPickerLauncher.launch(intent)
                                    //emailPickerLauncher.launch(null)
                                } else {
                                    contactsPermission.launchPermissionRequest()
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_adressbook),
                            contentDescription = "Address Book",
                            tint = colorResource(R.color.colorBlack)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.access_sharing_new_user_selection),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.colorBlack)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GroupSelectionDropdown(
                        label = stringResource(R.string.access_sharing_group_selection),
                        groups = uiState.availableGroups,
                        selectedGroup = uiState.selectedGroup,
                        onGroupSelected = { viewModel.onGroupSelected(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RoleSelectionDropdown(
                        label = stringResource(R.string.access_sharing_new_user_access_details),
                        selectedRole = uiState.selectedRole,
                        onRoleSelected = { viewModel.onRoleSelected(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.addUserAccess(macAddress) {
                                navigateToAccessSharingActivity(macAddress, nameOfDevice)
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .width(220.dp)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.colorPrimary)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.access_sharing_new_user_allow),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showShareAppDialog) {
            ShareAppDialog(
                email = uiState.shareAppEmail,
                onDismiss = { viewModel.dismissShareAppDialog() }
            )
        }
    }
}

@Composable
private fun GroupSelectionDropdown(
    label: String,
    groups: List<RGroupInfo>,
    selectedGroup: RGroupInfo?,
    onGroupSelected: (RGroupInfo) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(colorResource(R.color.colorPrimary))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(colorResource(R.color.colorWhite))
                    .padding(4.dp)
                    .clickable { expanded = !expanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedGroup?.groupOwnerName ?: label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(
                            if (selectedGroup != null) R.color.colorBlack else R.color.colorGray
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = colorResource(R.color.colorBlack)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.Transparent)
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = group.groupOwnerName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onGroupSelected(group)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(3.dp))
}

@Composable
private fun RoleSelectionDropdown(
    label: String,
    selectedRole: RUserAccessRole,
    onRoleSelected: (RUserAccessRole) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val roles = listOf(
        RUserAccessRole.ADMIN to context.getString(R.string.access_sharing_admin_role_full),
        RUserAccessRole.USER to context.getString(R.string.access_sharing_user_role_full)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(colorResource(R.color.colorPrimary))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(colorResource(R.color.colorWhite))
                    .padding(4.dp)
                    .clickable { expanded = !expanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = roles.find { it.first == selectedRole }?.second ?: label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.colorBlack)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = colorResource(R.color.colorBlack)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.Transparent)
                ) {
                    roles.forEach { (role, roleName) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = roleName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onRoleSelected(role)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(3.dp))
}

@Composable
private fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(colorResource(R.color.colorPrimary))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(colorResource(R.color.colorWhite))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 5.dp, top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = colorResource(R.color.colorBlack)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 10.dp, bottom = 12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_register_email),
                        contentDescription = null,
                        tint = colorResource(R.color.colorGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareAppDialog(
    email: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.grant_access_error),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "User not registered",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val appLink = BuildConfig.APP_ANDR_DOWNLOAD_URL
                    val iOSLink = BuildConfig.APP_IOS_DOWNLOAD_URL
                    val shareBodyText = context.getString(
                        R.string.access_sharing_share_app_text,
                        appLink,
                        iOSLink
                    )

                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        putExtra(Intent.EXTRA_SUBJECT, "SchlauBox App")
                        putExtra(Intent.EXTRA_TEXT, shareBodyText)
                    }

                    context.startActivity(
                        Intent.createChooser(
                            emailIntent,
                            context.getString(R.string.access_sharing_share_choose_sharing)
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Share app")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.app_generic_cancel))
            }
        }
    )
}