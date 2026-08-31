package com.vanshkashya.liquidglass

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val backdrop = rememberLayerBackdrop()

    var drawerOpen by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf(currentTime()) }

    val apps = remember {
        loadInstalledApps(context)
    }

    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE7F1FF),
                        Color(0xFFF9FBFF),
                        Color(0xFFDCE7F5)
                    )
                )
            )
    ) {

        /*
         * BACKDROP SOURCE
         *
         * Kyant0's library samples this layer
         * to create the glass refraction.
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {

            Box(
                modifier = Modifier
                    .offset(
                        x = (-90).dp,
                        y = 160.dp
                    )
                    .size(350.dp)
                    .clip(RoundedCornerShape(180.dp))
                    .background(Color(0x5593C9FF))
            )

            Box(
                modifier = Modifier
                    .offset(
                        x = 270.dp,
                        y = 620.dp
                    )
                    .size(390.dp)
                    .clip(RoundedCornerShape(200.dp))
                    .background(Color(0x55B7A0FF))
            )
        }

        if (drawerOpen) {

            AppDrawer(
                apps = apps,
                backdrop = backdrop,
                onClose = {
                    drawerOpen = false
                }
            )

        } else {

            HomeScreen(
                time = time,
                backdrop = backdrop,
                onOpenDrawer = {
                    drawerOpen = true
                }
            )
        }

        /*
         * Swipe gesture on the whole launcher.
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(drawerOpen) {

                    detectVerticalDragGestures(
                        onVerticalDrag = { _, amount ->

                            if (!drawerOpen && amount < -15f) {
                                drawerOpen = true
                            }

                            if (drawerOpen && amount > 15f) {
                                drawerOpen = false
                            }
                        }
                    )
                }
        )
    }
}

@Composable
fun HomeScreen(
    time: String,
    backdrop: com.kyant.backdrop.Backdrop,
    onOpenDrawer: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = time,
            fontSize = 64.sp,
            color = Color(0xFF101820)
        )

        Text(
            text = SimpleDateFormat(
                "EEEE, d MMMM",
                Locale.getDefault()
            ).format(Date()),
            fontSize = 17.sp,
            color = Color(0xFF526273)
        )

        Spacer(
            modifier = Modifier.height(50.dp)
        )

        /*
         * REAL KYANT0 BACKDROP GLASS SEARCH
         */
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(58.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedRect(29.dp.toPx())
                    },
                    effects = {
                        vibrancy()
                        blur(4.dp.toPx())
                        lens(
                            16.dp.toPx(),
                            24.dp.toPx()
                        )
                    },
                    onDrawSurface = {
                        drawRect(
                            Color.White.copy(alpha = 0.18f)
                        )
                    }
                )
                .clickable {
                    onOpenDrawer()
                },
            contentAlignment = Alignment.CenterStart
        ) {

            Text(
                text = "⌕   Search apps",
                modifier = Modifier.padding(
                    horizontal = 24.dp
                ),
                fontSize = 17.sp,
                color = Color(0xFF526273)
            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "↑  Swipe up for apps",
            fontSize = 13.sp,
            color = Color(0xFF667585)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * LIQUID GLASS DOCK
         */
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(78.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedRect(39.dp.toPx())
                    },
                    effects = {
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(
                            20.dp.toPx(),
                            30.dp.toPx()
                        )
                    },
                    onDrawSurface = {
                        drawRect(
                            Color.White.copy(alpha = 0.18f)
                        )
                    }
                )
                .clickable {
                    onOpenDrawer()
                },
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "⌕     ◉     ◎     ▦",
                fontSize = 25.sp,
                color = Color(0xFF17232E)
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    backdrop: com.kyant.backdrop.Backdrop,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 45.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
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
                text = apps.size.toString(),
                fontSize = 14.sp,
                color = Color(0xFF637181)
            )
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        /*
         * GLASS DRAWER CONTAINER
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedRect(32.dp.toPx())
                    },
                    effects = {
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(
                            22.dp.toPx(),
                            34.dp.toPx()
                        )
                    },
                    onDrawSurface = {
                        drawRect(
                            Color.White.copy(alpha = 0.16f)
                        )
                    }
                )
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(
                    top = 20.dp,
                    bottom = 40.dp
                )
            ) {

                items(
                    items = apps,
                    key = {
                        it.packageName
                    }
                ) { app ->

                    AppIcon(
                        app = app,
                        backdrop = backdrop,
                        onClick = {

                            val launchIntent =
                                context.packageManager
                                    .getLaunchIntentForPackage(
                                        app.packageName
                                    )

                            if (launchIntent != null) {
                                context.startActivity(
                                    launchIntent
                                )
                            }
                        }
                    )
                }
            }

            /*
             * Close area
             */
            Text(
                text = "↓",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .clickable {
                        onClose()
                    },
                fontSize = 18.sp,
                color = Color(0xFF5B6875)
            )
        }
    }
}

@Composable
fun AppIcon(
    app: InstalledApp,
    backdrop: com.kyant.backdrop.Backdrop,
    onClick: () -> Unit
) {

    val bitmap = remember(app.packageName) {

        app.icon
            .toBitmap(
                width = 120,
                height = 120
            )
            .asImageBitmap()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 12.dp,
                horizontal = 4.dp
            )
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /*
         * Each icon gets a small real liquid-glass
         * surface instead of a normal alpha box.
         */
        Box(
            modifier = Modifier
                .size(66.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedRect(20.dp.toPx())
                    },
                    effects = {
                        vibrancy()
                        blur(3.dp.toPx())
                        lens(
                            10.dp.toPx(),
                            18.dp.toPx()
                        )
                    },
                    onDrawSurface = {
                        drawRect(
                            Color.White.copy(alpha = 0.14f)
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            Image(
                bitmap = bitmap,
                contentDescription = app.name,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = app.name,
            fontSize = 11.sp,
            color = Color(0xFF26313C),
            maxLines = 1
        )
    }
}

fun loadInstalledApps(
    context: Context
): List<InstalledApp> {

    val packageManager = context.packageManager

    val intent = Intent(
        Intent.ACTION_MAIN,
        null
    ).apply {
        addCategory(
            Intent.CATEGORY_LAUNCHER
        )
    }

    return packageManager
        .queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )
        .mapNotNull { info ->

            val packageName =
                info.activityInfo.packageName

            val name =
                info.loadLabel(packageManager)
                    ?.toString()
                    ?: return@mapNotNull null

            val icon =
                info.loadIcon(packageManager)
                    ?: return@mapNotNull null

            InstalledApp(
                name = name,
                packageName = packageName,
                icon = icon
            )
        }
        .distinctBy {
            it.packageName
        }
        .sortedBy {
            it.name.lowercase(Locale.getDefault())
        }
}

fun currentTime(): String {

    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date())
}
