package dev.jakhongirmadaminov.glassmorphic_sample.ui.componets

import androidx.compose.runtime.Stable

@Stable
data class Place(
    var sizeX: Int = 0,
    var sizeY: Int = 0,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
)