package hr.sil.android.schlauebox.compose.view.ui.access_sharing

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.view.ui.home.activities.AccessSharingAddUserActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessSharingScreen(
    macAddress: String,
    nameOfDevice: String,
    viewModel: AccessSharingViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddUser: (macAddress: String, nameOfDevice: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var memberToDelete by remember { mutableStateOf<RGroupDisplayMembersChild?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(macAddress) {
        viewModel.loadAccessSharing(macAddress, nameOfDevice)
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
        Text(
            text = stringResource(R.string.app_generic_access_sharing),
            color = colorResource(R.color.colorBlack),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.transparentColor))
                .wrapContentHeight()
                .padding(vertical = 10.dp)
                .background(colorResource(R.color.transparentColor))
        )
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(R.color.colorPrimary)
                )
            }

            uiState.showEmptyState -> {
                EmptyAccessState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                AccessSharingList(
                    members = uiState.members,
                    onDeleteClick = { member ->
                        memberToDelete = member
                        showDeleteDialog = true
                    }
                )
            }
        }

        if (uiState.canAddUsers) {
            FloatingActionButton(
                onClick = {
                    onNavigateToAddUser(macAddress, nameOfDevice)
                },
                containerColor = colorResource(R.color.colorPrimary),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 40.dp, bottom = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add User",
                    tint = colorResource(R.color.colorWhite)
                )
            }
        }

    }

    if (showDeleteDialog && memberToDelete != null) {
        DeleteConfirmationDialog(
            memberName = memberToDelete?.endUserName ?: "",
            onConfirm = {
                memberToDelete?.let { viewModel.deleteUserAccess(it) }
                showDeleteDialog = false
                memberToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                memberToDelete = null
            }
        )
    }
}

@Composable
private fun AccessSharingList(
    members: List<ItemRGroupInfo>,
    onDeleteClick: (RGroupDisplayMembersChild) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp),
        //contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(members) { item ->
            when (item) {
                is RGroupDisplayMembersHeader -> {
                    HeaderItem(headerTitle = item.groupOwnerName)
                }

                is RGroupDisplayMembersAdmin -> {
                    AdminItem(adminName = item.groupOwnerName)
                }

                is RGroupDisplayMembersChild -> {
                    MemberItem(
                        member = item,
                        onDeleteClick = { onDeleteClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderItem(
    headerTitle: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = headerTitle.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = colorResource(R.color.colorBlack),
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.transparentColor))
            .padding(start = 18.dp, top = 40.dp, bottom = 10.dp, end = 10.dp)
    )

    VerticalDivider(
        thickness = 5.dp,
        modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Blue, RectangleShape)
    )
}

@Composable
private fun AdminItem(
    adminName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = adminName,
        style = MaterialTheme.typography.titleSmall,
        color = colorResource(R.color.colorWhite),
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.help_item))
            .padding(start = 18.dp, top = 10.dp, bottom = 10.dp, end = 10.dp)
    )
    VerticalDivider(
        thickness = 25.dp
    )
}

@Composable
private fun MemberItem(
    member: RGroupDisplayMembersChild,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roleText = when (member.role) {
        "ADMIN" -> context.getString(R.string.access_sharing_admin_role_full)
        "USER" -> context.getString(R.string.access_sharing_user_role_full)
        else -> ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.help_item_transparent))
            .padding(start = 26.dp, top = 10.dp, bottom = 10.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = member.endUserName,
                style = MaterialTheme.typography.titleSmall,
                color = colorResource(R.color.colorWhite)
            )

            if (member.endUserEmail.isNotEmpty()) {
                Text(
                    text = member.endUserEmail,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.colorWhite),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (roleText.isNotEmpty()) {
                Text(
                    text = roleText,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.colorWhite),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = colorResource(R.color.colorWhite)
            )
        }
    }
}

@Composable
private fun EmptyAccessState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.key_sharing_content),
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center
            ),
            color = colorResource(R.color.colorBlack)
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    memberName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.app_generic_confirm),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = context.getString(R.string.access_sharing_warning_message, memberName)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.app_generic_confirm),
                    color = colorResource(R.color.colorPrimary)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(android.R.string.cancel),
                    color = colorResource(R.color.colorGray)
                )
            }
        }
    )
}