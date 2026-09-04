package com.lmt.global.base.common

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import java.util.WeakHashMap
import kotlin.math.roundToInt


object ScreenRenderer {

    private const val DESIGN_WIDTH = 360f
    private const val DESIGN_STATUS_BAR_HEIGHT = 44f
    private const val DESIGN_CONTENT_HEIGHT = 800f - DESIGN_STATUS_BAR_HEIGHT

    private data class Bounds(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    )

    private data class PositionedView(val view: View, val bounds: Bounds)

    private data class RenderState(
        val frame: Int,
        val positionedViews: MutableList<PositionedView>,
        val layoutListener: View.OnLayoutChangeListener
    )

    private val renderStates = WeakHashMap<FrameLayout, RenderState>()

    data class Hotspot(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val description: String,
        val action: () -> Unit
    )

    fun render(
        container: FrameLayout,
        @DrawableRes frame: Int,
        hotspots: List<Hotspot> = emptyList()
    ) {
        renderStates.remove(container)?.let {
            container.removeOnLayoutChangeListener(it.layoutListener)
        }
        container.removeAllViews()
        container.tag = frame

        container.addView(
            ImageView(container.context).apply {
                setImageResource(frame)
                scaleType = ImageView.ScaleType.FIT_XY
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val positionedViews = hotspots.mapTo(mutableListOf()) { hotspot ->
            val hotspotView = View(container.context).apply {
                contentDescription = hotspot.description
                isClickable = true
                isFocusable = true
                setOnClickListener { hotspot.action() }
            }
            container.addView(hotspotView, FrameLayout.LayoutParams(0, 0))
            PositionedView(
                hotspotView,
                Bounds(hotspot.left, hotspot.top, hotspot.right, hotspot.bottom)
            )
        }

        val listener = View.OnLayoutChangeListener {
                _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val sizeChanged = right - left != oldRight - oldLeft ||
                bottom - top != oldBottom - oldTop
            if (sizeChanged) layoutPositionedViews(container, frame, positionedViews)
        }
        renderStates[container] = RenderState(frame, positionedViews, listener)
        container.addOnLayoutChangeListener(listener)
        container.post { layoutPositionedViews(container, frame, positionedViews) }
    }

    fun addOverlay(
        container: FrameLayout,
        @DrawableRes expectedFrame: Int,
        view: View,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val state = renderStates[container]
        if (state == null || state.frame != expectedFrame || container.tag != expectedFrame) return

        val positionedView = PositionedView(view, Bounds(left, top, right, bottom))
        state.positionedViews.add(positionedView)
        container.addView(view, FrameLayout.LayoutParams(0, 0))
        container.post {
            if (renderStates[container] === state) {
                layoutPositionedView(container, positionedView)
            }
        }
    }

    private fun layoutPositionedViews(
        container: FrameLayout,
        expectedFrame: Int,
        positionedViews: List<PositionedView>
    ) {
        if (container.tag != expectedFrame || container.width == 0 || container.height == 0) return
        positionedViews.forEach { layoutPositionedView(container, it) }
    }

    private fun layoutPositionedView(container: FrameLayout, positionedView: PositionedView) {
        if (container.width == 0 || container.height == 0) return

        val bounds = positionedView.bounds
        val scaleX = container.width / DESIGN_WIDTH
        val scaleY = container.height / DESIGN_CONTENT_HEIGHT
        val contentTop = (bounds.top - DESIGN_STATUS_BAR_HEIGHT)
            .coerceIn(0f, DESIGN_CONTENT_HEIGHT)
        val contentBottom = (bounds.bottom - DESIGN_STATUS_BAR_HEIGHT)
            .coerceIn(0f, DESIGN_CONTENT_HEIGHT)
        positionedView.view.layoutParams = FrameLayout.LayoutParams(
            ((bounds.right - bounds.left) * scaleX).roundToInt(),
            ((contentBottom - contentTop) * scaleY).roundToInt()
        ).apply {
            leftMargin = (bounds.left * scaleX).roundToInt()
            topMargin = (contentTop * scaleY).roundToInt()
        }
    }
}
