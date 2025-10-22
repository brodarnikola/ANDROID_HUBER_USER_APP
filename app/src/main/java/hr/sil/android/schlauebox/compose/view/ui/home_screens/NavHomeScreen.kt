
package hr.sil.android.schlauebox.compose.view.ui.home_screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.sil.android.schlauebox.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.schlauebox.cache.DatabaseHandler
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTypography
import hr.sil.android.schlauebox.compose.view.ui.theme.Black
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.InstalationType
import hr.sil.android.schlauebox.core.remote.model.RLockerKeyPurpose
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.data.ItemHomeScreen
import hr.sil.android.schlauebox.store.model.MPLDevice

import androidx.compose.material3.MaterialTheme as Material3

import hr.sil.android.schlauebox.store.MPLDeviceStore


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NavHomeScreen(
    viewModel: NavHomeViewModel = viewModel(),
    onDeviceClick: (macAddress: String) -> Unit,
    //onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDevices()
    }

    // Handle unauthorized event
    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            //onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            //.background(Color.Black) // Adjust based on your theme
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Header with user info
            UserAddressHeader(
                userName = uiState.userName,
                address = uiState.address,
                modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp)
            )


            // Device list or empty state
            if (uiState.devices.isEmpty()) {
                EmptyDeviceState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                )
            } else {
                DeviceList(
                    devices = uiState.devices,
                    onDeviceClick = onDeviceClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun UserAddressHeader(
    userName: String,
    address: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.TopCenter  // Changed from Center to TopCenter
    ) {
        // Background card with address info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,  // Changed from Bottom to Center
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = 35.dp)  // Space for the profile icon
                .height(65.dp)
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.colorWhite),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.colorGray),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(top = 15.dp)  // Internal padding to push text down
        ) {
            Text(
                text = userName,
                color = colorResource(R.color.colorBlack),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                maxLines = 1,  // Maximum 2 lines
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Text(
                text = address,
                color = colorResource(R.color.colorBlack),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 14.sp,
                maxLines = 1,  // Maximum 2 lines
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
            )
        }

        // Profile icon overlay (on top)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.colorDarkAccent)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Profile Icon",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<ItemHomeScreen>,
    onDeviceClick: (macAddress: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(devices) { item ->
            when (item) {
                is ItemHomeScreen.Header -> {
                    DeviceHeaderItem(headerTitle = item.headerTitle)
                }
                is ItemHomeScreen.Child -> {
                    DeviceChildItem(
                        device = item,
                        onClick = {
                            onDeviceClick(item.mplOrSplDevice?.macAddress ?: "")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceHeaderItem(
    headerTitle: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = headerTitle.uppercase(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun DeviceChildItem(
    device: ItemHomeScreen.Child,
    onClick: (macAddress: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val parcelLocker = MPLDeviceStore.devices[device.mplOrSplDevice?.macAddress]

    if (parcelLocker == null) return

    val deviceState = remember(parcelLocker) {
        calculateDeviceState(parcelLocker, context)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding( vertical = 5.dp)
            .background( colorResource(R.color.colorWhite30PercentTransparency) )
            .clickable(
                enabled = !deviceState.unavailable,
                onClick =
                {
                    onClick(device.mplOrSplDevice?.macAddress ?: "")
                }
            ),
            //.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Device Icon Section with Badge
        Box(
            modifier = Modifier
                .size(55.dp)
                .weight(2f),
            contentAlignment = Alignment.Center
        ) {
            // Main device icon
            Image(
                painter = painterResource(id = deviceState.iconResId),
                contentDescription = "Device Icon",
                modifier = Modifier.size(45.dp)
            )

            // Notification badge (circle with count)
            if (deviceState.notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-5).dp, y = (-5).dp)
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = deviceState.notificationCount.toString(),
                        color = colorResource(R.color.colorWhite),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .offset( y = (-3).dp)
                    )
                }
            }
        }

        // Device Data Section
        Column(
            modifier = Modifier
                .weight(6.5f)
                .padding(start = 10.dp)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Device Name
            if (deviceState.showName && deviceState.name.isNotEmpty()) {
                Text(
                    text = deviceState.name.uppercase(),
                    color = colorResource(R.color.colorWhite),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,  // Maximum 2 lines
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

            // Device Address
            if (deviceState.showAddress && deviceState.address.isNotEmpty()) {
                Text(
                    text = deviceState.address.uppercase(),
                    color = colorResource(R.color.colorWhite),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,  // Maximum 2 lines
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

            // Locker Availability
            if (deviceState.showAvailability && deviceState.availabilityText.isNotEmpty()) {
                Text(
                    text = deviceState.availabilityText.uppercase(),
                    color = colorResource(R.color.colorBlack),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,  // Maximum 2 lines
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }

        // Arrow Icon
        if (deviceState.showArrow) {
            Image(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = "Navigate",
                modifier = Modifier
                    .weight(1.5f)
                    .padding(start = 20.dp)
            )
        } else {
            Spacer(modifier = Modifier.weight(1.5f))
        }
    }
}

// Data class to hold device UI state
private data class DeviceState(
    val iconResId: Int,
    val name: String,
    val address: String,
    val availabilityText: String,
    val showName: Boolean,
    val showAddress: Boolean,
    val showAvailability: Boolean,
    val showArrow: Boolean,
    val unavailable: Boolean,
    val notificationCount: Int
)

// Calculate device state based on all conditions
private fun calculateDeviceState(parcelLocker: MPLDevice, context: Context): DeviceState {
    // Calculate notification count
    val lockerKeys = parcelLocker.activeKeys.filter {
        it.purpose != RLockerKeyPurpose.UNKNOWN && it.purpose != RLockerKeyPurpose.PAH
    }
    val usedKeys = DatabaseHandler.deliveryKeyDb.get(parcelLocker.macAddress)?.keyIds ?: listOf()
    val notificationCount = lockerKeys.map { it.id }.subtract(usedKeys.asIterable()).size

    var unavailable = false
    var showAvailability = false
    var availabilityText = ""
    var name = parcelLocker.name
    var address = parcelLocker.address
    var showArrow = true

    // Determine icon based on device type and state
    val iconResId = when {
        // MPL Master Device
        (parcelLocker.installationType == InstalationType.DEVICE &&
                parcelLocker.masterUnitType == RMasterUnitType.MPL) ||
                parcelLocker.type == MPLDeviceType.MASTER -> {
            when {
                parcelLocker.isInBleProximity &&
                        parcelLocker.hasUserRightsOnLocker() &&
                        parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED -> {
                    showAvailability = true
                    availabilityText = parcelLocker.availableLockers.joinToString(" ") {
                        "${it.size}: ${it.count}"
                    }
                    R.drawable.ic_available_mpl
                }
                !parcelLocker.isInBleProximity && parcelLocker.hasUserRightsOnLocker() -> {
                    showAvailability = true
                    availabilityText = parcelLocker.availableLockers.joinToString(" ") {
                        "${it.size}: ${it.count}"
                    }
                    R.drawable.ic_locker_yellow
                }
                parcelLocker.isInBleProximity && !parcelLocker.hasUserRightsOnLocker() -> {
                    R.drawable.ic_unregistered_mpl
                }
                else -> {
                    unavailable = true
                    showArrow = false
                    address = context.getString(R.string.app_generic_not_activated)
                    R.drawable.ic_unavailable_mpl
                }
            }
        }

        // Tablet Device
        parcelLocker.installationType == InstalationType.TABLET ||
                parcelLocker.type == MPLDeviceType.TABLET -> {
            when {
                parcelLocker.isInBleProximity &&
                        parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED &&
                        parcelLocker.masterUnitId != -1 -> {
                    R.drawable.ic_t_mpl_green
                }
                parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED &&
                        parcelLocker.isInBleProximity &&
                        parcelLocker.masterUnitId == -1 -> {
                    R.drawable.ic_t_mpl_grey
                }
                !parcelLocker.isInBleProximity -> {
                    R.drawable.ic_t_mpl_yellow
                }
                else -> {
                    unavailable = true
                    showArrow = false
                    R.drawable.ic_t_mpl_red
                }
            }
        }

        // SPL Device
        else -> {
            when {
                parcelLocker.isInBleProximity && parcelLocker.hasUserRightsOnLocker() -> {
                    if (parcelLocker.name.isEmpty()) {
                        name = parcelLocker.macAddress
                    }
                    R.drawable.ic_available_spl
                }
                parcelLocker.isInBleProximity &&
                        !parcelLocker.isSplActivate &&
                        !parcelLocker.hasUserRightsOnLocker() -> {
                    if (parcelLocker.address.isEmpty()) {
                        name = parcelLocker.macAddress
                        address = context.getString(R.string.app_generic_not_activated)
                    }
                    R.drawable.ic_s_locker_grey
                }
                !parcelLocker.isInBleProximity && parcelLocker.hasUserRightsOnLocker() -> {
                    R.drawable.ic_s_locker_yellow
                }
                parcelLocker.isInBleProximity && !parcelLocker.hasUserRightsOnLocker() -> {
                    unavailable = true
                    showArrow = false
                    R.drawable.ic_unavailable_spl
                }
                else -> {
                    unavailable = true
                    showArrow = false
                    address = context.getString(R.string.app_generic_access_forbidden)
                    R.drawable.ic_unavailable_spl
                }
            }
        }
    }

    // Determine what to show based on name and address
    val showName = name.isNotEmpty()
    val showAddress = address.isNotEmpty()

    return DeviceState(
        iconResId = iconResId,
        name = name,
        address = address,
        availabilityText = availabilityText,
        showName = showName,
        showAddress = showAddress,
        showAvailability = showAvailability,
        showArrow = showArrow,
        unavailable = unavailable,
        notificationCount = notificationCount
    )
}

@Composable
private fun EmptyDeviceState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.locker_details_registration_body),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}