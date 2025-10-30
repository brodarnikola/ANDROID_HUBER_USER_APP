package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import GeneratedPinDialogViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun GeneratedPinDialog(
    macAddress: String,
    lockerSize: RLockerSize,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit,
    viewModel: GeneratedPinDialogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(macAddress) {
        viewModel.loadGeneratedPin(macAddress)
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
                    text = stringResource(R.string.generated_pin_title),
                    fontSize = 18.sp,
                    color = colorResource(R.color.colorBlack),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorResource(R.color.colorGray),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = colorResource(R.color.colorPrimary)
                            )
                        }
                        uiState.generatedPin.isNotEmpty() -> {
                            Text(
                                text = uiState.generatedPin,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.colorBlack),
                                letterSpacing = 12.sp,
                                textAlign = TextAlign.Center
                            )
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
                            if (uiState.generatedPin.isNotEmpty()) {
                                onConfirm(macAddress, uiState.generatedPin.toInt(), lockerSize.name)
                                onDismiss()
                            }
                        },
                        enabled = !uiState.isLoading && uiState.generatedPin.isNotEmpty()
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