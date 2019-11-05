package com.github.snuffix.round_navigation

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.FrameLayout
import com.treeline.circlenavigation.afterMeasured
import kotlin.math.min

class RoundNavigationTabsWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var arcRadius = 230f
    set(value) {
        field = value
        arcView?.setArcRadius(value)
        buttonsLayout?.setArcRadius(value)
    }

    private val horizontalPadding = 80
    private var buttonsLayout: RoundNavigationButtonsLayout? = null
    private var arcView: RoundNavigationTabsArcView? = null

    init {
        afterMeasured {
            (getContext() as? Activity)?.let { activity ->
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

                val viewSize =
                    min(displayMetrics.widthPixels, displayMetrics.heightPixels) - horizontalPadding

                translationY = (viewSize / 2).toFloat()

                layoutParams =
                    LayoutParams(viewSize, viewSize, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)

                val viewParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

                arcView = RoundNavigationTabsArcView(arcRadius, context)
                addView(arcView, viewParams)

                buttonsLayout = RoundNavigationButtonsLayout(arcRadius, context)
                addView(buttonsLayout, viewParams)

                buttonsLayout?.setOnButtonClickListener { buttonNumber ->
                    arcView?.selectTab(buttonNumber)
                }
            }
        }
    }
}
