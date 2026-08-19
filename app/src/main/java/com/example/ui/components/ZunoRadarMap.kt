package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Labour
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ZunoRadarMap(
    customerLat: Double,
    customerLng: Double,
    labours: List<Labour>,
    selectedLabour: Labour? = null,
    onLabourSelected: (Labour) -> Unit = {},
    modifier: Modifier = Modifier,
    isLiveTracking: Boolean = false,
    acceptedLabour: Labour? = null
) {
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val markerPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "marker_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE2E8F0))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
            .testTag("zuno_radar_map")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(labours, customerLat, customerLng) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val maxDelta = 0.035

                        for (labour in labours) {
                            val dLat = labour.latitude - customerLat
                            val dLng = labour.longitude - customerLng
                            val markerX = centerX + (dLng / maxDelta * (size.width * 0.42f)).toFloat()
                            val markerY = centerY - (dLat / maxDelta * (size.height * 0.42f)).toFloat()

                            val distanceSq = (tapOffset.x - markerX) * (tapOffset.x - markerX) +
                                    (tapOffset.y - markerY) * (tapOffset.y - markerY)
                            if (distanceSq < 36.dp.toPx() * 36.dp.toPx()) {
                                onLabourSelected(labour)
                                break
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f
            val maxRadius = minOf(width, height) * 0.44f

            // Clean dot matrix background
            val dotColor = Color(0xFF94A3B8).copy(alpha = 0.35f)
            val step = 20.dp.toPx()
            var x = step / 2
            while (x < width) {
                var y = step / 2
                while (y < height) {
                    drawCircle(dotColor, radius = 1.2f.dp.toPx(), center = Offset(x, y))
                    y += step
                }
                x += step
            }

            // Radar rings in subtle slate
            val ringColor = Color(0xFFCBD5E1)
            drawCircle(ringColor, radius = maxRadius * 0.33f, center = Offset(centerX, centerY), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))))
            drawCircle(ringColor, radius = maxRadius * 0.66f, center = Offset(centerX, centerY), style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))))
            drawCircle(ringColor, radius = maxRadius, center = Offset(centerX, centerY), style = Stroke(1.5f))

            // Pulse wave
            drawCircle(
                color = PolishPrimary.copy(alpha = (1f - pulseRadius) * 0.25f),
                radius = maxRadius * pulseRadius,
                center = Offset(centerX, centerY),
                style = Stroke(2.5f)
            )

            // Draw available labour markers: Green dots with 2px white border
            val maxDelta = 0.035
            labours.forEach { labour ->
                val dLat = labour.latitude - customerLat
                val dLng = labour.longitude - customerLng
                val markerX = (centerX + (dLng / maxDelta * (width * 0.42f)).toFloat()).coerceIn(20.dp.toPx(), width - 20.dp.toPx())
                val markerY = (centerY - (dLat / maxDelta * (height * 0.42f)).toFloat()).coerceIn(20.dp.toPx(), height - 20.dp.toPx())

                val isSelected = selectedLabour?.uid == labour.uid

                // Marker pulse glow
                drawCircle(
                    color = PolishGreen.copy(alpha = 0.25f),
                    radius = (if (isSelected) 16.dp.toPx() else 10.dp.toPx()) * markerPulse,
                    center = Offset(markerX, markerY)
                )

                // White border
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 9.dp.toPx() else 7.dp.toPx(),
                    center = Offset(markerX, markerY)
                )

                // Green Core
                drawCircle(
                    color = if (isSelected) PolishPrimary else PolishGreen,
                    radius = if (isSelected) 6.5f.dp.toPx() else 5.dp.toPx(),
                    center = Offset(markerX, markerY)
                )
            }

            // Live Tracking route if accepted
            if (isLiveTracking && acceptedLabour != null) {
                val dLat = acceptedLabour.latitude - customerLat
                val dLng = acceptedLabour.longitude - customerLng
                val labourX = (centerX + (dLng / maxDelta * (width * 0.42f)).toFloat()).coerceIn(24.dp.toPx(), width - 24.dp.toPx())
                val labourY = (centerY - (dLat / maxDelta * (height * 0.42f)).toFloat()).coerceIn(24.dp.toPx(), height - 24.dp.toPx())

                drawLine(
                    color = PolishPrimary,
                    start = Offset(centerX, centerY),
                    end = Offset(labourX, labourY),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                )

                // Worker moving indicator
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(labourX, labourY))
                drawCircle(color = PolishPrimary, radius = 7.dp.toPx(), center = Offset(labourX, labourY))
            }

            // Center User Location Marker: Large Blue Pin with 4px White Border & Shadow
            drawCircle(
                color = Color(0x33005AC1),
                radius = 18.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = PolishPrimary,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }
    }
}
