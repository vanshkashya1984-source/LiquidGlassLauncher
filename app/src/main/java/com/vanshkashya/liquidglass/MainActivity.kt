package com.vanshkashya.liquidglass

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consume
import androidx.core.graphics.drawable.toBitmap
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class InstalledApp(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LiquidGlassLauncher()
        }
    }
}

@Composable
fun LiquidGlassLauncher() {

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val backdrop = rememberLayerBackdrop()

    var apps by remember {
        mutableStateOf(loadInstalledApps(context))
    }

    var query by remember {
        mutableStateOf("")
    }

    var drawerOpen by remember {
        mutableStateOf(false)
    }

    /*
     * Drawer progress:
     *
     * 0f = home
     * 1f = completely open
     */
    val drawerProgress = remember {
        Animatable(0f)
    }

    var dragDistance by remember {
        mutableFloatStateOf(0f)
    }

    var time by remember {
        mutableStateOf(currentTime())
    }

    LaunchedEffect(Unit) {

        while (true) {
            time = currentTime()
            delay(1000)
        }
    }

    DisposableEffect(Unit) {

        val lifecycleOwner = context as ComponentActivity

        val observer =
            androidx.lifecycle.LifecycleEventObserver { _, event ->

                if (
                    event ==
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME
                ) {
                    apps = loadInstalledApps(context)
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(drawerOpen) {

        drawerProgress.animateTo(
            targetValue = if (drawerOpen) 1f else 0f,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val filteredApps =
        remember(apps, query) {

            if (query.isBlank()) {
                apps
            } else {
                apps.filter {
                    it.name.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }
        }

    /*
     * Root gesture.
     *
     * Drawer follows the finger instead of
     * appearing instantly.
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFEAF4FF),
                        Color(0xFFF8FBFF),
                        Color(0xFFD9E6F4)
                    )
                )
            )
            .pointerInput(drawerOpen) {

                detectVerticalDragGestures(

                    onVerticalDrag = { change, amount ->

                        change.consume()

                        dragDistance += amount

                        val height =
                            size.height.toFloat()

                        val progressDelta =
                            amount / height

                        val newProgress =
                            (
                                drawerProgress.value -
                                    progressDelta
                                )
                                .coerceIn(0f, 1f)

                        drawerProgress.snapTo(
                            newProgress
                        )
                    },

                    onDragEnd = {

                        val shouldOpen =
                            drawerProgress.value > 0.22f

                        haptic.performHapticFeedback(
                            HapticFeedbackType.TextHandleMove
                        )

                        drawerOpen = shouldOpen

                        dragDistance = 0f
                    },

                    onDragCancel = {

                        drawerOpen =
                            drawerProgress.value > 0.5f

                        dragDistance = 0f
                    }
                )
            }
    ) {

        /*
         * =====================================================
         * BACKGROUND
         * =====================================================
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {

            /*
             * Large background light
             */
            Box(
                modifier = Modifier
                    .offset(
                        x = (-100).dp,
                        y = 110.dp
                    )
                    .size(390.dp)
                    .clip(
                        RoundedCornerShape(195.dp)
                    )
                    .background(
                        Color(0x5596CFFF)
                    )
            )

            /*
             * Secondary light
             */
            Box(
                modifier = Modifier
                    .offset(
                        x = 260.dp,
                        y = 570.dp
                    )
                    .size(420.dp)
                    .clip(
                        RoundedCornerShape(210.dp)
                    )
                    .background(
                        Color(0x55B7A7FF)
                    )
            )

            /*
             * Small highlight
             */
            Box(
                modifier = Modifier
                    .offset(
                        x = 110.dp,
                        y = 390.dp
                    )
                    .size(180.dp)
                    .clip(
                        RoundedCornerShape(90.dp)
                    )
                    .background(
                        Color(0x3320C8FF)
                    )
            )
        }

        /*
         * =====================================================
         * HOME SCREEN
         * =====================================================
         */

        HomeScreen(
            time = time,
            backdrop = backdrop,
            drawerProgress = drawerProgress.value,
            onOpenDrawer = {

                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )

                drawerOpen = true
            }
        )

        /*
         * =====================================================
         * DRAWER
         * =====================================================
         */

        AppDrawer(
            apps = filteredApps,
            query = query,
            onQueryChange = {
                query = it
            },
            backdrop = backdrop,
            progress = drawerProgress.value,
            onClose = {

                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )

                drawerOpen = false
                query = ""
            }
        )
    }
}

/* ============================================================
 * HOME
 * ============================================================ */

@Composable
fun HomeScreen(
    time: String,
    backdrop: Backdrop,
    drawerProgress: Float,
    onOpenDrawer: () -> Unit
) {

    val scale =
        1f - (drawerProgress * 0.045f)

    val alpha =
        1f - (drawerProgress * 0.55f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {

                scaleX = scale
                scaleY = scale

                this.alpha = alpha
            }
            .padding(
                top = 74.dp,
                bottom = 28.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        /*
         * TIME
         */

        Text(
            text = time,
            fontSize = 68.sp,
            color = Color(0xFF101820),
            letterSpacing = (-2).sp
        )

        Text(
            text =
                SimpleDateFormat(
                    "EEEE, d MMMM",
                    Locale.getDefault()
                ).format(Date()),
            fontSize = 16.sp,
            color = Color(0xFF596979)
        )

        Spacer(
            modifier = Modifier.height(45.dp)
        )

        /*
         * SEARCH GLASS
         */

        GlassSearch(
            backdrop = backdrop,
            text = "Search apps",
            onClick = onOpenDrawer
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "↑  Swipe up",
            fontSize = 13.sp,
            color = Color(0xFF647384)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * GLASS DOCK
         */

        GlassDock(
            backdrop = backdrop,
            onOpenDrawer = onOpenDrawer
        )
    }
}

/* ============================================================
 * SEARCH
 * ============================================================ */

@Composable
fun GlassSearch(
    backdrop: Backdrop,
    text: String,
    onClick: () -> Unit
) {

    PressableGlass(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .height(58.dp),
        onClick = onClick
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF526171)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = text,
                fontSize = 16.sp,
                color = Color(0xFF596979)
            )
        }
    }
}

/* ============================================================
 * DOCK
 * ============================================================ */

@Composable
fun GlassDock(
    backdrop: Backdrop,
    onOpenDrawer: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .height(82.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = {
                    RoundedCornerShape(41.dp)
                },
                effects = {

                    vibrancy()

                    blur(12f)

                    lens(
                        22f,
                        36f
                    )
                }
            )
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement =
                Arrangement.SpaceEvenly,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            DockButton(
                icon = Icons.Default.Phone,
                onClick = {}
            )

            DockButton(
                icon = Icons.Default.Search,
                onClick = onOpenDrawer
            )

            DockButton(
                icon = Icons.Default.GridView,
                onClick = onOpenDrawer
            )
        }
    }
}

@Composable
fun DockButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    var pressed by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .scale(
                if (pressed) 0.88f else 1f
            )
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                Color.White.copy(
                    alpha =
                        if (pressed) 0.55f
                        else 0.28f
                )
            )
            .pointerInput(Unit) {

                androidx.compose.foundation.gestures.detectTapGestures(

                    onPress = {

                        pressed = true

                        try {
                            tryAwaitRelease()
                        } finally {
                            pressed = false
                        }

                        onClick()
                    }
                )
            },
        contentAlignment =
            Alignment.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF18232E)
        )
    }
}

/* ============================================================
 * DRAWER
 * ============================================================ */

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    query: String,
    onQueryChange: (String) -> Unit,
    backdrop: Backdrop,
    progress: Float,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    /*
     * Drawer slides from bottom.
     */

    val screenHeight =
        with(
            androidx.compose.ui.platform.LocalDensity.current
        ) {
            900.dp.toPx()
        }

    val translateY =
        (1f - progress) * screenHeight

    val drawerScale =
        0.94f + (progress * 0.06f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {

                translationY = translateY

                scaleX = drawerScale
                scaleY = drawerScale

                alpha = progress

                transformOrigin =
                    androidx.compose.ui.graphics.TransformOrigin(
                        0.5f,
                        1f
                    )
            }
    ) {

        /*
         * Dim layer.
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha =
                            0.04f * progress
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 35.dp,
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 18.dp
                )
        ) {

            /*
             * HEADER
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Apps",
                    fontSize = 34.sp,
                    color = Color(0xFF111A22)
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${apps.size}",
                    fontSize = 14.sp,
                    color = Color(0xFF637181)
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            /*
             * MAIN GLASS PANEL
             */

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = {
                            RoundedCornerShape(34.dp)
                        },
                        effects = {

                            vibrancy()

                            blur(14f)

                            lens(
                                24f,
                                38f
                            )
                        }
                    )
            ) {

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    /*
                     * SEARCH FIELD
                     */

                    DrawerSearch(
                        value = query,
                        onValueChange =
                            onQueryChange
                    )

                    /*
                     * APP GRID
                     */

                    LazyVerticalGrid(
                        columns =
                            GridCells.Fixed(4),

                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                top = 8.dp,
                                bottom = 60.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(8.dp),

                        horizontalArrangement =
                            Arrangement.spacedBy(2.dp)
                    ) {

                        items(
                            items = apps,
                            key = {
                                it.packageName
                            }
                        ) { app ->

                            AppGridItem(
                                app = app,
                                onClick = {

                                    val int
