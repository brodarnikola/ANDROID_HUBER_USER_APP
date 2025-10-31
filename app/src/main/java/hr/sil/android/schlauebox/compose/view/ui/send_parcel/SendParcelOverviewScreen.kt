package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.RCreatedLockerKey
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.view.ui.home.activities.SendParcelDeliveryActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendParcelsOverviewScreen(
    macAddress: String,
    viewModel: SendParcelsOverviewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showPinDialog by remember { mutableStateOf(false) }
    var showPinManagementDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf<RCreatedLockerKey?>(null) }
    var showNotInProximityDialog by remember { mutableStateOf(false) }
    LaunchedEffect(macAddress) {
        viewModel.loadData(macAddress)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_three),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(uiState.parcels) { index, parcel ->
                    SendParcelItem(
                        parcel = parcel,
                        deviceType = uiState.deviceType,
                        isInProximity = uiState.isInBleProximity,
                        onCancelClick = {
                            if (uiState.isInBleProximity == true) {
                                showCancelDialog = parcel
                            } else {
                                showNotInProximityDialog = true
                            }
                        },
                        isCancelling = uiState.cancellingParcelId == parcel.id
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.showNextButton) {
                    Button(
                        onClick = {
                            if (uiState.device?.masterUnitType == RMasterUnitType.MPL ||
                                uiState.device?.type == MPLDeviceType.SPL_PLUS
                            ) {
                                // Navigate to SelectParcelSizeScreen
                            } else {
                                if (uiState.device?.pinManagementAllowed == false) {
                                    showPinDialog = true
                                } else {
                                    showPinManagementDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .width(220.dp)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.colorPrimary)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_generic_next),
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    if (showPinDialog) {
        GeneratedPinDialog(
            macAddress = macAddress,
            lockerSize = RLockerSize.S,
            onDismiss = { showPinDialog = false },
            onConfirm = { mac, pin, size ->
//                val intent = Intent(context, SendParcelDeliveryActivity::class.java).apply {
//                    putExtra("rMacAddress", mac)
//                    putExtra("pin", pin)
//                    putExtra("size", size)
//                }
//                context.startActivity(intent)
            }
        )
    }
    if (showPinManagementDialog) {
        PinManagementDialog(
            macAddress = macAddress,
            lockerSize = RLockerSize.S,
            onDismiss = { showPinManagementDialog = false },
            onConfirm = { mac, pin, size ->
//                val intent = Intent(context, SendParcelDeliveryActivity::class.java).apply {
//                    putExtra("rMacAddress", mac)
//                    putExtra("pin", pin)
//                    putExtra("size", size)
//                }
//                context.startActivity(intent)
            }
        )
    }
    showCancelDialog?.let { parcel ->
        CancelDeliveryDialog(
            parcel = parcel,
            onDismiss = { showCancelDialog = null },
            onConfirm = {
                viewModel.cancelParcel(
                    macAddress = macAddress,
                    parcel = parcel
                )
                showCancelDialog = null
            }
        )
    }
    if (showNotInProximityDialog) {
        CancelDeliveryNotInProximityDialog(
            onDismiss = { showNotInProximityDialog = false }
        )
    }
}

@Composable
private fun SendParcelItem(
    parcel: RCreatedLockerKey,
    deviceType: MPLDeviceType,
    isInProximity: Boolean?,
    onCancelClick: () -> Unit,
    isCancelling: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorWhite)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.app_generic_parcel_pin, parcel.pin),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.colorBlack)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (deviceType == MPLDeviceType.SPL) {
                    Text(
                        text = stringResource(
                            R.string.app_generic_time_created,
                            parcel.timeCreated
                        ),
                        fontSize = 14.sp,
                        color = colorResource(R.color.colorBlack)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.app_generic_size, parcel.lockerSize ?: ""),
                        fontSize = 14.sp,
                        color = colorResource(R.color.colorBlack)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(
                            R.string.app_generic_time_created,
                            parcel.timeCreated
                        ),
                        fontSize = 12.sp,
                        color = colorResource(R.color.colorGray)
                    )
                }
            }
            if (isCancelling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colorResource(R.color.colorPrimary)
                )
            } else {
                IconButton(onClick = onCancelClick) {
                    Icon(
                        painter = painterResource(
                            id = if (isInProximity == true)
                                R.drawable.ic_cancel_access
                            else
                                R.drawable.ic_cancel_access_disabled
                        ),
                        contentDescription = "Cancel",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

@Composable
fun CancelDeliveryDialog(
    parcel: RCreatedLockerKey,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.app_generic_are_you_sure),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Cancel delivery message: ${parcel.pin}", //stringResource(R.string.app_generic_cancel_delivery_message),
                fontSize = 14.sp
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
                    text = stringResource(R.string.app_generic_cancel),
                    color = colorResource(R.color.colorBlack)
                )
            }
        },
        containerColor = colorResource(R.color.colorWhite),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun CancelDeliveryNotInProximityDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.app_generic_error),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.not_in_proximity_of_locker),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.app_generic_ok),
                    color = colorResource(R.color.colorPrimary)
                )
            }
        },
        containerColor = colorResource(R.color.colorWhite),
        shape = RoundedCornerShape(8.dp)
    )
}