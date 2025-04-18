package com.ptit.common.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import com.bumptech.glide.integration.compose.DrawPainter
import com.bumptech.glide.integration.compose.Transition
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class MyCrossFade(
    private val animationSpec: AnimationSpec<Float>
) : Transition.Factory {

    override fun build(): Transition = MyCrossFadeImpl(animationSpec)

    companion object : Transition.Factory {
        override fun build(): Transition =
            MyCrossFadeImpl(animationSpec = tween(1000))
    }

    override fun equals(other: Any?): Boolean {
        if (other is MyCrossFade) {
            return animationSpec == other.animationSpec
        }
        return false
    }

    override fun hashCode(): Int {
        return animationSpec.hashCode()
    }
}

internal class MyCrossFadeImpl(
    private val animationSpec: AnimationSpec<Float>
) : Transition {

    private companion object {
        const val OPAQUE_ALPHA = 1f
    }

    private val animatable: Animatable<Float, AnimationVector1D> =
        Animatable(0f, Float.VectorConverter, OPAQUE_ALPHA)

    override suspend fun transition(invalidate: () -> Unit) {
        try {
            invalidate()
            animatable.animateTo(OPAQUE_ALPHA, animationSpec)
        } finally {
            withContext(NonCancellable) {
                animatable.snapTo(OPAQUE_ALPHA)
            }
            invalidate()
        }
    }

    override suspend fun stop() {
        animatable.stop()
    }

    override val drawPlaceholder: DrawPainter = { painter, size, alpha, colorFilter ->
        with(painter) {
            draw(size, (OPAQUE_ALPHA - animatable.value) * alpha, colorFilter)
        }
    }

    override val drawCurrent: DrawPainter = { painter, size, alpha, colorFilter ->
        with(painter) {
            draw(size, animatable.value * alpha, colorFilter)
        }
    }
}