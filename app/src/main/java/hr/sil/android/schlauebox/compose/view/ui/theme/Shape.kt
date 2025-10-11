package hr.sil.android.schlauebox.compose.view.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import androidx.compose.material3.Shapes as Material3Shapes

val AppShapes =
    Material3Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )