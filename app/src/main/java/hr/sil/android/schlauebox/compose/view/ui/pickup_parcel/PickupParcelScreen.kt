package hr.sil.android.schlauebox.compose.view.ui.pickup_parcel

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RCreatedLockerKey
import hr.sil.android.schlauebox.core.remote.model.RLockerKeyPurpose
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.formatFromStringToDate
import hr.sil.android.schlauebox.core.util.formatToViewDateTimeDefaults
import hr.sil.android.schlauebox.store.MPLDeviceStore
import java.text.ParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupParcelScreen(
    macAddress: String,
    viewModel: PickupParcelViewModel = viewModel(),
    navigateUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onFinish: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var cleaningChecked by remember { mutableStateOf(false) }

    LaunchedEffect(macAddress) {
        viewModel.loadPickupParcel(context, macAddress)
    }

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onNavigateToLogin()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onPause()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            //.padding(paddingValues)
            //.background(R.drawable.bg_three)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_three),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = colorResource(R.color.colorPinkishGray)
            )

            Text(
                text = stringResource(R.string.pickup_parcel_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.colorBlack),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp, start = 15.dp, end = 15.dp)
            )

            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { viewModel.openParcel(context) },
                    enabled = !uiState.isConnecting && !uiState.isUnlocked,
                    modifier = Modifier.fillMaxSize()
                ) {
//                    val lockIcon = when {
//                        uiState.isUnlocked -> R.drawable.ic_unlocked
//                        uiState.isConnecting -> R.drawable.ic_locked_disabled
//                        else -> R.drawable.ic_locked
//                    }
                    val lockIcon = when {
                        uiState.imageIcon == 0 -> R.drawable.ic_locked
                        uiState.imageIcon == 1 -> R.drawable.ic_unlocked
                        else -> R.drawable.ic_locked_disabled
                    }
                    Image(
                        painter = painterResource(id = lockIcon),
                        contentDescription = "Lock Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (uiState.isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)
                            .offset(y = 30.dp),
                        color = colorResource(R.color.colorWhite)
                    )
                }
            }

//            Text(
//                text = uiState.statusText,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = colorResource(R.color.colorWhite),
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(top = 10.dp, start = 15.dp, end = 15.dp)
//            )

            Text(
                text = uiState.statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.colorWhite),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp, start = 16.dp, end = 16.dp)
            )

            if (uiState.showForceOpenButton) {
                Button(
                    onClick = { viewModel.forceOpen(context) },
                    enabled = !uiState.isForceOpening,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorPrimary)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .width(200.dp)
                        .height(35.dp)
                ) {
                    if (uiState.isForceOpening) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colorResource(R.color.colorWhite)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.app_generic_force_open),
                            color = colorResource(R.color.colorWhite)
                        )
                    }
                }
            }

            if (uiState.showFinishButton) {
                Button(
                    onClick = { onFinish() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorPrimary)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 20.dp)
                        .width(200.dp)
                        .height(35.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_generic_confirm),
                        color = colorResource(R.color.colorWhite)
                    )
                }
            }

            if (uiState.showCleaningCheckbox) {
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp, start = 20.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_needs_cleaning),
                        contentDescription = "Cleaning Icon"
                    )

                    Checkbox(
                        checked = cleaningChecked,
                        onCheckedChange = {
                            cleaningChecked = it
                            if (it) {
                                viewModel.setLockerCleaning(context)
                            }
                        },
                        enabled = uiState.cleaningCheckboxEnabled && !uiState.isCleaningLocker,
                        modifier = Modifier.padding(start = 15.dp)
                    )

                    Text(
                        text = stringResource(R.string.locker_needs_cleaning).uppercase(),
                        color = colorResource(R.color.colorWhite),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                if (uiState.isCleaningLocker) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(40.dp),
                        color = colorResource(R.color.colorWhite)
                    )
                }
            }

            KeysList(
                keys = uiState.keys,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun KeysList(
    keys: List<RCreatedLockerKey>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val device = MPLDeviceStore.devices.values.firstOrNull()
    val deviceType = device?.masterUnitType ?: RMasterUnitType.MPL

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(keys) { key ->
            KeyItem(key = key, deviceType = deviceType)
        }
    }
}

@Composable
private fun KeyItem(
    key: RCreatedLockerKey,
    deviceType: RMasterUnitType,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyText = getKeyText(context, key, deviceType)
    val showShare = key.purpose == RLockerKeyPurpose.DELIVERY
    val showDelete = key.purpose == RLockerKeyPurpose.PAF && key.createdForId != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.help_item_transparent))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = keyText,
            fontSize = 14.sp,
            color = colorResource(R.color.colorWhite),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 35.dp)
        )

        if (showShare) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = colorResource(R.color.colorWhite)
                )
            }
        }

        if (showDelete) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = colorResource(R.color.colorWhite)
                )
            }
        }
    }
}

@Composable
private fun getKeyText(
    context: android.content.Context,
    key: RCreatedLockerKey,
    deviceType: RMasterUnitType
): String {
    return when (key.purpose) {
        RLockerKeyPurpose.DELIVERY -> {
            val formattedTime = formatCorrectDate(key.timeCreated)
            if (deviceType == RMasterUnitType.MPL) {
                context.getString(
                    R.string.peripheral_settings_share_access,
                    key.lockerSize,
                    formattedTime
                )
            } else {
                context.getString(R.string.peripheral_settings_share_access_spl, formattedTime)
            }
        }

        RLockerKeyPurpose.PAF -> {
            val formattedTime = formatCorrectDate(key.baseTimeCreated ?: "")
            if (key.createdForId != null) {
                if (deviceType == RMasterUnitType.MPL) {
                    context.getString(
                        R.string.peripheral_settings_remove_access,
                        key.createdForEndUserName,
                        key.lockerSize,
                        formattedTime
                    )
                } else {
                    context.getString(
                        R.string.peripheral_settings_remove_access_spl,
                        key.createdForEndUserName,
                        formattedTime
                    )
                }
            } else {
                if (deviceType == RMasterUnitType.MPL) {
                    context.getString(
                        R.string.peripheral_settings_grant_access,
                        key.createdByName,
                        key.lockerSize,
                        formattedTime
                    )
                } else {
                    context.getString(
                        R.string.peripheral_settings_grant_access_spl,
                        key.createdByName,
                        formattedTime
                    )
                }
            }
        }

        else -> ""
    }
}

private fun formatCorrectDate(timeCreated: String): String {
    return try {
        val fromStringToDate = timeCreated.formatFromStringToDate()
        fromStringToDate.formatToViewDateTimeDefaults()
    } catch (e: ParseException) {
        ""
    }
}