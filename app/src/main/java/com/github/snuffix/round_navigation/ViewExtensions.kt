package com.treeline.circlenavigation

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView

fun ImageView.animateColor(startColor: Int, endColor: Int, animationDuration: Long): ValueAnimator {
    return ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).apply {
        duration = animationDuration
        addUpdateListener { animator ->
            this@animateColor.setColorFilter(
                animator.animatedValue as Int,
                PorterDuff.Mode.SRC_ATOP
            )
        }
        start()
    }
}

fun View.afterMeasured(block: View.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            block()
        }
    })
}

inline fun <reified T> ViewGroup.getChildViews(): List<T> {
    val views = mutableListOf<T>()

    for (i in 0 until childCount) {
        val view = getChildAt(i)

        if (view is T) {
            views.add(getChildAt(i) as T)
        }
    }

    return views
}
