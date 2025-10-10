package hr.sil.android.schlauebox.compose.view.ui.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hr.sil.android.schlauebox.compose.view.ui.theme.DarkColorScheme
import hr.sil.android.schlauebox.compose.view.ui.theme.LightColorScheme


import androidx.compose.material3.MaterialTheme as Material3

@Composable
fun NewDesignButton(
    paddingTop: Dp? = null,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    verticalTextPadding: Dp = 10.dp
) {
    val finalPaddingTop = if( paddingTop != null ) paddingTop else 32.dp
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = finalPaddingTop)
    ) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier
                .heightIn(min = 40.dp)
                .widthIn(min = 248.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Material3.colorScheme.primary,
                contentColor = if(enabled) Material3.colorScheme.onPrimary else if(!isSystemInDarkTheme()) Material3.colorScheme.primary.copy(alpha = 0.12f) else Material3.colorScheme.primary,
                disabledContainerColor = Material3.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            enabled = enabled
        ) {
            Text(
                text = title,
                color = if(enabled) Material3.colorScheme.onPrimary else if(!isSystemInDarkTheme()) Material3.colorScheme.primary.copy(alpha = 0.12f) else Material3.colorScheme.primary,
                style = Material3.typography.labelLarge,
                modifier = Modifier
                    .padding(vertical = verticalTextPadding)
            )
        }
    }
}

@Composable
fun NewDesignButton2(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
//            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Material3.colorScheme.primary,
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            color = Material3.colorScheme.onPrimary,
            style = Material3.typography.labelLarge
        )
    }
}

@Composable
fun PagerCard(
    image: Int,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.heightIn(min = 20.dp))
        ResizableImage(imageRes = image)
        Spacer(modifier = Modifier.heightIn(min = 20.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .heightIn(min = 200.dp, max = 450.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                color = Material3.colorScheme.onSurface, // onboarding screens - default color
                style = Material3.typography.headlineLarge.copy(textAlign = TextAlign.Center),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.heightIn(min = 16.dp))
            Text(
                text = description,
                style = Material3.typography.bodyLarge,
                color = Material3.colorScheme.onSurfaceVariant, // onboarding screens - default color
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DotsIndicatorNewDesign(
    totalDots: Int,
    selectedIndex: Int,
) {
    val colorScheme = when {
        isSystemInDarkTheme() -> DarkColorScheme
        else -> LightColorScheme
    }
    LazyRow(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Material3.colorScheme.primary)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = colorScheme.outline,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ResizableImage(@DrawableRes imageRes: Int) {
    BoxWithConstraints {
        val imageModifier = if (maxWidth < maxHeight) {
            Modifier
                .height(maxWidth)
                .fillMaxWidth()
        } else {
            Modifier
                .width(maxHeight)
                .fillMaxHeight()
        }
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = imageModifier,
            contentScale = ContentScale.FillBounds
        )
    }
}
