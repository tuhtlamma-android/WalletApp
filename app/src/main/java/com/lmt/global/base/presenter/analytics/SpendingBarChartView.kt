package com.lmt.global.base.presenter.analytics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.lmt.global.base.R
import kotlin.math.max

class SpendingBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.wallet_slate)
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            10f,
            resources.displayMetrics
        )
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.sora_regular)
    }
    private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.wallet_alice_blue)
        strokeWidth = dp(1f)
    }
    private val regularBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.wallet_purple_mid)
    }
    private val peakBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.wallet_mango)
    }
    private val emptyBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.wallet_purple_soft)
    }
    private val reusableBar = RectF()

    private var points = emptyList<AnalyticsTrendPoint>()

    init {
        minimumHeight = dp(DEFAULT_HEIGHT_DP).toInt()
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun setPoints(points: List<AnalyticsTrendPoint>) {
        this.points = points
        contentDescription = resources.getQuantityString(
            R.plurals.analytics_chart_description,
            points.size,
            points.size
        )
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = max(
            suggestedMinimumWidth,
            (paddingLeft + paddingRight + points.size * MIN_SLOT_WIDTH_DP * density).toInt()
        )
        val desiredHeight = max(suggestedMinimumHeight, dp(DEFAULT_HEIGHT_DP).toInt())
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val chartTop = paddingTop + dp(CHART_TOP_PADDING_DP)
        val baseline = height - paddingBottom - dp(LABEL_AREA_HEIGHT_DP)
        val chartHeight = (baseline - chartTop).coerceAtLeast(dp(1f))
        val availableWidth = (width - paddingLeft - paddingRight).toFloat()
        val slotWidth = availableWidth / points.size
        val barWidth = minOf(dp(MAX_BAR_WIDTH_DP), slotWidth * BAR_WIDTH_RATIO)
        val maxAmount = points.maxOfOrNull(AnalyticsTrendPoint::amountMinor) ?: 0L
        val peakIndex = points.indexOfFirst { it.amountMinor == maxAmount && maxAmount > 0L }

        canvas.drawLine(
            paddingLeft.toFloat(),
            baseline,
            (width - paddingRight).toFloat(),
            baseline,
            baselinePaint
        )

        points.forEachIndexed { index, point ->
            val centerX = paddingLeft + slotWidth * index + slotWidth / 2f
            val barHeight = if (point.amountMinor <= 0L || maxAmount <= 0L) {
                dp(EMPTY_BAR_HEIGHT_DP)
            } else {
                max(
                    dp(MIN_ACTIVE_BAR_HEIGHT_DP),
                    chartHeight * point.amountMinor.toFloat() / maxAmount.toFloat()
                )
            }
            val paint = when {
                point.amountMinor <= 0L -> emptyBarPaint
                index == peakIndex -> peakBarPaint
                else -> regularBarPaint
            }
            reusableBar.set(
                centerX - barWidth / 2f,
                baseline - barHeight,
                centerX + barWidth / 2f,
                baseline
            )
            canvas.drawRoundRect(reusableBar, dp(BAR_RADIUS_DP), dp(BAR_RADIUS_DP), paint)
            canvas.drawText(
                point.label,
                centerX,
                baseline + dp(LABEL_BASELINE_OFFSET_DP),
                labelPaint
            )
        }
    }

    private fun dp(value: Float): Float = value * density

    private companion object {
        const val DEFAULT_HEIGHT_DP = 168f
        const val MIN_SLOT_WIDTH_DP = 48f
        const val MAX_BAR_WIDTH_DP = 24f
        const val BAR_WIDTH_RATIO = 0.48f
        const val CHART_TOP_PADDING_DP = 8f
        const val LABEL_AREA_HEIGHT_DP = 28f
        const val LABEL_BASELINE_OFFSET_DP = 19f
        const val EMPTY_BAR_HEIGHT_DP = 3f
        const val MIN_ACTIVE_BAR_HEIGHT_DP = 8f
        const val BAR_RADIUS_DP = 5f
    }
}
