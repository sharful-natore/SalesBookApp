package com.example.ui.components

import androidx.compose.runtime.Composable

fun loc(isEnglish: Boolean, bn: String, en: String): String {
    return if (isEnglish) en else bn
}
