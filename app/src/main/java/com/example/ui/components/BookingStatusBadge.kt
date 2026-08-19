package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.ui.theme.*

@Composable
fun BookingStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val (bgColor, textColor, dotColor, displayText) = when (status.lowercase()) {
        Booking.STATUS_PENDING -> {
            Quadruple(
                ZunoAmberLight,
                Color(0xFF92400E),
                ZunoAmber,
                "Waiting for Labour"
            )
        }
        Booking.STATUS_ACCEPTED -> {
            Quadruple(
                ZunoEmeraldLight,
                Color(0xFF065F46),
                ZunoEmerald,
                "Booking Accepted"
            )
        }
        Booking.STATUS_REJECTED -> {
            Quadruple(
                ZunoRoseLight,
                Color(0xFF991B1B),
                ZunoRose,
                "Booking Rejected"
            )
        }
        Booking.STATUS_CANCELLED -> {
            Quadruple(
                Color(0xFFF1F5F9),
                Color(0xFF475569),
                Color(0xFF94A3B8),
                "Cancelled"
            )
        }
        Booking.STATUS_COMPLETED -> {
            Quadruple(
                ZunoIce,
                ZunoPrimaryDark,
                ZunoPrimary,
                "Completed"
            )
        }
        else -> {
            Quadruple(
                Color(0xFFF1F5F9),
                ZunoTextSecondary,
                Color(0xFF94A3B8),
                status.replaceFirstChar { it.uppercase() }
            )
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (status.equals(Booking.STATUS_PENDING, ignoreCase = true))
                            dotColor.copy(alpha = alpha)
                        else dotColor
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = displayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
