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

    var drawerOpen by remember {
        mutableStateOf(false)
    }

    var time by remember {
        mutableStateOf(currentTime())
    }

    var apps by remember {
        mutableStateOf(loadInstalledApps(context))
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE7F1FF),
                        Color(0xFFF8FBFF),
                        Color(0xFFDCE7F5)
                    )
                )
            )
    ) {

        /*
         * BACKGROUND / BACKDROP SOURCE
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {

            Box(
                modifier = Modifier
                    .offset(
                        x = (-80).dp,
                        y = 150.dp
                    )
                    .size(360.dp)
                    .clip(
                        RoundedCornerShape(180.dp)
                    )
                    .background(
                        Color(0x5595CFFF)
                    )
            )

            Box(
                modifier = Modifier
                    .offset(
                        x = 270.dp,
                        y = 650.dp
                    )
                    .size(360.dp)
                    .clip(
                        RoundedCornerShape(180.dp)
                    )
                    .background(
                        Color(0x55B5A0FF)
                    )
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
         * SWIPE GESTURE
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(drawerOpen) {

                    detectVerticalDragGestures(
                        onVerticalDrag = { _, amount ->

                            if (
                                !drawerOpen &&
                                amount < -20f
                            ) {
                                drawerOpen = true
                            }

                            if (
                                drawerOpen &&
                                amount > 20f
                            ) {
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
    backdrop: Backdrop,
    onOpenDrawer: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 70.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
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
            color = Color(0xFF536273)
        )

        Spacer(
            modifier = Modifier.height(50.dp)
        )

        /*
         * SEARCH
         */
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(58.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedCornerShape(29.dp)
                    },
                    effects = {
                        vibrancy()
                        blur(8f)
                        lens(18f, 28f)
                    }
                )
                .clickable {
                    onOpenDrawer()
                },
            contentAlignment =
                Alignment.CenterStart
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
         * DOCK
         */
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(78.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedCornerShape(39.dp)
                    },
                    effects = {
                        vibrancy()
                        blur(10f)
                        lens(20f, 32f)
                    }
                )
                .clickable {
                    onOpenDrawer()
                },
            contentAlignment =
                Alignment.Center
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                GlassDockIcon("⌕")
                GlassDockIcon("◉")
                GlassDockIcon("◎")
                GlassDockIcon("▦")
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
fun GlassDockIcon(
    symbol: String
) {

    Text(
        text = symbol,
        fontSize = 25.sp,
        color = Color(0xFF17232E)
    )
}

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    backdrop: Backdrop,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 42.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
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
                text = "${apps.size} apps",
                fontSize = 14.sp,
                color = Color(0xFF637181)
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * DRAWER GLASS PANEL
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedCornerShape(32.dp)
                    },
                    effects = {
                        vibrancy()
                        blur(12f)
                        lens(22f, 36f)
                    }
                )
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = 25.dp,
                        bottom = 45.dp
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

                            val intent =
                                context.packageManager
                                    .getLaunchIntentForPackage(
                                        app.packageName
                                    )

                            if (intent != null) {
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            Text(
                text = "↓",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 5.dp)
                    .clickable {
                        onClose()
                    },
                fontSize = 20.sp,
                color = Color(0xFF526171)
            )
        }
    }
}

@Composable
fun AppIcon(
    app: InstalledApp,
    backdrop: Backdrop,
    onClick: () -> Unit
) {

    val bitmap = remember(
        app.packageName
    ) {

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
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(66.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        RoundedCornerShape(20.dp)
                    },
                    effects = {
                        vibrancy()
                        blur(5f)
                        lens(10f, 18f)
                    }
                ),
            contentAlignment =
                Alignment.Center
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

    val pm = context.packageManager

    val intent = Intent(
        Intent.ACTION_MAIN,
        null
    ).apply {
        addCategory(
            Intent.CATEGORY_LAUNCHER
        )
    }

    return pm
        .queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )
        .mapNotNull { info ->

            val packageName =
                info.activityInfo.packageName

            val name =
                info.loadLabel(pm)
                    ?.toString()
                    ?: return@mapNotNull null

            val icon =
                info.loadIcon(pm)
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
            it.name.lowercase(
                Locale.getDefault()
            )
        }
}

fun currentTime(): String {

    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date())
}
