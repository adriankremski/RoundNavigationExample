package com.github.snuffix.round_navigation

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.treeline.circlenavigation.afterMeasured
import com.treeline.circlenavigation.animateColor
import com.treeline.circlenavigation.getChildViews
import kotlin.math.cos
import kotlin.math.sin


@SuppressLint("ViewConstructor")
class RoundNavigationButtonsLayout @JvmOverloads constructor(
    private var arcRadius: Float,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val arcStartAngles = listOf(160f, 230f, 300f)
    private var buttonSize = (arcRadius / 2).toInt()
    private val buttonPadding = 60
    private val buttonImageResources = listOf(
        R.drawable.ic_donut_large_black_24dp,
        R.drawable.ic_whatshot_24dp, R.drawable.ic_favorite_24dp
    )
    private val buttonUnselectedColor = Color.parseColor("#d5d3e3")
    private val buttonSelectedColor = Color.WHITE

    private var animator: ValueAnimator? = null
    private val animationDuration = 500.toLong()

    private var buttonClickListener: ((Int) -> Unit)? = null

    private var selectedButtonNumber = 0

    init {
        afterMeasured {
            initButtons()
        }
    }

    private fun initButtons() {
        buttonSize = (arcRadius * 7/12).toInt()
        arcStartAngles.forEachIndexed { index, angle ->
            createButtonImageView(index, angle)
        }
    }

    fun setArcRadius(value: Float) {
        arcRadius = value
        removeAllViews()
        initButtons()
        invalidate()
    }

    private fun createButtonImageView(buttonNumber: Int, startAngle: Float) {
        val angleInRadians = Math.toRadians((startAngle + 40).toDouble())
        val centerX = width / 2 + ((width / 2 - arcRadius / 2) * cos(angleInRadians))
        val centerY = width / 2 + ((width / 2 - arcRadius / 2) * sin(angleInRadians))

        val buttonImageView = ImageView(context)
        buttonImageView.setOnClickListener { buttonClickListener?.invoke(buttonNumber) }
        buttonImageView.setImageResource(buttonImageResources[buttonNumber])

        val buttonColor = if (buttonNumber == selectedButtonNumber) {
            buttonSelectedColor
        } else {
            buttonUnselectedColor
        }

        buttonImageView.setColorFilter(buttonColor)
        buttonImageView.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding)
        val width = buttonSize + buttonPadding * 2
        val height = buttonSize + buttonPadding * 2
        val params = LayoutParams(width, height)
        params.leftMargin = (centerX).toInt() - width / 2
        params.topMargin = (centerY).toInt() - height / 2
        addView(buttonImageView, params)
    }

    fun setOnButtonClickListener(block: (Int) -> Unit) {
        buttonClickListener = { buttonNumber ->
            animator?.cancel()
            animator = null

            selectedButtonNumber = buttonNumber

            getChildViews<ImageView>().forEachIndexed { index, button ->
                if (index != buttonNumber) {
                    button.setColorFilter(buttonUnselectedColor)
                } else {
                    animator = button.animateColor(
                        buttonUnselectedColor,
                        buttonSelectedColor,
                        animationDuration
                    )
                }
            }

            block(buttonNumber)
        }
    }
}
