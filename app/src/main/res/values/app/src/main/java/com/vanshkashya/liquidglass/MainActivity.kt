package com.vanshkashya.liquidglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

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

    var time by remember { mutableStateOf(currentTime()) }

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
                        Color(0xFFE8EEF5),
                        Color(0xFFF8FAFC),
                        Color(0xFFDCE5EF)
                    )
                )
            )
    ) {

        // Large clock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = time,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF18202A)
            )

            Text(
                text = SimpleDateFormat(
                    "EEEE, d MMMM",
                    Locale.getDefault()
                ).format(Date()),
                fontSize = 16.sp,
                color = Color(0xFF596574)
            )
        }

        // Glass search bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 230.dp)
                .fillMaxWidth(0.88f)
                .height(52.dp)
                .background(
                    Color.White.copy(alpha = 0.42f),
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {

            Text(
                text = "⌕   Search",
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 16.sp,
                color = Color(0xFF5E6875)
            )
        }

        // Glass dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .fillMaxWidth(0.88f)
                .height(76.dp)
                .background(
                    Color.White.copy(alpha = 0.48f),
                    RoundedCornerShape(38.dp)
                )
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                GlassIcon("☎")
                GlassIcon("◉")
                GlassIcon("◎")
                GlassIcon("▣")
            }
        }
    }
}

@Composable
fun GlassIcon(symbol: String) {

    Box(
        modifier = Modifier
            .size(52.dp)
            .background(
                Color.White.copy(alpha = 0.55f),
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

fun currentTime(): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date())
}
