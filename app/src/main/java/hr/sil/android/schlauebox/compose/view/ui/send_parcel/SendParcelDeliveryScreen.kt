package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendParcelDeliveryScreen(
    macAddress: String,
    pin: Int,
    size: String,
    onNavigateBack: () -> Unit = {},
    onFinish: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: SendParcelDeliveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(macAddress, pin, size) {
        viewModel.sendParcel(macAddress, pin, size)
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
        Image(
            painter = painterResource(id = R.drawable.bg_three),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                color = colorResource(R.color.colorPinkishGray),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.nav_send_parcel_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.colorBlack),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(90.dp))

            Image(
                painter = painterResource(R.drawable.ic_package),
                contentDescription = "Package Icon",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = colorResource(R.color.colorPrimary)
                    )
                }

                uiState.isSuccess -> {
                    Text(
                        text = stringResource(R.string.nav_send_parcel_content_text),
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                }

                uiState.hasError -> {
                    Text(
                        text = stringResource(R.string.nav_send_parcel_failed),
                        fontSize = 16.sp,
                        color = colorResource(R.color.colorError),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            when {
                uiState.isSuccess -> {
                    Button(
                        onClick = onFinish,
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
                            text = stringResource(R.string.app_generic_finish),
                            color = Color.White
                        )
                    }
                }

                uiState.hasError -> {
                    Button(
                        onClick = { viewModel.sendParcel(macAddress, pin, size) },
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
                            text = stringResource(R.string.nav_send_parcel_error_button),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
