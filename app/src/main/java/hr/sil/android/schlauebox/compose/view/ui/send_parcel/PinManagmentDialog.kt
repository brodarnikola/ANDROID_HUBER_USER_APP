package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RPinManagement

@Composable
fun PinManagementDialog(
    macAddress: String,
    lockerSize: RLockerSize,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit,
    viewModel: PinManagementDialogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(macAddress) {
        viewModel.loadPins(macAddress)
    }

    Dialog(onDismissRequest = { }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            color = colorResource(R.color.colorWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.pin_managment_title),
                    fontSize = 18.sp,
                    color = colorResource(R.color.colorBlack),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = colorResource(R.color.colorPrimary)
                            )
                        }
                        uiState.pins.isNotEmpty() -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.pins) { pin ->
                                    PinManagementItem(
                                        pin = pin,
                                        isSelected = pin.pinId == uiState.selectedPin?.pinId,
                                        onSelected = { viewModel.selectPin(pin) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.generated_pin_description),
                    fontSize = 18.sp,
                    color = colorResource(R.color.colorBlack),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            text = stringResource(R.string.app_generic_cancel),
                            color = colorResource(R.color.colorBlack)
                        )
                    }

                    TextButton(
                        onClick = {
                            uiState.selectedPin?.let { selectedPin ->
                                viewModel.savePinIfNeeded(macAddress)
                                onConfirm(macAddress, selectedPin.pin.toInt(), lockerSize.name)
                                onDismiss()
                            }
                        },
                        enabled = !uiState.isLoading && uiState.selectedPin != null
                    ) {
                        Text(
                            text = stringResource(R.string.app_generic_confirm),
                            color = colorResource(R.color.colorBlack)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}

@Composable
private fun PinManagementItem(
    pin: RPinManagement,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) colorResource(R.color.colorPrimary).copy(alpha = 0.1f)
                else colorResource(R.color.colorWhite),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onSelected() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (pin.pinGenerated == true) {
                Text(
                    text = "Generated PIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.colorBlack)
                )
            } else {
                pin.pinName?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.colorBlack)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = pin.pin,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.colorBlack)
            )
        }

        if (isSelected) {
            Text(
                text = "✓",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.colorPrimary)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}