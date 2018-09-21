package org.mab.wheelpicker

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.support.constraint.ConstraintSet
import android.support.constraint.Guideline
import android.util.Log
import android.view.GestureDetector
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Scroller
import android.widget.TextView


class CircularWheelPicker : ConstraintLayout {
    companion object {
        val LEFT = 0
        val RIGHT = 1
    }


    private val TAG = CircularWheelPicker::class.java.simpleName
    private var itemList = ArrayList<String>()
    private var position = 0
    private var ROTAION_ANGLE_OFFSET: Float = 0f
    private var currentPosotion = 0

    private var mDetector: GestureDetector? = null
    private var mPieRotation: Float = 0.0f
    private var mScroller: Scroller? = null
    private var mScrollAnimator: ValueAnimator? = null
    private var isDatasetHasSet = false
    private var isChideDone = false

    /**
     * The initial fling velocity is divided by this amount.
     */
    private val FLING_VELOCITY_DOWNSCALE = 6


    private val wheel_layout by lazy {
        ConstraintLayout(context).apply {
            setBackgroundResource(R.drawable.circle_shape)
            id = View.generateViewId()
        }
    }
    private val constraintSet by lazy {
        ConstraintSet()
    }

    private val negativeGuideline by lazy {
        Guideline(context).apply {
            Log.d(TAG, "Call to guidline object")
            id = View.generateViewId()

        }
    }

    private val positiveGuideline by lazy {
        Guideline(context).apply {
            id = View.generateViewId()
        }
    }
    private val dummy_view by lazy {
        View(context).apply {
            id = View.generateViewId()
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addGuidLines()
        addMainWheelLayout()
        addDummyView()
        configureWheel()
    }

    private fun addGuidLines() {
        constraintSet.clone(this@CircularWheelPicker)
        constraintSet.create(negativeGuideline.id, ConstraintSet.VERTICAL_GUIDELINE)
        constraintSet.create(positiveGuideline.id, ConstraintSet.VERTICAL_GUIDELINE)
        constraintSet.applyTo(this@CircularWheelPicker)
        constraintSet.clone(this@CircularWheelPicker)
        constraintSet.setGuidelinePercent(negativeGuideline.id, -0.9f)
        constraintSet.setGuidelinePercent(positiveGuideline.id, 0.4f)
        constraintSet.applyTo(this@CircularWheelPicker)
    }

    private fun addMainWheelLayout() {
        val params = ConstraintLayout.LayoutParams(0, 0)
        params.bottomToBottom = id
        params.topToTop = id
        params.startToStart = negativeGuideline.id
        params.endToStart = positiveGuideline.id
        wheel_layout.layoutParams = params
        addView(wheel_layout)
    }

    private fun addDummyView() {
        val params = ConstraintLayout.LayoutParams(0, 0)
        params.bottomToBottom = wheel_layout.id
        params.topToTop = wheel_layout.id
        params.startToStart = wheel_layout.id
        params.endToEnd = wheel_layout.id
        dummy_view.layoutParams = params
        wheel_layout.addView(dummy_view)
    }

    private fun configureWheel() {
        Log.d(TAG, "call to setWheel")
        wheel_layout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (wheel_layout.measuredHeight > 1) {
                    wheel_layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val param = wheel_layout.layoutParams
                    param.height = (wheel_layout.measuredHeight - wheel_layout.measuredHeight * 0.2).toInt()
                    param.width = (wheel_layout.measuredHeight - wheel_layout.measuredHeight * 0.2).toInt()
                    wheel_layout.layoutParams = param

                    val param2 = dummy_view.layoutParams as ConstraintLayout.LayoutParams
                    param2.width = wheel_layout.measuredHeight
                    param2.height = wheel_layout.measuredHeight
                    dummy_view.layoutParams = param2

                    Log.d(TAG, "Rendering done")
                    isChideDone = true
                    if (!isDatasetHasSet) {
                        setDataSet(itemList)
                    }
                }
            }
        })


    }

    fun setDataSet(itemList: ArrayList<String>) {
        this.itemList = itemList
        if (!isChideDone)
            return
        else
            isDatasetHasSet = true
        ROTAION_ANGLE_OFFSET = 360.0f / itemList.size
        Log.d(TAG, "call to setDataSet")
        itemList.forEachIndexed { index, value ->
            val textView = TextView(context).apply {
                id = View.generateViewId()
                text = value
                //textSize should be some percentage
                textSize = 20f
                setTextColor(Color.WHITE)
                layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    circleConstraint = dummy_view.id
                    circleRadius = ((dummy_view.measuredWidth / 2))
                    circleAngle = ((ROTAION_ANGLE_OFFSET * index) + 90f % 360f)
                    Log.d(TAG, "Circle Radius : ${((dummy_view.measuredWidth / 2))}")
                    Log.d(TAG, "Circle Angle : ${((ROTAION_ANGLE_OFFSET * index) + 90f % 360f)}")
                }
            }
            wheel_layout.addView(textView)
        }

    }


}