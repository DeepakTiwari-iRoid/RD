package com.app.thoughtrisemotivational

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.rd.R

@Preview(showBackground = false)
@Composable
private fun ThoughtBottomNavPreview() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            ThoughtBottomNav(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                topLevelRoute = topLevelRoutes,
                currentDestination = currentDestination,
                textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                param = ParamBottomBar(color = Color(0xFF8A9AB0), cornerRadius = 80f)
            ) {
                /*navController.navigate(it.title) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }*/
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {

        }
    }
}


@Composable
fun ThoughtBottomNav(
    modifier: Modifier = Modifier,
    topLevelRoute: List<TopLevelRoute<String>>,
    param: ParamBottomBar,
    currentDestination: NavDestination?,
    textStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.White),
    onItemClick: (TopLevelRoute<String>) -> Unit
) {

    val ctx = LocalContext.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableIntStateOf(0) }
    var previousIndex by remember { mutableIntStateOf(-1) }


    val currentIcon = getBitmapFromVectorDrawable(ctx, topLevelRoute[selectedIndex].selectedIcon)
    val itemsSize = topLevelRoute.size


    // State variables to control the indicator position
    var indXOffset by remember { mutableFloatStateOf(70f) }
    var indYOffset by remember { mutableFloatStateOf(190f) }

    // Animating the position of the indicator
    val animateXOffset by animateFloatAsState(targetValue = indXOffset, label = "xOffset")
    val animateYOffset by animateFloatAsState(targetValue = indYOffset, label = "yOffset")

    val canvasModifier = modifier.then(
        Modifier
            .height(86.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val iconWidth = currentIcon?.width ?: 0
                    val iconHeight = currentIcon?.height ?: 0
                    val totalSpacing = size.width - (iconWidth * itemsSize)
                    val iconSpacing = totalSpacing / (itemsSize + 1)

                    topLevelRoute.forEachIndexed { index, _ ->
                        val xOffset = calculateIconPosition(index, iconWidth, iconSpacing.toFloat())
                        val yOffset = (size.height / 2) - (iconHeight / 2)

                        val iconBounds = Rect(
                            offset = Offset(xOffset, yOffset.toFloat()),
                            size = Size(iconWidth.toFloat(), iconHeight.toFloat())
                        )

                        if (iconBounds.contains(offset)) {

                            val selectedItem = topLevelRoutes.firstOrNull { it.route == currentDestination?.route }
                            selectedIndex = topLevelRoutes.indexOf(selectedItem)

                            if (index in topLevelRoute.indices) {
                                onItemClick(topLevelRoute[selectedIndex])
                                indXOffset = xOffset + (iconWidth - 80.dp.toPx()) / 2
                                indYOffset = size.height - 17.dp.toPx()
                            }
                        }
                        return@detectTapGestures
                    }
                }
            })

    Canvas(modifier = canvasModifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val (indicatorWidth, indicatorHeight) = with(density) {
            Pair(80.dp.toPx(), 17.dp.toPx())
        }

        val iconWidth = currentIcon?.width ?: 0
        val totalSpacing = canvasWidth - (iconWidth * itemsSize)
        val iconSpacing = totalSpacing / (itemsSize + 1)

        if (indXOffset == 0f && indYOffset == 0f) {
            indXOffset = calculateIconPosition(selectedIndex, iconWidth, iconSpacing) + (iconWidth - indicatorWidth) / 2
            indYOffset = canvasHeight - indicatorHeight
        }

        drawBackground(canvasWidth, canvasHeight, param.cornerRadius, param.strokeWidth, param.color)

        topLevelRoute.forEachIndexed { index, item ->
            val isSelected = selectedIndex == index
            val bitmap = getBitmapForItem(ctx, isSelected, item)

            if (isSelected && previousIndex != index) {
                onItemClick(topLevelRoute[index])
                previousIndex = index
            }

            bitmap?.let { icon ->
                val xOffset = calculateIconPosition(index, icon.width, iconSpacing)
                val yOffset = (canvasHeight / 2) - (icon.height / 2)
                drawImage(icon.asImageBitmap(), Offset(xOffset, yOffset))
            }
        }

        drawIndicatorPath(animateXOffset, animateYOffset, indicatorWidth, indicatorHeight, param.color)

        // Draw Item title with the animated offset
        val measuredText =
            textMeasurer.measure(
                AnnotatedString(topLevelRoute[selectedIndex].title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = textStyle
            )


        drawText(
            measuredText, topLeft = Offset(
                animateXOffset + (indicatorWidth - measuredText.size.width) / 2,
                animateYOffset + (indicatorHeight - measuredText.size.height) / 2
            )
        )

    }
}

fun getBitmapForItem(ctx: Context, isSelected: Boolean, item: TopLevelRoute<String>): Bitmap? {
    return getBitmapFromVectorDrawable(ctx, if (isSelected) item.selectedIcon else item.unSelectedIcon)
}

fun calculateIconPosition(index: Int, iconWidth: Int, iconSpacing: Float): Float {
    return iconSpacing * (index + 1) + iconWidth * index
}

fun DrawScope.drawBackground(
    canvasWidth: Float,
    canvasHeight: Float,
    backgroundRadius: Float,
    strokeWidth: Float,
    contentColor: Color
) {
    val outerBorder = Path().apply {
        moveTo(0f, 0f)
        lineTo(canvasWidth, 0f)
        lineTo(canvasWidth, canvasHeight)
        lineTo(0f, canvasHeight)
        close()
    }

    drawRoundRect(
        color = Color.White,
        topLeft = outerBorder.getBounds().topLeft,
        cornerRadius = CornerRadius(backgroundRadius, backgroundRadius - (strokeWidth * 2))
    )

    drawPath(
        path = outerBorder,
        color = contentColor,
        style = Stroke(width = strokeWidth, pathEffect = PathEffect.cornerPathEffect(backgroundRadius))
    )
}

fun DrawScope.drawIndicatorPath(
    animateXOffset: Float,
    animateYOffset: Float,
    indicatorWidth: Float,
    indicatorHeight: Float,
    contentColor: Color
) {
    val cornerRadius = 50f
    val path = Path().apply {
        moveTo(animateXOffset + cornerRadius, animateYOffset)
        lineTo(animateXOffset + indicatorWidth - cornerRadius, animateYOffset)
        arcTo(
            Rect(Offset(animateXOffset + indicatorWidth - cornerRadius, animateYOffset), Size(cornerRadius, cornerRadius)),
            startAngleDegrees = 270f, sweepAngleDegrees = 90f, forceMoveTo = false
        )
        lineTo(animateXOffset + indicatorWidth, animateYOffset + indicatorHeight)
        lineTo(animateXOffset, animateYOffset + indicatorHeight)
        lineTo(animateXOffset, animateYOffset + cornerRadius)
        arcTo(
            Rect(Offset(animateXOffset, animateYOffset), Size(cornerRadius, cornerRadius)),
            startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false
        )
        close()
    }

    drawPath(path, contentColor)
}


fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
    // Get the drawable (VectorDrawable or any other drawable type)
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

    // Set intrinsic width and height if not specified
    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

    // Create the bitmap
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Create a canvas that will be used to draw the vector drawable
    val canvas = android.graphics.Canvas(bitmap)

    // Set the bounds of the drawable to fit inside the canvas
    drawable.setBounds(0, 0, canvas.width, canvas.height)

    // Draw the vector drawable onto the canvas
    drawable.draw(canvas)

    return bitmap
}


data class TopLevelRoute<T : Any>(
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unSelectedIcon: Int,
    val title: String,
    val route: T
)

@Stable
data class ParamBottomBar(
    val color: Color = Color.Black,
    val cornerRadius: Float = 0f,
    val strokeWidth: Float = 5f
)


val topLevelRoutes = listOf(
    TopLevelRoute(
        R.drawable.ic_home_filled,
        R.drawable.ic_home_outline,
        "Home",
        "home"
    ), TopLevelRoute(
        R.drawable.ic_home_filled,
        R.drawable.ic_home_outline,
        "Archive",
        "home"
    ), TopLevelRoute(
        R.drawable.ic_home_filled,
        R.drawable.ic_home_outline,
        "Subscription",
        "home"
    ), TopLevelRoute(
        R.drawable.ic_home_filled,
        R.drawable.ic_home_outline,
        "Profile",
        "home"
    )
)
