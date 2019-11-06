package com.github.snuffix.round_navigation

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.graphics.withRotation
import java.lang.StrictMath.pow
import kotlin.math.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.R.attr.bitmap
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable






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
    private val shadowGradientPaint = Paint()

    private val viewBoundsRect = RectF()

    private val tabIndicatorStartColor = Color.parseColor("#643cbf")
    private val tabIndicatorEndColor = Color.parseColor("#7646cf")

    private val backgroundStartColor = Color.parseColor("#cfcfcf")
    private val backgroundEndColor = Color.parseColor("#f0f0f0")

    private val shadowGradientStartColor = Color.parseColor("#44FFFFFF")
    private val shadowGradientEndColor = Color.parseColor("#00FFFFFF")

    private var tabIndicatorGradient: RadialGradient? = null
    private var backgroundGradient: RadialGradient? = null
    private var shadowGradient: RadialGradient? = null
    private var gradientBitmap: Bitmap? = null

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

        shadowGradientPaint.strokeWidth = 10f
        shadowGradientPaint.isAntiAlias = true
        shadowGradientPaint.style = Paint.Style.FILL_AND_STROKE

        setLayerType(LAYER_TYPE_HARDWARE, null)

        gradientBitmap =  drawableToBitmap(getResources().getDrawable(R.drawable.ic_favorite_24dp, null))
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(100, (arcRadius/2).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
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

        val centerCoordinatesExtraDistance = 40f
        val startShadowCenterCoordinates = calculateArcCornerPoints(startAngle + sweepAngle * 2/3)
        val endShadowCenterCoordinates = calculateArcCornerPoints(startAngle + sweepAngle / 2)

        drawIconGradientShadow(
            canvas,
            gradientCenterX = startShadowCenterCoordinates.startX - centerCoordinatesExtraDistance,
            gradientCenterY = startShadowCenterCoordinates.startY - centerCoordinatesExtraDistance
        )

        drawIconGradientShadow(
            canvas,
            gradientCenterX = endShadowCenterCoordinates.endX + centerCoordinatesExtraDistance,
            gradientCenterY = endShadowCenterCoordinates.endY - centerCoordinatesExtraDistance
        )

    }

    private fun drawIconGradientShadow(
        canvas: Canvas,
        gradientCenterX: Float,
        gradientCenterY: Float
    ) {


        val gradient = RadialGradient(
            gradientCenterX,
            gradientCenterY,
            arcRadius * 3/4,
            shadowGradientStartColor,
            shadowGradientEndColor,
            Shader.TileMode.CLAMP
        )

        shadowGradientPaint.shader = gradient
        canvas.drawCircle(gradientCenterX, gradientCenterY, arcRadius , shadowGradientPaint)
    }

    private fun createViewRadialGradient(startColor: Int, endColor: Int) = RadialGradient(
        width / 2f, height / 2f,
        width / 2f, startColor, endColor, Shader.TileMode.MIRROR
    )

    private fun drawArcStartCap(canvas: Canvas) {
        val arcPoints = calculateArcCornerPoints(startAngle)
        canvas.withRotation(
            arcPoints.angleRelativeToHorizontalAxis(),
            arcPoints.startX,
            arcPoints.startY
        ) {
            val cornersRect = RectF(
                arcPoints.startX + 5,
                arcPoints.startY - 10f,
                arcPoints.startX + arcRadius - 5,
                arcPoints.startY + 20
            )

            val distanceToCenter = Point(
                arcPoints.startX.toInt(),
                arcPoints.startY.toInt()
            ) distanceTo Point(width / 2, height / 2)

            val rotatedGradientCenterX = arcPoints.startX + distanceToCenter
            val rotatedGradientCenterY = arcPoints.startY

            val gradient = RadialGradient(
                rotatedGradientCenterX, rotatedGradientCenterY,
                width / 2f, tabIndicatorStartColor, tabIndicatorEndColor, Shader.TileMode.MIRROR
            )

            tabIndicatorStartCornersPaint.shader = gradient
            canvas.drawRoundRect(
                cornersRect,
                arcCornersRadius,
                arcCornersRadius,
                tabIndicatorStartCornersPaint
            )
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
        canvas.withRotation(
            arcPoints.angleRelativeToHorizontalAxis(),
            arcPoints.startX,
            arcPoints.startY
        ) {
            val cornersRect = RectF(
                arcPoints.startX + 5,
                arcPoints.startY - 10f,
                arcPoints.startX + arcRadius - 5,
                arcPoints.startY + 20
            )

            val distanceToCenter = Point(
                arcPoints.startX.toInt(),
                arcPoints.startY.toInt()
            ) distanceTo Point(width / 2, height / 2)

            val rotatedGradientCenterX = arcPoints.startX + distanceToCenter
            val rotatedGradientCenterY = arcPoints.startY

            val gradient = RadialGradient(
                rotatedGradientCenterX, rotatedGradientCenterY,
                width / 2f, tabIndicatorStartColor, tabIndicatorEndColor, Shader.TileMode.MIRROR
            )

            tabIndicatorEndCornersPaint.shader = gradient
            canvas.drawRoundRect(
                cornersRect,
                arcCornersRadius,
                arcCornersRadius,
                tabIndicatorEndCornersPaint
            )
        }
    }

    private data class ArcCornerPoints(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float
    ) {

        fun angleRelativeToHorizontalAxis() =
            (atan2(endY - startY, endX - startX) * 180 / PI).toFloat()
    }

    private infix fun Point.distanceTo(anotherPoint: Point): Float {
        return sqrt(
            pow(
                (this.x - anotherPoint.x).toDouble(),
                2.0
            ) + pow((this.y - anotherPoint.y).toDouble(), 2.0)
        ).toFloat()
    }
}
