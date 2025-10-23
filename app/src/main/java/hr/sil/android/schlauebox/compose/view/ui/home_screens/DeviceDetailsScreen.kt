package hr.sil.android.schlauebox.compose.view.ui.home_screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.view.ui.dialog.GeneratedPinDialog
import hr.sil.android.schlauebox.view.ui.dialog.PinManagmentDialog
import hr.sil.android.schlauebox.view.ui.home.activities.*

@Composable
fun DeviceDetailsScreen(
    macAddress: String,
    nameOfDevice: String,
    viewModel: DeviceDetailsViewModel = viewModel(),
    onNavigateToPickup: () -> Unit = {},
    onNavigateToSendParcel: () -> Unit = {},
    onNavigateToAccessSharing: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(macAddress) {
        viewModel.loadDeviceDetails(macAddress)
    }

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DeviceNameTitle(
                deviceName = uiState.deviceName,
                modifier = Modifier.padding(top = 14.dp, start = 20.dp, end = 20.dp)
            )

            DeviceAddressHeader(
                address = uiState.deviceAddress,
                showEditButton = uiState.showEditButton,
                onEditClick = {
                    val startIntent = Intent(context, EditSplActivity::class.java)
                    startIntent.putExtra("rMacAddress", macAddress)
                    context.startActivity(startIntent)
                },
                modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp)
            )

            if (uiState.hasUserRights) {
                DeviceActionsSection(
                    macAddress = macAddress,
                    nameOfDevice = nameOfDevice,
                    uiState = uiState,
                    onPickupClick = {
                        val startIntent = Intent(context, PickupParcelActivity::class.java)
                        startIntent.putExtra("rMacAddress", macAddress)
                        context.startActivity(startIntent)
                    },
                    onSendParcelClick = {
                        handleSendParcelClick(context, macAddress, uiState)
                    },
                    onAccessSharingClick = {
                        val startIntent = Intent(context, AccessSharingActivity::class.java)
                        startIntent.putExtra("rMacAddress", macAddress)
                        startIntent.putExtra("nameOfDevice", nameOfDevice)
                        context.startActivity(startIntent)
                    },
                    onHelpClick = {
                        val startIntent = Intent(context, HelpActivity::class.java)
                        startIntent.putExtra("rMacAddress", macAddress)
                        context.startActivity(startIntent)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )

                TelemetrySection(
                    humidity = uiState.humidity,
                    temperature = uiState.temperature,
                    pressure = uiState.pressure,
                    rssi = uiState.rssi,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                NoAccessSection(
                    uiState = uiState,
                    onRequestAccessClick = {
                        viewModel.requestAccess()
                    },
                    onForceOpenClick = {
                        viewModel.forceOpenDevice(context)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceNameTitle(
    deviceName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = deviceName,
        color = colorResource(R.color.colorBlack),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun DeviceAddressHeader(
    address: String,
    showEditButton: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = colorResource(R.color.colorWhite),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = colorResource(R.color.colorGray),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = address,
                color = colorResource(R.color.colorBlack),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (showEditButton) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceActionsSection(
    macAddress: String,
    nameOfDevice: String,
    uiState: DeviceDetailsUiState,
    onPickupClick: () -> Unit,
    onSendParcelClick: () -> Unit,
    onAccessSharingClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                iconRes = R.drawable.ic_pickup_parcel,
                text = stringResource(R.string.app_generic_pickup_parcel),
                enabled = uiState.pickupButtonEnabled,
                onClick = onPickupClick,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            ActionButton(
                iconRes = R.drawable.ic_pickup_parcel,
                text = stringResource(R.string.app_generic_send_parcel),
                enabled = uiState.sendParcelButtonEnabled,
                onClick = onSendParcelClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                iconRes = R.drawable.ic_key_sharing,
                text = stringResource(R.string.app_generic_access_sharing),
                enabled = uiState.accessSharingButtonEnabled,
                onClick = onAccessSharingClick,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            ActionButton(
                iconRes = R.drawable.ic_help,
                text = stringResource(R.string.app_generic_help),
                enabled = true,
                onClick = onHelpClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    iconRes: Int,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(180.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(160.dp),
            alpha = if (enabled) 1f else 0.4f
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = colorResource(R.color.colorWhite),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
//    Column(
//        modifier = modifier
//            .size(160.dp)
//            .clickable(enabled = enabled, onClick = onClick),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Image(
//            painter = painterResource(id = iconRes),
//            contentDescription = text,
//            modifier = Modifier.size(120.dp),
//            alpha = if (enabled) 1f else 0.4f
//        )
//
//        Text(
//            text = text,
//            color = colorResource(R.color.colorWhite),
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Normal,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(top = 8.dp)
//        )
//    }
}

@Composable
private fun TelemetrySection(
    humidity: String,
    temperature: String,
    pressure: String,
    rssi: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.colorDarkAccent))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TelemetryItem(
            iconRes = R.drawable.ic_humidity,
            value = "$humidity%"
        )

        TelemetryItem(
            iconRes = R.drawable.ic_temperature,
            value = "$temperature C"
        )

        TelemetryItem(
            iconRes = R.drawable.ic_air_pressure,
            value = "$pressure HPa"
        )

        TelemetryItem(
            iconRes = R.drawable.ic_signal_strength,
            value = "$rssi db"
        )
    }
}

@Composable
private fun TelemetryItem(
    iconRes: Int,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = value,
            color = colorResource(R.color.colorWhite),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun NoAccessSection(
    uiState: DeviceDetailsUiState,
    onRequestAccessClick: () -> Unit,
    onForceOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!uiState.showRequestPendingText) {
                Text(
                    text = stringResource(R.string.locker_details_registration_body),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.colorWhite),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.showForceOpenButton && !uiState.isForceOpening) {
                    Button(
                        onClick = onForceOpenClick,
                        modifier = Modifier
                            .width(220.dp)
                            .height(35.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.colorPrimary)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_generic_force_open),
                            color = colorResource(R.color.colorWhite),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (uiState.isForceOpening) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(35.dp),
                        color = colorResource(R.color.colorPrimary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (uiState.showRequestAccessButton && !uiState.isRequestingAccess) {
                    val device = MPLDeviceStore.devices[uiState.macAddress]
                    val buttonText = if (device?.masterUnitType == RMasterUnitType.SPL ||
                        device?.type == MPLDeviceType.SPL ||
                        device?.type == MPLDeviceType.SPL_PLUS) {
                        stringResource(R.string.locker_details_activate_btn)
                    } else {
                        stringResource(R.string.locker_details_registration_btn)
                    }

                    Button(
                        onClick = onRequestAccessClick,
                        modifier = Modifier
                            .width(220.dp)
                            .height(35.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.colorPrimary)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = buttonText,
                            color = colorResource(R.color.colorWhite),
                            fontSize = 14.sp
                        )
                    }
                }

                if (uiState.isRequestingAccess) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(35.dp),
                        color = colorResource(R.color.colorPrimary)
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.locker_details_registration_pending_body),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.colorWhite),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
            }
        }
    }
}

private fun handleSendParcelClick(
    context: android.content.Context,
    macAddress: String,
    uiState: DeviceDetailsUiState
) {
    val device = MPLDeviceStore.devices[macAddress]
    val isPaHListAvailable = hasUserShareKeys(uiState)

    if (isPaHListAvailable) {
        val startIntent = Intent(context, SendParcelsOverviewActivity::class.java)
        startIntent.putExtra("rMacAddress", macAddress)
        context.startActivity(startIntent)
    } else {
        if (device?.masterUnitType == RMasterUnitType.MPL ||
            device?.installationType == hr.sil.android.schlauebox.core.remote.model.InstalationType.TABLET ||
            (device?.type == MPLDeviceType.SPL_PLUS &&
                    device?.keypadType != hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType.SPL)) {
            val startIntent = Intent(context, SelectParcelSizeActivity::class.java)
            startIntent.putExtra("rMacAddress", macAddress)
            context.startActivity(startIntent)
        }
    }
}

private fun hasUserShareKeys(uiState: DeviceDetailsUiState): Boolean {
    if (uiState.activeKeysForLocker.isEmpty() || uiState.pahKeys.isEmpty()) return false

    for (activeKey in uiState.activeKeysForLocker) {
        for (pahKey in uiState.pahKeys) {
            if (activeKey.createdById == pahKey.createdById) {
                return true
            }
        }
    }
    return false
}