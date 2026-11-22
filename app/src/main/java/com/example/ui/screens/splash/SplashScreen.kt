package com.example.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.VyroPrismLogo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        visible = true
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1250, easing = FastOutSlowInEasing)
        )
        ready = true
        delay(300L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        VyroElectricPurple.copy(alpha = 0.20f),
                        VyroNeonBlue.copy(alpha = 0.08f),
                        VyroDeepBlack
                    ),
                    radius = 900f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.88f, animationSpec = tween(550))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VyroPrismLogo(
                    size = 112.dp,
                    animateGlow = true
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.brand_vyro),
                        style = MaterialTheme.typography.displayLarge.copy(
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VyroTextPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.brand_studio),
                        style = MaterialTheme.typography.displayLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = VyroNeonBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.splash_tagline),
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                    color = VyroTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                LinearProgressIndicator(
                    progress = { progress.value },
                    color = VyroElectricPurple,
                    trackColor = VyroGraphiteElevated,
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (ready) {
                        stringResource(R.string.splash_status_ready)
                    } else {
                        stringResource(R.string.splash_status_init)
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ready) VyroNeonBlue else VyroTextSecondary
                )
            }
        }
    }
}
