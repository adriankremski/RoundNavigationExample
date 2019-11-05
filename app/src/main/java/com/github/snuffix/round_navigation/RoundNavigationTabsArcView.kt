package com.github.snuffix.round_navigation

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.graphics.withRotation
import java.lang.StrictMath.pow
import kotlin.math.*


class RoundNavigationTabsArcView @JvmOverloads constructor(
    private var arcRadius: Float = 230f,
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val startAngle = 160f
    private val sweepAngle = 80f
    private val tabRotation = 70f
    private val arcCornersRadius = 12f

    private val tabIndicatorPaint = Paint()
    private val tabIndicatorStartCornersPaint = Paint()
    private val tabIndicatorEndCornersPaint = Paint()
    private val backgroundPaint = Paint()

    private val viewBoundsRect = RectF()

    private val tabIndicatorStartColor = Color.parseColor("#643cbf")
    private val tabIndicatorEndColor = Color.parseColor("#7646cf")

    private val backgroundStartColor = Color.parseColor("#cfcfcf")
    private val backgroundEndColor = Color.parseColor("#f0f0f0")

    private var tabIndicatorGradient: RadialGradient? = null
    private var backgroundGradient: RadialGradient? = null

    init {
        initPaints()
    }

    private fun initPaints() {
        tabIndicatorPaint.strokeWidth = arcRadius
        tabIndicatorPaint.isAntiAlias = true
        tabIndicatorPaint.strokeCap = Paint.Cap.BUTT
        tabIndicatorPaint.pathEffect = CornerPathEffect(arcRadius);
        tabIndicatorPaint.style = Paint.Style.STROKE

        backgroundPaint.strokeWidth = arcRadius
        backgroundPaint.isAntiAlias = true
        backgroundPaint.style = Paint.Style.STROKE

        tabIndicatorStartCornersPaint.strokeWidth = 10f
        tabIndicatorStartCornersPaint.isAntiAlias = true
        tabIndicatorStartCornersPaint.style = Paint.Style.FILL_AND_STROKE

        tabIndicatorEndCornersPaint.strokeWidth = 10f
        tabIndicatorEndCornersPaint.isAntiAlias = true
        tabIndicatorEndCornersPaint.style = Paint.Style.FILL_AND_STROKE

        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setArcRadius(value: Float) {
        arcRadius = value
        initPaints()
        invalidate()
    }

    fun selectTab(tabNumber: Int) {
        animate().rotation((tabRotation * tabNumber)).apply {
            duration = 500
            interpolator = OvershootInterpolator()
            start()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        viewBoundsRect.set(arcRadius / 2, arcRadius / 2, width - arcRadius / 2, height - arcRadius / 2)
        backgroundGradient = backgroundGradient ?: createViewRadialGradient(backgroundStartColor, backgroundEndColor)
        tabIndicatorGradient = tabIndicatorGradient ?: createViewRadialGradient(tabIndicatorStartColor, tabIndicatorEndColor)

        backgroundPaint.shader = backgroundGradient
        tabIndicatorPaint.shader = tabIndicatorGradient
        tabIndicatorStartCornersPaint.shader = tabIndicatorGradient


        canvas.drawArc(viewBoundsRect, -90.0f, 360.0f, false, backgroundPaint)
        canvas.drawArc(viewBoundsRect, startAngle, sweepAngle, false, tabIndicatorPaint)

        drawArcStartCap(canvas)
        drawArcEndCap(canvas)
    }

    private fun createViewRadialGradient(startColor: Int, endColor: Int) = RadialGradient(
        width / 2f, height / 2f,
        width / 2f, startColor, endColor, Shader.TileMode.MIRROR
    )

    private fun drawArcStartCap(canvas: Canvas) {
        val arcPoints = calculateArcCornerPoints(startAngle)
        canvas.withRotation(arcPoints.angleRelativeToHorizontalAxis(), arcPoints.startX, arcPoints.startY) {
            val cornersRect = RectF(arcPoints.startX + 5, arcPoints.startY - 10f, arcPoints.startX + arcRadius - 5, arcPoints.startY + 20)

            val distanceToCenter = Point(arcPoints.startX.toInt(), arcPoints.startY.toInt()) distanceTo Point(width / 2, height / 2)

            val rotatedGradientCenterX = arcPoints.startX + distanceToCenter
            val rotatedGradientCenterY = arcPoints.startY

            val gradient = RadialGradient(
                rotatedGradientCenterX, rotatedGradientCenterY,
                width / 2f, tabIndicatorStartColor, tabIndicatorEndColor, Shader.TileMode.MIRROR
            )

            tabIndicatorStartCornersPaint.shader = gradient
            canvas.drawRoundRect(cornersRect, arcCornersRadius, arcCornersRadius, tabIndicatorStartCornersPaint)
        }
    }

    private fun calculateArcCornerPoints(arcAngle: Float): ArcCornerPoints {
        val angleInRadians = Math.toRadians((arcAngle).toDouble())

        return ArcCornerPoints(
            startX = (width / 2 + ((width / 2) * cos(angleInRadians))).toFloat(),
            startY = (width / 2 + ((width / 2) * sin(angleInRadians))).toFloat(),
            endX = (width / 2 + ((width / 2 - arcRadius) * cos(angleInRadians))).toFloat(),
            endY = (width / 2 + ((width / 2 - arcRadius) * sin(angleInRadians))).toFloat()
        )
    }

    private fun drawArcEndCap(canvas: Canvas) {
        val arcPoints = calculateArcCornerPoints(startAngle + 80)
        canvas.withRotation(arcPoints.angleRelativeToHorizontalAxis(), arcPoints.startX, arcPoints.startY) {
            val cornersRect = RectF(arcPoints.startX + 5, arcPoints.startY - 10f, arcPoints.startX + arcRadius - 5, arcPoints.startY + 20)

            val distanceToCenter = Point(arcPoints.startX.toInt(), arcPoints.startY.toInt()) distanceTo Point(width / 2, height / 2)

            val rotatedGradientCenterX = arcPoints.startX + distanceToCenter
            val rotatedGradientCenterY = arcPoints.startY

            val gradient = RadialGradient(
                rotatedGradientCenterX, rotatedGradientCenterY,
                width / 2f, tabIndicatorStartColor, tabIndicatorEndColor, Shader.TileMode.MIRROR
            )

            tabIndicatorEndCornersPaint.shader = gradient
            canvas.drawRoundRect(cornersRect, arcCornersRadius, arcCornersRadius, tabIndicatorEndCornersPaint)
        }
    }

    private data class ArcCornerPoints(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float
    ) {

        fun angleRelativeToHorizontalAxis() = (atan2(endY - startY, endX - startX) * 180 / PI).toFloat()
    }

    private infix fun Point.distanceTo(anotherPoint: Point): Float {
        return sqrt(pow((this.x - anotherPoint.x).toDouble(), 2.0) + pow((this.y - anotherPoint.y).toDouble(), 2.0)).toFloat()
    }
}
