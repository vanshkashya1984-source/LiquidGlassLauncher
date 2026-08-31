package com.vanshkashya.liquidglass

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LiquidGlassHome()
        }
    }
}

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: android.graphics.drawable.Drawable
)

@Composable
fun LiquidGlassHome() {

    val context = LocalContext.current
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    val backdrop = rememberLayerBackdrop()

    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var drawerOpen by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    var time by remember {
        mutableStateOf(currentTime())
    }

    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        apps = loadApps(launcherApps)
    }

    val filteredApps = remember(apps, search) {
        if (search.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(search, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // BACKGROUND
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFB9D8FF),
                            Color(0xFFEAF3FF),
                            Color(0xFFC8D9EA)
                        )
                    )
                )
        ) {

            Box(
                modifier = Modifier
                    .offset(x = (-60).dp, y = 120.dp)
                    .size(220.dp)
                    .background(
                        Color(0xFF8CC8FF).copy(alpha = 0.55f),
                        CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 70.dp, y = (-130).dp)
                    .size(260.dp)
                    .background(
                        Color(0xFFBDA7FF).copy(alpha = 0.45f),
                        CircleShape
                    )
            )
        }

        if (!drawerOpen) {

            // CLOCK
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 75.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = time,
                    fontSize = 60.sp,
                    color = Color(0xFF18202A)
                )

                Text(
                    text = SimpleDateFormat(
                        "EEEE, d MMMM",
                        Locale.getDefault()
                    ).format(Date()),
                    fontSize = 16.sp,
                    color = Color(0xFF4F5C6B)
                )
            }

            // SEARCH
            GlassSearch(
                backdrop = backdrop,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 205.dp)
                    .fillMaxWidth(0.88f)
                    .height(56.dp)
                    .clickable {
                        drawerOpen = true
                    }
            )

            // DOCK
            GlassDock(
                backdrop = backdrop,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
                    .fillMaxWidth(0.90f)
                    .height(82.dp),
                onDrawerClick = {
                    drawerOpen = true
                }
            )

        } else {

            // APP DRAWER
            AppDrawer(
                backdrop = backdrop,
                apps = filteredApps,
                search = search,
                onSearchChange = {
                    search = it
                },
                onClose = {
                    drawerOpen = false
                    search = ""
                },
                onLaunch = { app ->
                    launchApp(
                        launcherApps,
                        app
                    )
                }
            )
        }
    }
}

@Composable
fun GlassSearch(
    backdrop: com.kyant.backdrop.Backdrop,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { RoundedCornerShape(28.dp) },
            effects = {
                vibrancy()
                blur(8.dp.toPx())
                lens(
                    refractionHeight = 18.dp.toPx(),
                    refractionAmount = 24.dp.toPx(),
                    chromaticAberration = true
                )
            },
            onDrawSurface = {
                drawRect(
                    Color.White.copy(alpha = 0.20f)
                )
            }
        ),
        contentAlignment = Alignment.CenterStart
    ) {

        Text(
            text = "⌕   Search",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 16.sp,
            color = Color(0xFF3D4855)
        )
    }
}

@Composable
fun GlassDock(
    backdrop: com.kyant.backdrop.Backdrop,
    modifier: Modifier = Modifier,
    onDrawerClick: () -> Unit
) {

    Row(
        modifier = modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { RoundedCornerShape(41.dp) },
            effects = {
                vibrancy()
                blur(10.dp.toPx())
                lens(
                    refractionHeight = 22.dp.toPx(),
                    refractionAmount = 30.dp.toPx(),
                    chromaticAberration = true
                )
            },
            onDrawSurface = {
                drawRect(
                    Color.White.copy(alpha = 0.18f)
                )
            }
        ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        GlassDockIcon("☎")
        GlassDockIcon("◉")

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable {
                    onDrawerClick()
                }
                .background(
                    Color.White.copy(alpha = 0.22f),
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "⌘",
                fontSize = 24.sp,
                color = Color(0xFF202830)
            )
        }

        GlassDockIcon("◎")
    }
}

@Composable
fun GlassDockIcon(symbol: String) {

    Box(
        modifier = Modifier
            .size(54.dp)
            .background(
                Color.White.copy(alpha = 0.22f),
                RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = symbol,
            fontSize = 22.sp,
            color = Color(0xFF202830)
        )
    }
}

@Composable
fun AppDrawer(
    backdrop: com.kyant.backdrop.Backdrop,
    apps: List<AppInfo>,
    search: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit,
    onLaunch: (AppInfo) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 55.dp,
                start = 18.dp,
                end = 18.dp,
                bottom = 20.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Apps",
                modifier = Modifier.weight(1f),
                fontSize = 32.sp,
                color = Color(0xFF18202A)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.35f),
                        CircleShape
                    )
                    .clickable {
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "×",
                    fontSize = 28.sp,
                    color = Color(0xFF202830)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        GlassDrawerSearch(
            backdrop = backdrop,
            value = search,
            onValueChange = onSearchChange
        )

        Spacer(modifier = Modifier.height(18.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 30.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            items(
                items = apps,
                key = {
                    it.packageName + it.activityName
                }
            ) { app ->

                AppGridItem(
                    app = app,
                    onClick = {
                        onLaunch(app)
                    }
                )
            }
        }
    }
}

@Composable
fun GlassDrawerSearch(
    backdrop: com.kyant.backdrop.Backdrop,
    value: String,
    onValueChange: (String) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(26.dp) },
                effects = {
                    vibrancy()
                    blur(8.dp.toPx())
                    lens(
                        refractionHeight = 16.dp.toPx(),
                        refractionAmount = 20.dp.toPx(),
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        Color.White.copy(alpha = 0.20f)
                    )
                }
            )
    ) {

        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 18.dp,
                    vertical = 15.dp
                ),
            singleLine = true,
            decorationBox = { innerTextField ->

                if (value.isEmpty()) {
                    Text(
                        text = "⌕   Search apps",
                        color = Color(0xFF596574),
                        fontSize = 16.sp
                    )
                }

                innerTextField()
            }
        )
    }
}

@Composable
fun AppGridItem(
    app: AppInfo,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Color.White.copy(alpha = 0.24f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.foundation.Image(
                bitmap = app.icon
                    .toBitmap(
                        width = 128,
                        height = 128
                    )
                    .asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
            )
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = app.label,
            fontSize = 12.sp,
            color = Color(0xFF202830),
            maxLines = 1
        )
    }
}

fun loadApps(
    launcherApps: LauncherApps
): List<AppInfo> {

    return launcherApps
        .getActivityList(null, android.os.Process.myUserHandle())
        .map { info: LauncherActivityInfo ->

            AppInfo(
                label = info.label?.toString() ?: info.applicationInfo.packageName,
                packageName = info.applicationInfo.packageName,
                activityName = info.name,
                icon = info.getIcon(0)
            )
        }
        .distinctBy {
            it.packageName + it.activityName
        }
        .sortedBy {
            it.label.lowercase(Locale.getDefault())
        }
}

fun launchApp(
    launcherApps: LauncherApps,
    app: AppInfo
) {

    val component = android.content.ComponentName(
        app.packageName,
        app.activityName
    )

    launcherApps.startMainActivity(
        component,
        android.os.Process.myUserHandle(),
        null,
        null
    )
}

fun currentTime(): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date())
}
