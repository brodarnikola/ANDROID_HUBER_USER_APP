package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
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
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RPinManagement
@Composable
fun PinManagementDialog_COPY_2(
    macAddress: String,
    lockerSize: RLockerSize,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit,
    viewModel: PinManagementDialogViewModel_COPY_2 = viewModel()
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
                                itemsIndexed(uiState.pins) { index, pin ->
                                    PinManagementItem(
                                        pin = pin,
                                        isSelected = pin.pinId == uiState.selectedPin?.pinId &&
                                                pin.pin == uiState.selectedPin?.pin,
                                        onSelected = { viewModel.selectPin(pin) },
                                        onToggleNaming = { viewModel.toggleNaming(pin) },
                                        onNameChanged = { name -> viewModel.updatePinName(name) },
                                        onSavePin = { viewModel.saveGeneratedPin(macAddress) },
                                        onToggleDelete = { viewModel.toggleDelete(pin) },
                                        onDeletePin = { viewModel.deletePin(macAddress, pin) },
                                        isSavingPin = uiState.isSavingPin,
                                        isDeletingPin = uiState.isDeletingPin
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
                                if (App.ref.pinManagementName.isNotEmpty() && selectedPin.pinGenerated == true) {
                                    viewModel.saveGeneratedPin(macAddress)
                                }

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
    onSelected: () -> Unit,
    onToggleNaming: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSavePin: () -> Unit,
    onToggleDelete: () -> Unit,
    onDeletePin: () -> Unit,
    isSavingPin: Boolean,
    isDeletingPin: Boolean
) {
    var pinNameInput by remember(pin.pinId, pin.isExtendedToName) {
        mutableStateOf("")
    }

    LaunchedEffect(pin.isExtendedToName) {
        if (!pin.isExtendedToName) {
            pinNameInput = ""
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) colorResource(R.color.colorPrimary).copy(alpha = 0.1f)
                else colorResource(R.color.colorWhite),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onSelected() }
            .padding(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pin.pin,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.colorBlack)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (!pin.isExtendedToName) {
                    Text(
                        text = if (pin.pinGenerated == true) {
                            stringResource(R.string.pin_managment_generate)
                        } else {
                            pin.pinName ?: ""
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.colorBlack)
                    )
                }
            }
            if (pin.pinGenerated == true) {
                IconButton(
                    onClick = onToggleNaming,
                    enabled = !isSavingPin
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add name",
                        tint = colorResource(R.color.colorPrimary)
                    )
                }
            } else {
                if (!pin.isExtendedToDelete) {
                    IconButton(
                        onClick = onToggleDelete,
                        enabled = !isDeletingPin
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Delete",
                            tint = colorResource(R.color.colorBlack)
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = pin.isExtendedToName && pin.pinGenerated == true) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .background(
                        color = colorResource(R.color.colorGray),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = pinNameInput,
                    onValueChange = {
                        pinNameInput = it
                        onNameChanged(it)
                    },
                    placeholder = {
                        Text(stringResource(R.string.pin_managment_input_edittext))
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorResource(R.color.colorGray),
                        unfocusedContainerColor = colorResource(R.color.colorGray),
                        focusedIndicatorColor = colorResource(android.R.color.transparent),
                        unfocusedIndicatorColor = colorResource(android.R.color.transparent)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSavePin,
                    enabled = pinNameInput.isNotEmpty() && !isSavingPin
                ) {
                    if (isSavingPin) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorResource(R.color.colorPrimary)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = colorResource(R.color.colorPrimary)
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = pin.isExtendedToDelete && pin.pinGenerated == false) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_generic_are_you_sure),
                    fontSize = 12.sp,
                    color = colorResource(R.color.colorBlack)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onToggleDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.colorPrimary)
                        ),
                        modifier = Modifier.height(40.dp),
                        enabled = !isDeletingPin
                    ) {
                        Text(
                            text = stringResource(R.string.app_generic_cancel),
                            fontSize = 12.sp,
                            color = colorResource(R.color.colorWhite)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDeletePin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.colorPrimary)
                        ),
                        modifier = Modifier.height(40.dp),
                        enabled = !isDeletingPin
                    ) {
                        if (isDeletingPin) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = colorResource(R.color.colorWhite)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.pin_managment_delete_pin),
                                fontSize = 12.sp,
                                color = colorResource(R.color.colorWhite)
                            )
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}