package com.vanshkashya.liquidglass

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
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

    var drawerOpen by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(currentTime()) }

    var apps by remember {
        mutableStateOf(loadInstalledApps(context))
    }

    // Refresh apps whenever launcher resumes
    DisposableEffect(Unit) {
        val lifecycleObserver =
            androidx.lifecycle.LifecycleEventObserver { _, event ->

                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    apps = loadInstalledApps(context)
                }
            }

        val lifecycle = (context as ComponentActivity).lifecycle
        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    // Clock
    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            delay(1000)
        }
    }

    val filteredApps = remember(apps, searchText) {
        if (searchText.isBlank()) {
            apps
        } else {
            apps.filter {
                it.name.contains(searchText, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE5F0FF),
                        Color(0xFFF7FAFF),
                        Color(0xFFDCE8F5)
                    )
                )
            )
            .pointerInput(drawerOpen) {

                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->

                        if (dragAmount < -15) {
                            drawerOpen = true
                        }

                        if (dragAmount > 15) {
                            drawerOpen = false
                            searchText = ""
                        }
                    }
                )
            }
    ) {

        // Background blobs
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = 170.dp)
                .size(360.dp)
                .clip(RoundedCornerShape(180.dp))
                .background(Color(0x5590C8FF))
        )

        Box(
            modifier = Modifier
                .offset(x = 300.dp, y = 700.dp)
                .size(350.dp)
                .clip(RoundedCornerShape(175.dp))
                .background(Color(0x55B59AFF))
        )

        if (!drawerOpen) {

            HomeScreen(
                time = time,
                onOpenDrawer = {
                    drawerOpen = true
                }
            )

        } else {

            AppDrawer(
                apps = filteredApps,
                searchText = searchText,
                onSearchChange = {
                    searchText = it
                },
                onClose = {
                    drawerOpen = false
                    searchText = ""
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    time: String,
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
            fontWeight = FontWeight.Light,
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

        Spacer(modifier = Modifier.height(55.dp))

        // Search / app drawer button
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(58.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.45f))
                .pointerInput(Unit) {

                    detectVerticalDragGestures(
                        onVerticalDrag = { _, amount ->

                            if (amount < -10) {
                                onOpenDrawer()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {

            Text(
                text = "⌕   Search apps",
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 17.sp,
                color = Color(0xFF526171)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Swipe hint
        Text(
            text = "↑  Swipe up for apps",
            fontSize = 13.sp,
            color = Color(0xFF667585)
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassDock(
            onOpenDrawer = onOpenDrawer
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun GlassDock(
    onOpenDrawer: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .height(82.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(Color.White.copy(alpha = 0.50f)),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            DockButton(
                symbol = "⌕",
                onClick = onOpenDrawer
            )

            DockButton(
                symbol = "◉",
                onClick = onOpenDrawer
            )

            DockButton(
                symbol = "◎",
                onClick = onOpenDrawer
            )

            DockButton(
                symbol = "▦",
                onClick = onOpenDrawer
            )
        }
    }
}

@Composable
fun DockButton(
    symbol: String,
    onClick: () -> Unit
) {

    androidx.compose.foundation.clickable(
        onClick = onClick
    )

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(Color.White.copy(alpha = 0.48f)),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = symbol,
            fontSize = 24.sp,
            color = Color(0xFF18232D)
        )
    }
}

@Composable
fun AppDrawer(
    apps: List<InstalledApp>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 55.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Apps",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111A22)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${apps.size}",
                fontSize = 15.sp,
                color = Color(0xFF617080)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Search
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            singleLine = true,
            placeholder = {
                Text("Search installed apps")
            },
            leadingIcon = {
                Text(
                    text = "⌕",
                    fontSize = 22.sp
                )
            },
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.48f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.40f),
                focusedBorderColor = Color.White.copy(alpha = 0.8f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.55f)
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Swipe down to close",
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            fontSize = 12.sp,
            color = Color(0xFF687687)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
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
                    onClick = {

                        try {

                            val launchIntent =
                                android.content.pm.PackageManager
                                    .getLaunchIntentForPackage(
                                        app.packageName
                                    )

                            if (launchIntent != null) {

                                launchIntent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                )

                                val context =
                                    androidx.compose.ui.platform
                                        .LocalContext.current

                                context.startActivity(
                                    launchIntent
                                )
                            }

                        } catch (_: Exception) {
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppIcon(
    app: InstalledApp,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val bitmap = remember(app.packageName) {
            app.icon
                .toBitmap(
                    width = 120,
                    height = 120
                )
                .asImageBitmap()
        }

        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Color.White.copy(alpha = 0.40f)
                ),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = app.name,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.name,
            fontSize = 12.sp,
            color = Color(0xFF26313C),
            maxLines = 1
        )
    }
}

fun loadInstalledApps(
    context: android.content.Context
): List<InstalledApp> {

    val packageManager = context.packageManager

    val intent = Intent(
        Intent.ACTION_MAIN,
        null
    ).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfos =
        packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

    return resolveInfos
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
            it.name.lowercase()
        }
}

fun currentTime(): String {

    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date())
}
