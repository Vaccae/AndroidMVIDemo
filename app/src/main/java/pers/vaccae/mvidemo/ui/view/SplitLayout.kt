package pers.vaccae.mvidemo.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintAttribute.setAttributes
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo
import pers.vaccae.mvidemo.R

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 15:07
 * 功能模块说明：
 */
class SplitLayout :FrameLayout{

    private var windowLayoutInfo: WindowLayoutInfo? = null
    private var startViewId = 0
    private var endViewId = 0

    private var lastWidthMeasureSpec: Int = 0
    private var lastHeightMeasureSpec: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setAttributes(attrs)
    }

    private fun setAttributes(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SplitLayout, 0, 0).apply {
            try {
                startViewId = getResourceId(R.styleable.SplitLayout_startViewId, 0)
                endViewId = getResourceId(R.styleable.SplitLayout_endViewId, 0)
            } finally {
                recycle()
            }
        }
    }


    fun updateWindowLayout(windowLayoutInfo: WindowLayoutInfo) {
        this.windowLayoutInfo = windowLayoutInfo
        requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val startView = findStartView()
        val endView = findEndView()
        val splitPositions = splitViewPositions(startView, endView)

        if (startView != null && endView != null && splitPositions != null) {
            val startPosition = splitPositions[0]
            val startWidthSpec = MeasureSpec.makeMeasureSpec(startPosition.width(),
                MeasureSpec.EXACTLY
            )
            val startHeightSpec = MeasureSpec.makeMeasureSpec(startPosition.height(),
                MeasureSpec.EXACTLY
            )
            startView.measure(startWidthSpec, startHeightSpec)
            startView.layout(
                startPosition.left, startPosition.top, startPosition.right,
                startPosition.bottom
            )

            val endPosition = splitPositions[1]
            val endWidthSpec = MeasureSpec.makeMeasureSpec(endPosition.width(), MeasureSpec.EXACTLY)
            val endHeightSpec = MeasureSpec.makeMeasureSpec(endPosition.height(),
                MeasureSpec.EXACTLY
            )
            endView.measure(endWidthSpec, endHeightSpec)
            endView.layout(
                endPosition.left, endPosition.top, endPosition.right,
                endPosition.bottom
            )
        } else {
            super.onLayout(changed, left, top, right, bottom)
        }
    }

    private fun findStartView(): View? {
        var startView = findViewById<View>(startViewId)
        if (startView == null && childCount > 0) {
            startView = getChildAt(0)
        }
        return startView
    }

    private fun findEndView(): View? {
        var endView = findViewById<View>(endViewId)
        if (endView == null && childCount > 1) {
            endView = getChildAt(1)
        }
        return endView
    }

    private fun splitViewPositions(startView: View?, endView: View?): Array<Rect>? {
        if (windowLayoutInfo == null || startView == null || endView == null) {
            return null
        }

        // Calculate the area for view's content with padding
        val paddedWidth = width - paddingLeft - paddingRight
        val paddedHeight = height - paddingTop - paddingBottom

        windowLayoutInfo?.displayFeatures
            ?.firstOrNull { feature -> isValidFoldFeature(feature) }
            ?.let { feature ->
                getFeaturePositionInViewRect(feature, this)?.let {
                    if (feature.bounds.left == 0) { // Horizontal layout
                        val topRect = Rect(
                            paddingLeft, paddingTop,
                            paddingLeft + paddedWidth, it.top
                        )
                        val bottomRect = Rect(
                            paddingLeft, it.bottom,
                            paddingLeft + paddedWidth, paddingTop + paddedHeight
                        )

                        if (measureAndCheckMinSize(topRect, startView) &&
                            measureAndCheckMinSize(bottomRect, endView)
                        ) {
                            return arrayOf(topRect, bottomRect)
                        }
                    } else if (feature.bounds.top == 0) { // Vertical layout
                        val leftRect = Rect(
                            paddingLeft, paddingTop,
                            it.left, paddingTop + paddedHeight
                        )
                        val rightRect = Rect(
                            it.right, paddingTop,
                            paddingLeft + paddedWidth, paddingTop + paddedHeight
                        )

                        if (measureAndCheckMinSize(leftRect, startView) &&
                            measureAndCheckMinSize(rightRect, endView)
                        ) {
                            return arrayOf(leftRect, rightRect)
                        }
                    }
                }
            }

        // We have tried to fit the children and measured them previously. Since they didn't fit,
        // we need to measure again to update the stored values.
        measure(lastWidthMeasureSpec, lastHeightMeasureSpec)
        return null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        lastWidthMeasureSpec = widthMeasureSpec
        lastHeightMeasureSpec = heightMeasureSpec
    }

    private fun measureAndCheckMinSize(rect: Rect, childView: View): Boolean {
        val widthSpec = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.AT_MOST)
        val heightSpec = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.AT_MOST)
        childView.measure(widthSpec, heightSpec)
        return childView.measuredWidthAndState and MEASURED_STATE_TOO_SMALL == 0 &&
                childView.measuredHeightAndState and MEASURED_STATE_TOO_SMALL == 0
    }

    private fun isValidFoldFeature(displayFeature: DisplayFeature) =
        (displayFeature as? FoldingFeature)?.let { feature ->
            getFeaturePositionInViewRect(feature, this) != null
        } ?: false


    private fun getFeaturePositionInViewRect(
        displayFeature: DisplayFeature,
        view: View,
        includePadding: Boolean = true
    ): Rect? {
        // The the location of the view in window to be in the same coordinate space as the feature.
        val viewLocationInWindow = IntArray(2)
        view.getLocationInWindow(viewLocationInWindow)

        // Intersect the feature rectangle in window with view rectangle to clip the bounds.
        val viewRect = Rect(
            viewLocationInWindow[0], viewLocationInWindow[1],
            viewLocationInWindow[0] + view.width, viewLocationInWindow[1] + view.height
        )

        // Include padding if needed
        if (includePadding) {
            viewRect.left += view.paddingLeft
            viewRect.top += view.paddingTop
            viewRect.right -= view.paddingRight
            viewRect.bottom -= view.paddingBottom
        }

        val featureRectInView = Rect(displayFeature.bounds)
        val intersects = featureRectInView.intersect(viewRect)
        if ((featureRectInView.width() == 0 && featureRectInView.height() == 0) ||
            !intersects
        ) {
            return null
        }

        // Offset the feature coordinates to view coordinate space start point
        featureRectInView.offset(-viewLocationInWindow[0], -viewLocationInWindow[1])

        return featureRectInView
    }

}