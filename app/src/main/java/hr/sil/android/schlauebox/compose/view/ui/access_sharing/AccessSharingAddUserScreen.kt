package hr.sil.android.schlauebox.compose.view.ui.access_sharing

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RGroupInfo
import hr.sil.android.schlauebox.core.remote.model.RUserAccessRole
import hr.sil.android.schlauebox.view.ui.home.activities.AccessSharingActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AccessSharingAddUserScreen(
    macAddress: String,
    nameOfDevice: String,
    viewModel: AccessSharingAddUserViewModel = viewModel(),
    navigateUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
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
        Column(modifier = Modifier.fillMaxSize()) {

            HorizontalDivider(
                color = colorResource(R.color.colorPinkishGray),
                thickness = 1.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.access_sharing_new_user_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.colorBlack),
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = { Text(stringResource(R.string.app_generic_email)) },
                    placeholder = { Text(stringResource(R.string.app_generic_email)) },
                    isError = uiState.emailError != null,
                    supportingText = {
                        uiState.emailError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_register_email),
                            contentDescription = "Email",
                            tint = colorResource(R.color.colorPrimary)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorPrimary),
                        unfocusedBorderColor = colorResource(R.color.colorGray)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (contactsPermission.status.isGranted) {
                                contactPickerLauncher.launch(null)
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
                        tint = colorResource(R.color.colorPrimary)
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
                            val startIntent = Intent(context, AccessSharingActivity::class.java)
                            startIntent.putExtra("rMacAddress", macAddress)
                            startIntent.putExtra("nameOfDevice", nameOfDevice)
                            context.startActivity(startIntent)
                            (context as? Activity)?.finish()
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorPrimary)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.access_sharing_new_user_allow),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupSelectionDropdown(
    label: String,
    groups: List<RGroupInfo>,
    selectedGroup: RGroupInfo?,
    onGroupSelected: (RGroupInfo) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.colorBlack),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedGroup?.groupOwnerName ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.colorPrimary),
                    unfocusedBorderColor = colorResource(R.color.colorGray)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.groupOwnerName) },
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.colorBlack),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = roles.find { it.first == selectedRole }?.second ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.colorPrimary),
                    unfocusedBorderColor = colorResource(R.color.colorGray)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.forEach { (role, roleName) ->
                    DropdownMenuItem(
                        text = { Text(roleName) },
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
                        putExtra(Intent.EXTRA_SUBJECT, "SchlaueBox App")
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
                Text("Share application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.app_generic_cancel))
            }
        }
    )
}