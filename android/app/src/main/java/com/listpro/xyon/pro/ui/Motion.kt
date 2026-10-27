package com.listpro.xyon.pro.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay

/** Curva de salida exponencial: arranca rápido y se posa suave. */
val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/** Vibraciones del sistema, respetando el ajuste de "respuesta táctil" del usuario. */
class Haptics(private val view: View) {
    private fun play(constant: Int) { view.performHapticFeedback(constant) }

    /** Elegir algo pequeño: un chip, una pestaña de subcategoría. */
    fun tick() = play(HapticFeedbackConstants.CLOCK_TICK)

    /** Pulsar un botón o cambiar de pestaña. */
    fun tap() = play(HapticFeedbackConstants.VIRTUAL_KEY)

    /** Algo entra en la cesta. */
    fun add() = play(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CONTEXT_CLICK)

    fun remove() = play(HapticFeedbackConstants.CLOCK_TICK)

    fun success() = play(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS)

    fun warn() = play(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS)
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

/**
 * Entrada escalonada: sube y aparece con un pequeño retraso según su posición.
 * Solo las primeras posiciones animan, para que el scroll largo no se llene de movimiento.
 */
@Composable
fun Modifier.appear(index: Int): Modifier {
    if (index > 10) return this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 40L)
        progress.animateTo(1f, tween(520, easing = EaseOutExpo))
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 36f
        val s = 0.97f + 0.03f * progress.value
        scaleX = s
        scaleY = s
    }
}

/** Se hunde un poco al pulsar, con muelle al soltar. */
@Composable
fun Modifier.pressScale(source: MutableInteractionSource, pressed: Float = 0.965f): Modifier {
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) pressed else 1f, spring(dampingRatio = 0.55f, stiffness = 600f), label = "press")
    return graphicsLayer { scaleX = scale; scaleY = scale }
}
