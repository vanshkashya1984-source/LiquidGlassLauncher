package com.vanshkashya.liquidglass

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

private data class InstalledApp(
    val name: String,
    val packageName: String,
    val activityName: String,
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
private fun LiquidGlassLauncher() {

    val context = LocalContext.current
    val backdrop = rememberLayerBackdrop()

    var drawerOpen by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    var time by remember { mutableStateOf(currentTime()) }

    LaunchedEffect(Unit) {
        apps = loadInstalledApps(context)

        while (true) {
            time = currentTime()
            delay(1000L)
        }
    }

    DisposableEffect(Unit) {

        val activity = context as ComponentActivity

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_RESUME) {
                apps = loadInstalledApps(context)
            }
        }

        activity.lifecycle.addObserver(observer)

        onDispose {
            activity.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE9F3FF),
                        Color(0xFFF7FAFF),
                        Color(0xFFDCE9F7)
                    )
                )
            )
    ) {

        /*
         * BACKGROUND
         *
         * The backdrop captures everything behind the glass.
         */
        BackgroundScene(backdrop = backdrop)

        /*
         * HOME
         */
        HomeScreen(
            visible = !drawerOpen,
            time = time,
            backdrop = backdrop,
            onOpenDrawer = {
                drawerOpen = true
            }
        )

        /*
         * APP DRAWER
         */
        AnimatedVisibility(
            visible = drawerOpen,
            enter = fadeIn(
                animationSpec = tween(180)
            ) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 420,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(150)
            ) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(320)
            ),
            modifier = Modifier.fillMaxSize()
        ) {

            AppDrawer(
                apps = apps,
                backdrop = backdrop,
                onClose = {
                    drawerOpen = false
                }
            )
        }

        /*
         * GLOBAL SWIPE AREA
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(drawerOpen) {

                    detectVerticalDragGestures(
                        onVerticalDrag = { _, _ -> },

                        onDragEnd = {
                            // Gesture completion is handled below
                        }
                    )
                }
        )

        /*
         * Dedicated gesture layer.
         *
         * It only reacts to sufficiently large swipes.
         */
        SwipeController(
            drawerOpen = drawerOpen,
            onDrawerChange = {
                drawerOpen = it
            }
        )
    }
}

/*
 * Background blobs.
 */
@Composable
private fun BackgroundScene(
    backdrop: Backdrop
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .layerBackdrop(backdrop)
    ) {

        Box(
            modifier = Modifier
                .offset(
                    x = (-105).dp,
                    y = 170.dp
                )
                .size(370.dp)
                .clip(CircleShape)
                .background(
                    Color(0x5598CFFF)
                )
        )

        Box(
            modifier = Modifier
                .offset(
                    x = 330.dp,
                    y = 650.dp
                )
                .size(390.dp)
                .clip(CircleShape)
                .background(
                    Color(0x55B7A6FF)
                )
        )

        Box(
            modifier = Modifier
                .offset(
                    x = 180.dp,
                    y = 780.dp
                )
                .size(230.dp)
                .clip(CircleShape)
                .background(
                    Color(0x5538D9B2)
                )
        )
    }
}

/*
 * Separate gesture controller.
 *
 * This avoids the old consume()/GridView gesture errors.
 */
@Composable
private fun SwipeController(
    drawerOpen: Boolean,
    onDrawerChange: (Boolean) -> Unit
) {

    var accumulatedDrag by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(drawerOpen) {

                detectVerticalDragGestures(

                    onDragStart = {
                        accumulatedDrag = 0f
                    },

                    onVerticalDrag = { _, dragAmount ->

                        accumulatedDrag += dragAmount
                    },

                    onDragEnd = {

                        if (!drawerOpen && accumulatedDrag < -90f) {
                            onDrawerChange(true)
                        }

                        if (drawerOpen && accumulatedDrag > 90f) {
                            onDrawerChange(false)
                        }

                        accumulatedDrag = 0f
                    },

                    onDragCancel = {
                        accumulatedDrag = 0f
                    }
                )
            }
    )
}

/*
 * HOME SCREEN
 */
@Composable
private fun HomeScreen(
    visible: Boolean,
    time: String,
    backdrop: Backdrop,
    onOpenDrawer: () -> Unit
) {

    if (!visible) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 64.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = time,
            fontSize = 68.sp,
            color = Color(0xFF101820)
        )

        Text(
            text = currentDate(),
            fontSize = 17.sp,
            color = Color(0xFF596979)
        )

        Spacer(
            modifier = Modifier.height(44.dp)
        )

        /*
         * SEARCH GLASS
         */
        GlassPanel(
            backdrop = backdrop,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(58.dp),
            radius = 30.dp
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onOpenDrawer()
                    }
                    .padding(horizontal = 21.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF4F6070),
                    modifier = Modifier.size(21.dp)
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "Search apps",
                    fontSize = 17.sp,
                    color = Color(0xFF536273)
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "↑  Swipe up",
            fontSize = 13.sp,
            color = Color(0xFF617282)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * GLASS DOCK
         */
        GlassPanel(
            backdrop = backdrop,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(82.dp),
            radius = 42.dp
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                DockButton(
                    icon = {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Phone",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                ) {
                    openSystemApp(
                        LocalContext.current,
                        "com.android.dialer"
                    )
                }

                DockButton(
                    icon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    onClick = onOpenDrawer
                )

                DockButton(
                    icon = {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = "Apps",
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    onClick = onOpenDrawer
                )

                DockButton(
                    icon = {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = "Widgets",
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    onClick = onOpenDrawer
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )
    }
}

/*
 * APP DRAWER
 */
@Composable
private fun AppDrawer(
    apps: List<InstalledApp>,
    backdrop: Backdrop,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    var search by remember {
        mutableStateOf("")
    }

    val filteredApps = remember(
        apps,
        search
    ) {

        if (search.isBlank()) {
            apps
        } else {
            apps.filter {
                it.name.contains(
                    search,
                    ignoreCase = true
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 42.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 0.dp
            )
    ) {

        /*
         * TOP HANDLE
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Color.White.copy(alpha = 0.75f)
                    )
            )
        }

        /*
         * DRAWER PANEL
         */
        GlassPanel(
            backdrop = backdrop,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            radius = 34.dp
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 18.dp,
                        start = 18.dp,
                        end = 18.dp
                    )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Apps",
                        fontSize = 34.sp,
                        color = Color(0xFF101820)
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${filteredApps.size}",
                        fontSize = 14.sp,
                        color = Color(0xFF657585)
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    GlassCircleButton(
                        onClick = onClose
                    ) {

                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF273441)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /*
                 * SEARCH
                 */
                GlassPanel(
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    radius = 26.dp
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF586979),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        androidx.compose.foundation.text.BasicTextField(
                            value = search,
                            onValueChange = {
                                search = it
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xFF17232E)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->

                                Box {

                                    if (search.isEmpty()) {

                                        Text(
                                            text = "Search apps",
                                            fontSize = 16.sp,
                                            color = Color(0xFF68798A)
                                        )
                                    }

                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                /*
                 * REAL APP GRID
                 */
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 10.dp,
                        bottom = 35.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(
                        items = filteredApps,
                        key = { it.packageName }
                    ) { app ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    launchApp(context, app)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            val bitmap = app.icon.toBitmap()

                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = app.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .graphicsLayer { alpha = 0.98f }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = app.name,
                                fontSize = 12.sp,
                                color = Color(0xFF36424A)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Helper functions used by the activity ---

private fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA)

    return resolveInfos.map { info ->
        val activity = info.activityInfo
        InstalledApp(
            name = info.loadLabel(pm).toString(),
            packageName = activity.packageName,
            activityName = activity.name,
            icon = info.loadIcon(pm)
        )
    }.sortedBy { it.name }
}

private fun launchApp(context: Context, app: InstalledApp) {
    try {
        val intent = Intent().apply {
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: try to open package main activity
        val pm = context.packageManager
        val i = pm.getLaunchIntentForPackage(app.packageName)
        i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (i != null) {
            context.startActivity(i)
        }
    }
}

private fun openSystemApp(context: Context, pkg: String) {
    val pm = context.packageManager
    val intent = pm.getLaunchIntentForPackage(pkg)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

fun currentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}

fun currentDate(): String {
    return SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
}
