package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassmorphicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val gradientBrush = remember(isDark) {
        if (isDark) {
            // Sleek professional Slate-Teal-Dark gradient
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F172A), // Slate 900
                    Color(0xFF0B0F19), // Deep Navy Slate
                    Color(0xFF020617)  // Slate 950
                )
            )
        } else {
            // Soft premium Slate/Teal fluid gradient for high visibility
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8FAFC), // Slate 50
                    Color(0xFFF1F5F9), // Slate 100
                    Color(0xFFE2E8F0)  // Slate 200
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    padding: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    // Frosted-glass container configurations
    val containerColor = if (isDark) {
        Color(0x2E161E2E) // Semi-transparent sleek dark slate card
    } else {
        Color(0xF2FFFFFF) // Extremely clean white card with high opacity (95%) for supreme light mode text contrast!
    }

    val borderColor = if (isDark) {
        Color(0x222DD4BF) // Neon mint/teal trace glow
    } else {
        Color(0x220F766E) // Subtle professional corporate teal boundary
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier
                .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    }
}
