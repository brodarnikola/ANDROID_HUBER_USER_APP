package hr.sil.android.schlauebox.compose.view.ui.help

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.view.ui.home.activities.HelpContentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    viewModel: HelpViewModel = viewModel(),
    navigateUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                //.padding(paddingValues)
        ) {

            HorizontalDivider(
                thickness = 1.dp,
                color = colorResource(R.color.colorPinkishGray)
            )

            Text(
                text = stringResource(R.string.help_description_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.colorBlack),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 15.dp, end = 15.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            HelpItem(
                title = stringResource(R.string.app_generic_pickup_parcel),
                onClick = {
                    val startIntent = Intent(context, HelpContentActivity::class.java)
                    startIntent.putExtra("title", R.string.app_generic_pickup_parcel)
                    startIntent.putExtra("content", R.string.help_pickup_parcel_content)
                    startIntent.putExtra("positionOfPicture", 0)
                    context.startActivity(startIntent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            HelpItem(
                title = stringResource(R.string.app_generic_send_parcel),
                onClick = {
                    val startIntent = Intent(context, HelpContentActivity::class.java)
                    startIntent.putExtra("title", R.string.app_generic_send_parcel)
                    startIntent.putExtra("content", R.string.help_send_parcel_text)
                    startIntent.putExtra("positionOfPicture", 1)
                    context.startActivity(startIntent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            HelpItem(
                title = stringResource(R.string.app_generic_key_sharing),
                onClick = {
                    val startIntent = Intent(context, HelpContentActivity::class.java)
                    startIntent.putExtra("title", R.string.app_generic_key_sharing)
                    startIntent.putExtra("content", R.string.help_share_access_text)
                    startIntent.putExtra("positionOfPicture", 2)
                    context.startActivity(startIntent)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.help_lbl_bottom),
                    fontSize = 12.sp,
                    color = colorResource(R.color.colorWhite),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = BuildConfig.APP_BASE_EMAIL,
                    fontSize = 12.sp,
                    color = colorResource(R.color.colorWhite),
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse("mailto:${BuildConfig.APP_BASE_EMAIL}")
                        )
                        context.startActivity(Intent.createChooser(emailIntent, ""))
                    }
                )
            }
        }
    }
}

@Composable
private fun HelpItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.help_item_transparent))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = colorResource(R.color.colorWhite),
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = colorResource(R.color.colorWhite)
        )
    }
}
