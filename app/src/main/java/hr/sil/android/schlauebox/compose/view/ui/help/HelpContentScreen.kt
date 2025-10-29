package hr.sil.android.schlauebox.compose.view.ui.help

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpContentScreen(
    titleResId: Int,
    contentResId: Int,
    picturePosition: Int,
    viewModel: HelpContentViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(titleResId, contentResId, picturePosition) {
        viewModel.loadContent(titleResId, contentResId, picturePosition)
    }

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onNavigateToLogin()
        }
    }

    val imageRes = when (picturePosition) {
        0 -> R.drawable.img_pickup_parcel_small
        1 -> R.drawable.img_send_parcel_small
        else -> R.drawable.img_share_access_small
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
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = colorResource(R.color.colorPinkishGray)
            )

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Help illustration",
                modifier = Modifier
                    .padding(top = 10.dp)
                    .align(Alignment.CenterHorizontally)
            )

            if (titleResId != 0) {
                Text(
                    text = stringResource(id = titleResId),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 25.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center
                    ),
                    lineHeight = 14.sp,
                    color = colorResource(R.color.colorBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp, start = 15.dp, end = 15.dp, bottom = 10.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 5.dp)
            ) {
                if (contentResId != 0) {
                    Text(
                        text = stringResource(id = contentResId),
                        fontSize = 14.sp,
                        color = colorResource(R.color.colorBlack),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

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
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_generic_help))
                        context.startActivity(Intent.createChooser(emailIntent, ""))
                    }
                )
            }
        }
    }
}