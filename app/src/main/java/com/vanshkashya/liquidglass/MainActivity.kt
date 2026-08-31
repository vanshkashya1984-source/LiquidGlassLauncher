package com.vanshkashya.liquidglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun LiquidGlassHome() {

    val backdrop = rememberLayerBackdrop()

    var time by remember {
        mutableStateOf(currentTime())
    }

    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            delay(1000)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Background source for the glass
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

            // Decorative background shapes
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

        // Clock
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

        // Liquid Glass Search
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 205.dp)
                .fillMaxWidth(0.88f)
                .height(56.dp)
                .drawBackdrop(
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

        // Liquid Glass Dock
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .fillMaxWidth(0.90f)
                .height(82.dp)
                .drawBackdrop(
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

            LiquidGlassIcon("☎")
            LiquidGlassIcon("◉")
            LiquidGlassIcon("◎")
            LiquidGlassIcon("▣")
        }
    }
}

@Composable
fun LiquidGlassIcon(symbol: String) {

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .drawBackdrop(
                backdrop = rememberLayerBackdrop(),
                shape = { RoundedCornerShape(18.dp) },
                effects = {
                    lens(
                        refractionHeight = 10.dp.toPx(),
                        refractionAmount = 14.dp.toPx(),
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        Color.White.copy(alpha = 0.22f)
                    )
                }
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

fun currentTime(): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date())
}
