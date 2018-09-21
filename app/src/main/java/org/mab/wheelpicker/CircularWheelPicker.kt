package org.mab.wheelpicker

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Scroller
import android.widget.TextView


class CircularWheelPicker : ConstraintLayout {
    companion object {
        val LEFT = 0
        val RIGHT = 1
    }


    private val TAG = CircularWheelPicker::class.java.simpleName
    private var itemList = ArrayList<String>()
    private var ROTAION_ANGLE_OFFSET: Float = 0f
    private var currentPosition = 0
    private var typeface: Typeface? = null
    private var textSize: Float = 20f
    private var mDetector: GestureDetector? = null
    private var mPieRotation: Float = 0.0f
    private var mScroller: Scroller? = null
    private var mScrollAnimator: ValueAnimator? = null
    private var isDataHasSet = false
    private var isChideDone = false
    private var viewType = LEFT
    private var wheelItemSelectionListener: WheelItemSelectionListener? = null

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


    private val dummy_view by lazy {
        View(context).apply {
            id = View.generateViewId()
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        getAttributedValues(attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        getAttributedValues(attrs)
        init()
    }

    private fun getAttributedValues(attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CircularWheelPicker,
                0, 0)
        viewType = a.getInteger(R.styleable.CircularWheelPicker_position, LEFT)
        a.recycle()
    }

    private fun init() {
        addMainWheelLayout()
        addDummyView()
        configureWheel()
    }


    private fun addMainWheelLayout() {
        val params = ConstraintLayout.LayoutParams(0, 0)
        params.bottomToBottom = id
        params.topToTop = id
        if (viewType == RIGHT) {
            params.startToStart = id
        } else {
            params.endToEnd = id
        }
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
                    param.height = (wheel_layout.measuredHeight - wheel_layout.measuredHeight * 0.1).toInt()
                    param.width = (wheel_layout.measuredHeight - wheel_layout.measuredHeight * 0.1).toInt()
                    wheel_layout.layoutParams = param

                    val param2 = dummy_view.layoutParams as ConstraintLayout.LayoutParams
                    param2.width = wheel_layout.measuredHeight
                    param2.height = wheel_layout.measuredHeight
                    dummy_view.layoutParams = param2

                    Log.d(TAG, "Rendering done")
                    isChideDone = true
                    if (!isDataHasSet) {
                        setDataSet(itemList)
                    }
                    setGestures()
                }
            }
        })


    }

    fun setViewType(viewType: Int) {
        if (viewType !in (1..2)) {
            throw Exception("Invalid view type exception should be left or right")
        } else {
            this.viewType = viewType
        }
    }

    fun setDataSet(itemList: ArrayList<String>) {
        this.itemList = itemList
        if (!isChideDone)
            return
        else
            isDataHasSet = true
        ROTAION_ANGLE_OFFSET = 360.0f / itemList.size
        Log.d(TAG, "call to setDataSet")
        itemList.forEachIndexed { index, value ->
            val textView = TextView(context).apply {
                id = View.generateViewId()
                text = value
                //textSize should be some percentage
                textSize = this@CircularWheelPicker.textSize
                this@CircularWheelPicker.typeface?.let {
                    typeface = it
                }
                setTextColor(Color.WHITE)
                layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    circleConstraint = dummy_view.id
                    circleRadius = (((dummy_view.measuredHeight / 2) - (dummy_view.measuredHeight / 2) * 0.2f).toInt())
                    circleAngle = ((ROTAION_ANGLE_OFFSET * index) + 90f % 360f)
                    Log.d(TAG, "Circle Radius : ${((dummy_view.measuredWidth / 2))}")
                    Log.d(TAG, "Circle Angle : ${((ROTAION_ANGLE_OFFSET * index) + 90f % 360f)}")
                }
            }
            wheel_layout.addView(textView)
        }

    }

    fun getCurrentPosition(): Int {
        return (((360 - mPieRotation) / ROTAION_ANGLE_OFFSET) % itemList.size).toInt()
    }

    fun getCurrentItem(): String {
        return itemList[currentPosition]
    }

    fun setCurrentPosition(index: Int) {
        if (index == currentPosition)
            return
        val oldRotation = mPieRotation
        Log.d(TAG, "Current Position : $currentPosition")
        Log.d(TAG, "Current Angle : $currentPosition")
        if (index > itemList.lastIndex)
            throw IndexOutOfBoundsException()
        mPieRotation = 360 - (index * ROTAION_ANGLE_OFFSET)

        ObjectAnimator.ofFloat(wheel_layout, "rotation", oldRotation, mPieRotation).apply {
            duration = 200
            interpolator = AccelerateInterpolator()

        }.start()
        (0 until wheel_layout.childCount).forEach {
            if (wheel_layout.getChildAt(it) is TextView) {
                ObjectAnimator.ofFloat(wheel_layout.getChildAt(it), "rotation", 360 - mPieRotation).apply {
                    duration = 200
                    interpolator = AccelerateInterpolator()

                }.start()
            }
        }
        currentPosition = index
    }

    fun setFont(typeface: Typeface) {
        this.typeface = typeface
    }

    fun setTextSize(size: Float) {
        this.textSize = textSize
    }

    fun setWheelItemSelectionListener(wheelItemSelectionListener: WheelItemSelectionListener) {
        this.wheelItemSelectionListener = wheelItemSelectionListener
    }

    private fun onScrollFinished() {
        val oldRotation = mPieRotation
        val reminder = mPieRotation.toInt() % ROTAION_ANGLE_OFFSET.toInt()
        if (reminder != 0) {
            mPieRotation = if ((reminder.toFloat() + (mPieRotation - mPieRotation.toInt())) < (ROTAION_ANGLE_OFFSET / 2)) {
                //go down
                ROTAION_ANGLE_OFFSET * (mPieRotation.toInt() / ROTAION_ANGLE_OFFSET.toInt())
            } else {
                ROTAION_ANGLE_OFFSET * ((mPieRotation.toInt() / ROTAION_ANGLE_OFFSET.toInt()) + 1)
            }
        }
        wheel_layout.rotation = mPieRotation
        ObjectAnimator.ofFloat(wheel_layout, "rotation", oldRotation, mPieRotation).apply {
            duration = 200
            interpolator = AccelerateInterpolator()

        }.start()
        (0 until wheel_layout.childCount).forEach {
            wheel_layout.getChildAt(it).rotation = 360 - mPieRotation
        }
        currentPosition = (((360 - mPieRotation) / ROTAION_ANGLE_OFFSET) % itemList.size).toInt()
        Log.d(TAG, "Angle Value : ${((360 - mPieRotation) / ROTAION_ANGLE_OFFSET) % itemList.size}")

        wheelItemSelectionListener?.onItemSeleted(currentPosition)
    }

    private fun isAnimationRunning(): Boolean {
        return !mScroller!!.isFinished
    }

    private fun vectorToScalarScroll(dx: Float, dy: Float, x: Float, y: Float): Float {
        val l = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        val crossX = -y
        val dot = crossX * dx + x * dy
        val sign = Math.signum(dot)
        return l * sign
    }


    private fun setPieRotation(rotation: Float) {
        var rotation = rotation
        rotation = (rotation % 360 + 360) % 360
        mPieRotation = rotation
        wheel_layout.rotation = rotation
        (0 until wheel_layout.childCount).forEach {
            wheel_layout.getChildAt(it).rotation = 360 - rotation
        }
    }


    private fun getPieRotation(): Float {
        return mPieRotation
    }

    private fun tickScrollAnimation() {
        if (!mScroller!!.isFinished) {
            mScroller!!.computeScrollOffset()
            setPieRotation(mScroller!!.currY.toFloat())
        } else {
            mScrollAnimator!!.cancel()
            onScrollFinished()
        }
    }

    private fun stopScrolling() {
        mScroller!!.forceFinished(true)
        onScrollFinished()
    }

    private fun setGestures() {
        mDetector = GestureDetector(context, gestureListener)
        mDetector!!.setIsLongpressEnabled(true)

        mPieRotation = 0f

        mScroller = Scroller(context)
        mScrollAnimator = ValueAnimator.ofFloat(0f, 1f)
        mScrollAnimator!!.addUpdateListener { tickScrollAnimation() }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var result = mDetector!!.onTouchEvent(event)

        if (!result) {
            if (event!!.action == MotionEvent.ACTION_UP) {
                stopScrolling()
                result = true
            }
        }
        return result
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val scrollToRotate = vectorToScalarScroll(
                    distanceX,
                    distanceY,
                    e2.x - (wheel_layout.width / 2 + wheel_layout.left),
                    e2.y - (wheel_layout.height / 2 + wheel_layout.top))

            setPieRotation(getPieRotation() - scrollToRotate / FLING_VELOCITY_DOWNSCALE)
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

            val scrollToRotate = vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.x - (wheel_layout.width / 2 + wheel_layout.left),
                    e2.y - (wheel_layout.height / 2 + wheel_layout.top))

            mScroller?.fling(
                    0,
                    getPieRotation().toInt(),
                    0,
                    scrollToRotate.toInt() / FLING_VELOCITY_DOWNSCALE,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE)

            mScrollAnimator!!.duration = mScroller!!.duration.toLong()
            mScrollAnimator!!.start()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            if (isAnimationRunning()) {
                stopScrolling()
            }
            return true
        }
    }

    interface WheelItemSelectionListener {
        fun onItemSeleted(index: Int)
    }


}