package hr.sil.android.schlauebox.compose.view.ui.onboarding_screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hr.sil.android.schlauebox.compose.view.ui.components.NewDesignButton
import hr.sil.android.schlauebox.compose.view.ui.components.ResizableImage
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTheme
import hr.sil.android.schlauebox.compose.view.ui.theme.Neutral90
import hr.sil.android.schlauebox.compose.view.ui.theme.Primary60
import hr.sil.android.schlauebox.compose.view.ui.theme.Primary90
import androidx.compose.material3.MaterialTheme as Material3

import hr.sil.android.schlauebox.R


@Composable
fun FirstOnboardingScreen(
    modifier: Modifier = Modifier,
    nextScreen: (route: String) -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.introductionBackgroundColor)
        ) {
            val (mainContent, bottomButton) = createRefs()

            Column(Modifier
                .constrainAs(mainContent) {
                    top.linkTo(parent.top)
                    bottom.linkTo(bottomButton.top)
                    start.linkTo(parent.start, margin = 24.dp)
                    end.linkTo(parent.end, margin = 24.dp)

                    height = Dimension.fillToConstraints
                }
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.heightIn(min = 20.dp))
                ResizableImage(imageRes = R.drawable.bg_onboarding)
                Spacer(modifier = Modifier.heightIn(min = 20.dp))
                androidx.compose.material3.Text(
                    text = stringResource(R.string.access_sharing_admin_role),
                    color = Material3.colorScheme.onPrimary, // Material3.colorScheme.onSurface, // onboarding screens - default color
                    style = Material3.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.heightIn(min = 16.dp))
                androidx.compose.material3.Text(
                    text = stringResource(R.string.access_sharing_admin_role),
                    style = Material3.typography.bodyLarge,
                    color = Material3.colorScheme.onSurfaceVariant, // onboarding screens - default color
                    textAlign = TextAlign.Center
                )
            }

            NewDesignButton(
                modifier = Modifier
                    .constrainAs(bottomButton) {
                        top.linkTo(mainContent.bottom)
                        bottom.linkTo(parent.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 24.dp)
                        end.linkTo(parent.end, margin = 24.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                    .semantics {
                        contentDescription = "onboardingButtonGetStarted"
                    },
                title = stringResource(R.string.access_sharing_admin_role),
                onClick = {
                    //nextScreen(SignUpOnboardingSections.SECOND_ONBOARDING_SCREEN.route)
                }
            )
        }
    }
}

@Composable
fun SecondOnboardingScreen(
    modifier: Modifier = Modifier,
    nextScreen: (route: String) -> Unit = {},
    titleText: String,
    descriptionText: String,
    textAlign: TextAlign = TextAlign.Center,
    buttonText: String = "",
    pageNumber: String? = null,
    firstImage: Int,
    secondeImage: Int? = null,
    nextScreenRoute: String
) {

    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.introductionBackgroundColor)
        ) {
            val (mainContent, bottomButton) = createRefs()

            Column(Modifier
                .constrainAs(mainContent) {
                    top.linkTo(parent.top)
                    bottom.linkTo(bottomButton.top)
                    start.linkTo(parent.start, margin = 24.dp)
                    end.linkTo(parent.end, margin = 24.dp)

                    height = Dimension.fillToConstraints
                }
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = firstImage),
                    contentDescription = null,
//                    contentScale = ContentScale.FillBounds
                )
                Spacer(modifier = Modifier.heightIn(min = 20.dp))
                androidx.compose.material3.Text(
                    text = titleText,
                    color = White, // Material3.colorScheme.onSurface, // onboarding screens - default color
                    style = Material3.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.heightIn(min = 16.dp))
                androidx.compose.material3.Text(
                    text = descriptionText,
                    style = Material3.typography.bodyLarge,
                    color = Neutral90, // Material3.colorScheme.onSurfaceVariant, // onboarding screens - default color
                    textAlign = textAlign // TextAlign.Start
                )
                if( secondeImage != null ) {
                    Spacer(modifier = Modifier.heightIn(min = 16.dp))
                    Image(
                        modifier = Modifier.height(260.dp).width(160.dp),
                        painter = painterResource(id = secondeImage),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds
                    )
                }
            }

            Column(
                modifier = Modifier
                    .constrainAs(bottomButton) {
                        top.linkTo(mainContent.bottom)
                        bottom.linkTo(parent.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 24.dp)
                        end.linkTo(parent.end, margin = 24.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                    .semantics {
                        contentDescription = "onboardingButtonGetStarted"
                    },
            ) {

                if( pageNumber != null ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = pageNumber,
                            style = Material3.typography.labelLarge,
                            color = White, // Material3.colorScheme.onPrimary, // onboarding screens - default color
                            textAlign = TextAlign.Start
                        )
                        androidx.compose.material3.Text(
                            text = " / 3",
                            style = Material3.typography.labelLarge,
                            color = Primary90, // onboarding screens - default color
                            textAlign = TextAlign.Start
                        )
                    }
                }
                Spacer(modifier = Modifier.heightIn(min = 1.dp))
                FilledTonalButton(
                    onClick = {
                        nextScreen(nextScreenRoute)
                    },
                    modifier = Modifier
                        .heightIn(min = 20.dp)
                        .fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = Primary60, // Material3.colorScheme.primary,
                        contentColor = Material3.colorScheme.onPrimary,
                        disabledContainerColor = Material3.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    androidx.compose.material3.Text(
                        text = buttonText,
                        color = White,
                        style = Material3.typography.labelLarge,
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}
