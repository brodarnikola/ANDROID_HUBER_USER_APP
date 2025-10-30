package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.util.logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectParcelSizeScreen(
    macAddress: String,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToDelivery: (macAddress: String, pin: Int, size: String) -> Unit = { _, _, _ -> },
    viewModel: SelectParcelSizeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGeneratedPinDialog by remember { mutableStateOf(false) }
    var showPinManagementDialog by remember { mutableStateOf(false) }

    LaunchedEffect(macAddress) {
        logger().info("select parcel size activity, mac address is: $macAddress")
        viewModel.loadAvailableLockers(macAddress)
    }

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onNavigateToLogin()
        }
    }

    if (showGeneratedPinDialog) {
        GeneratedPinDialog(
            macAddress = macAddress,
            lockerSize = uiState.selectedSize,
            onDismiss = { showGeneratedPinDialog = false },
            onConfirm = { mac, pin, size ->
                showGeneratedPinDialog = false
                onNavigateToDelivery(mac, pin, size)
            }
        )
    }

    if (showPinManagementDialog) {
        PinManagementDialog(
            macAddress = macAddress,
            lockerSize = uiState.selectedSize,
            onDismiss = { showPinManagementDialog = false },
            onConfirm = { mac, pin, size ->
                showPinManagementDialog = false
                onNavigateToDelivery(mac, pin, size)
            }
        )
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
            modifier = Modifier
                .fillMaxSize(),
                //.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                color = colorResource(R.color.colorPinkishGray),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = stringResource(R.string.nav_send_parcel_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.colorBlack),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Description
            Text(
                text = stringResource(R.string.send_parcel_description),
                fontSize = 20.sp,
                color = colorResource(R.color.colorBlack),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Parcel Size Grid
            if (uiState.showSizeSelection) {
                ParcelSizeGrid(
                    availableSizes = uiState.availableSizes,
                    selectedSize = uiState.selectedSize,
                    deviceType = uiState.deviceType,
                    onSizeSelected = { viewModel.selectSize(it) }
                )
            }

            Spacer(modifier = Modifier.weight(1f).border(2.dp, Color.Blue, RectangleShape))
            Box(
                modifier = Modifier.wrapContentSize()
                    .padding(15.dp).border(2.dp, Color.Green, RectangleShape),
                contentAlignment = Alignment.BottomCenter
            ) {


            // Next Button
            //if (uiState.showNextButton) {
                Button(
                    onClick = {
                        val device = uiState.device
                        val selectedSize = uiState.selectedSize
                        if (device != null && selectedSize != RLockerSize.UNKNOWN) {
                            showPinManagementDialog = true
//                            if (device.pinManagementAllowed == false) {
//                                showGeneratedPinDialog = true
//                                //onNavigateToGeneratedPinDialog(macAddress, selectedSize)
//                            } else {
//                                showPinManagementDialog = true
//                                //onNavigateToPinManagement(macAddress, selectedSize)
//                            }
                        }
                    },
                    enabled = uiState.selectedSize != RLockerSize.UNKNOWN && !uiState.isLoading,
                    modifier = Modifier
                        .width(220.dp)
                        .height(40.dp)
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorPrimary)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_generic_next),
                        color = Color.White
                    )
                }

            }
           // }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ParcelSizeGrid(
    availableSizes: Map<RLockerSize, Int>,
    selectedSize: RLockerSize,
    deviceType: String,
    onSizeSelected: (RLockerSize) -> Unit
) {
    val sizes = listOf(
        RLockerSize.XS,
        RLockerSize.S,
        RLockerSize.M,
        RLockerSize.L,
        RLockerSize.XL
    )

    // Filter sizes based on device type
    val visibleSizes = if (deviceType == "SPL_PLUS" || deviceType == "SPL") {
        sizes.filter { it != RLockerSize.XS && it != RLockerSize.M && it != RLockerSize.XL }
    } else {
        sizes
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        visibleSizes.chunked(3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { size ->
                    ParcelSizeItem(
                        size = size,
                        count = availableSizes[size] ?: 0,
                        isSelected = selectedSize == size,
                        onSelected = { onSizeSelected(size) },
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ParcelSizeItem(
    size: RLockerSize,
    count: Int,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val drawableRes = when (size) {
        RLockerSize.XS -> if (count == 0) R.drawable.btn_size_xs_unavailable
        else if (isSelected) R.drawable.btn_size_xs_selected
        else R.drawable.btn_size_xs_unselected

        RLockerSize.S -> if (count == 0) R.drawable.btn_size_s_unavailable
        else if (isSelected) R.drawable.btn_size_s_selected
        else R.drawable.btn_size_s_unselected

        RLockerSize.M -> if (count == 0) R.drawable.btn_size_m_unavailable
        else if (isSelected) R.drawable.btn_size_m_selected
        else R.drawable.btn_size_m_unselected

        RLockerSize.L -> if (count == 0) R.drawable.btn_size_l_unavailable
        else if (isSelected) R.drawable.btn_size_l_selected
        else R.drawable.btn_size_l_unselected

        RLockerSize.XL -> if (count == 0) R.drawable.btn_size_xl_unavailable
        else if (isSelected) R.drawable.btn_size_xl_selected
        else R.drawable.btn_size_xl_unselected

        else -> R.drawable.btn_size_xs_unavailable
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = size.name,
            modifier = Modifier
                .size(80.dp)
                .clickable(enabled = count > 0) { onSelected() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = context.getString(R.string.send_parcel_available, count.toString()),
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}