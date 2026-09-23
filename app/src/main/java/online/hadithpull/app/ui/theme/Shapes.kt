package online.hadithpull.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object HadithShapes {
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(26.dp)
    val pill = RoundedCornerShape(50)
}

val hadithMaterialShapes = Shapes(
    small = HadithShapes.sm,
    medium = HadithShapes.md,
    large = HadithShapes.lg,
)
