package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.send_parcel.PinManagementDialogViewModel
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

    // Load pins when dialog opens
    LaunchedEffect(macAddress) {
        viewModel.loadPins(macAddress)
    }

    Dialog(
        onDismissRequest = {
            onDismiss()
        /* Prevent dismiss on outside click */
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
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
                // Title
                Text(
                    text = stringResource(R.string.pin_managment_title),
                    fontSize = 18.sp,
                    color = colorResource(R.color.colorBlack),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.05.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Content: Loading or Pin List
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
                                items(
                                    items = uiState.pins,
                                    key = { pin -> "${pin.pinId}_${pin.pin}" }
                                ) { pin ->
                                    PinManagementItem(
                                        pin = pin,
                                        isSelected = pin.pinId == uiState.selectedPin?.pinId &&
                                                pin.pin == uiState.selectedPin?.pin,
                                        onSelected = { viewModel.selectPin(pin) },
                                        onToggleNaming = { viewModel.toggleNaming(pin) },
                                        currentPinName = uiState.currentPinName,
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
                        else -> {
                            Text(
                                text = uiState.errorMessage ?: "No pins available",
                                color = colorResource(R.color.colorBlack),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = stringResource(R.string.generated_pin_description),
                    fontSize = 18.sp,
                    color = colorResource(R.color.colorBlack),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !uiState.isLoading && !uiState.isSavingPin
                    ) {
                        Text(
                            text = stringResource(R.string.app_generic_cancel),
                            color = if (!uiState.isLoading && !uiState.isSavingPin) {
                                colorResource(R.color.colorBlack)
                            } else {
                                colorResource(R.color.colorBlack).copy(alpha = 0.5f)
                            }
                        )
                    }

                    TextButton(
                        onClick = {
                            uiState.selectedPin?.let { selectedPin ->
                                viewModel.saveAndConfirm(
                                    macAddress = macAddress,
                                    onComplete = { pin ->
                                        onConfirm(macAddress, pin.toInt(), lockerSize.name)
                                        onDismiss()
                                    }
                                )
                            }
                        },
                        enabled = !uiState.isLoading &&
                                uiState.selectedPin != null &&
                                !uiState.isSavingPin &&
                                !uiState.isDeletingPin
                    ) {
                        if (uiState.isSavingPin) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = colorResource(R.color.colorPrimary)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.app_generic_confirm),
                                color = if (!uiState.isLoading && uiState.selectedPin != null && !uiState.isSavingPin) {
                                    colorResource(R.color.colorBlack)
                                } else {
                                    colorResource(R.color.colorBlack).copy(alpha = 0.5f)
                                }
                            )
                        }
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
    currentPinName: String,
    onNameChanged: (String) -> Unit,
    onSavePin: () -> Unit,
    onToggleDelete: () -> Unit,
    onDeletePin: () -> Unit,
    isSavingPin: Boolean,
    isDeletingPin: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) {
                    colorResource(R.color.colorPrimary).copy(alpha = 0.1f)
                } else {
                    colorResource(R.color.colorWhite)
                },
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = !isSavingPin && !isDeletingPin) {
                onSelected()
            }
            .padding(12.dp)
    ) {
        // Main Row: Pin info and action button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pin.pin,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
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
                        color = colorResource(R.color.colorBlack).copy(alpha = 0.7f)
                    )
                }
            }

            // Action buttons
            if (pin.pinGenerated == true) {
                IconButton(
                    onClick = onToggleNaming,
                    enabled = !isSavingPin && !isDeletingPin
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add name",
                        tint = if (!isSavingPin && !isDeletingPin) {
                            colorResource(R.color.colorPrimary)
                        } else {
                            colorResource(R.color.colorPrimary).copy(alpha = 0.5f)
                        }
                    )
                }
            } else {
                if (!pin.isExtendedToDelete) {
                    IconButton(
                        onClick = onToggleDelete,
                        enabled = !isDeletingPin && !isSavingPin
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Delete",
                            tint = if (!isDeletingPin && !isSavingPin) {
                                colorResource(R.color.colorBlack)
                            } else {
                                colorResource(R.color.colorBlack).copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }
        }

        // Naming section (for generated pins)
        AnimatedVisibility(visible = pin.isExtendedToName && pin.pinGenerated == true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorResource(R.color.colorGray).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentPinName,
                        onValueChange = onNameChanged,
                        placeholder = {
                            Text(
                                stringResource(R.string.pin_managment_input_edittext),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorResource(R.color.colorWhite),
                            unfocusedContainerColor = colorResource(R.color.colorWhite),
                            focusedBorderColor = colorResource(R.color.colorPrimary),
                            unfocusedBorderColor = colorResource(R.color.colorBlack).copy(alpha = 0.3f)
                        ),
                        enabled = !isSavingPin
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSavePin,
                        enabled = currentPinName.isNotEmpty() && !isSavingPin
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
                                tint = if (currentPinName.isNotEmpty()) {
                                    colorResource(R.color.colorPrimary)
                                } else {
                                    colorResource(R.color.colorPrimary).copy(alpha = 0.5f)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation section (for saved pins)
        AnimatedVisibility(visible = pin.isExtendedToDelete && pin.pinGenerated == false) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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
                            containerColor = colorResource(R.color.colorPrimary).copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.height(36.dp),
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
                        modifier = Modifier.height(36.dp),
                        enabled = !isDeletingPin
                    ) {
                        if (isDeletingPin) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = colorResource(R.color.colorWhite),
                                strokeWidth = 2.dp
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