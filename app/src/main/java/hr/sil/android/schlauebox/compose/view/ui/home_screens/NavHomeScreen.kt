/*
 * Copyright © 2022 Sunbird. All rights reserved.
 *
 * Sunbird Secure Messaging
 *
 * Created by Cinnamon.
 */
package hr.sil.android.schlauebox.compose.view.ui.home_screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.data.ItemHomeScreen


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NavHomeScreen(
    viewModel: NavHomeViewModel = viewModel(),
    //onDeviceClick: (ItemHomeScreen.Child) -> Unit,
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
            .background(Color.Black) // Adjust based on your theme
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with user info
            UserAddressHeader(
                userName = uiState.userName,
                address = uiState.address,
                modifier = Modifier.padding(top = 40.dp, start = 16.dp, end = 16.dp)
            )

            // Profile icon overlay
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfileIcon()
            }

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
                    //onDeviceClick = onDeviceClick,
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
            .height(60.dp)
            .background(
                color = Color(0xFFF5F5F5), // Adjust based on profile_address_background
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = userName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = address,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileIcon(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color.White), // Adjust based on profile_icon_background
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = "Profile Icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DeviceList(
    devices: List<ItemHomeScreen>,
    //onDeviceClick: (ItemHomeScreen.Child) -> Unit,
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
                        //onClick = { onDeviceClick(item) }
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
        text = headerTitle,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun DeviceChildItem(
    device: ItemHomeScreen.Child,
    //onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implement your device item UI here based on your existing adapter layout
    // This is a placeholder - adjust based on your MplSplAdapter implementation
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = Color.DarkGray,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = device.mplOrSplDevice?.name ?: "Unknown Device",
            color = Color.White,
            fontSize = 16.sp
        )
    }
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