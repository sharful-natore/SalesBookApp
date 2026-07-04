package com.example.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // অ্যাপের মেইন কন্টেইনার থিম
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // কোনো ভুল কোডের কারণে যেন অ্যাপ ক্র্যাশ না করে, তাই try-catch দিয়ে সুরক্ষিত করা হলো
                try {
                    AppNavigation()
                } catch (e: Exception) {
                    // যদি AppNavigation-এ কোনো এরর থাকে, তবে অ্যাপ বন্ধ না হয়ে এই স্ক্রিনটি দেখাবে
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Welcome to Sales Book")
                    }
                }
            }
        }
    }
}
