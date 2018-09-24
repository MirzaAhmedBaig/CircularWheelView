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
import kotlin.math.abs


class CircularWheelPicker : ConstraintLayout {
    companion object {
        const val LEFT = 0
        const val RIGHT = 1
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
    private var position = 0
    private var selectionColor = Color.WHITE
    private var normalColor = Color.GRAY
    private var wheelItemSelectionListener: WheelItemSelectionListener? = null

    /**
     * The initial fling velocity is divided by this amount.
     */
    private val FLING_VELOCITY_DOWNSCALE = 6


    private val wheelLayout by lazy {
        ConstraintLayout(context).apply {
            setBackgroundResource(R.drawable.circle_shape)
            id = View.generateViewId()
        }
    }


    private val dummyView by lazy {
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
        wheelLayout.layoutParams = params
        addView(wheelLayout)
    }

    private fun addDummyView() {
        val params = ConstraintLayout.LayoutParams(0, 0)
        params.bottomToBottom = wheelLayout.id
        params.topToTop = wheelLayout.id
        params.startToStart = wheelLayout.id
        params.endToEnd = wheelLayout.id
        dummyView.layoutParams = params
        wheelLayout.addView(dummyView)
    }

    private fun configureWheel() {
        wheelLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (wheelLayout.measuredHeight > 1) {
                    wheelLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val param = wheelLayout.layoutParams
                    param.height = (wheelLayout.measuredHeight - wheelLayout.measuredHeight * 0.1).toInt()
                    param.width = (wheelLayout.measuredHeight - wheelLayout.measuredHeight * 0.1).toInt()
                    wheelLayout.layoutParams = param

                    val param2 = dummyView.layoutParams as ConstraintLayout.LayoutParams
                    param2.width = wheelLayout.measuredHeight
                    param2.height = wheelLayout.measuredHeight
                    dummyView.layoutParams = param2

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
            throw Exception("Invalid view type exception, should be left or right")
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
        ROTAION_ANGLE_OFFSET = if (viewType == LEFT) 360.0f / itemList.size else (360.0f / itemList.size) * -1
        itemList.forEachIndexed { index, value ->
            val textView = TextView(context).apply {
                id = View.generateViewId()
                text = value
                //textSize should be some percentage
                textSize = this@CircularWheelPicker.textSize
                this@CircularWheelPicker.typeface?.let {
                    typeface = it
                }
                if (index == 0)
                    setTextColor(selectionColor)
                else
                    setTextColor(normalColor)
                layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    circleConstraint = dummyView.id
                    circleRadius = (((dummyView.measuredHeight / 2) - (dummyView.measuredHeight / 2) * 0.2f).toInt())
                    circleAngle = if (viewType == RIGHT)
                        (((ROTAION_ANGLE_OFFSET * index) - 90f) % 360f)
                    else
                        (((ROTAION_ANGLE_OFFSET * index) + 90f) % 360f)
                }
            }
            wheelLayout.addView(textView)
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

        ObjectAnimator.ofFloat(wheelLayout, "rotation", oldRotation, mPieRotation).apply {
            duration = 200
            interpolator = AccelerateInterpolator()

        }.start()
        (0 until wheelLayout.childCount).forEach {
            if (wheelLayout.getChildAt(it) is TextView) {
                ObjectAnimator.ofFloat(wheelLayout.getChildAt(it), "rotation", 360 - mPieRotation).apply {
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

    fun setColor(normalColor: Int) {
        this.normalColor = normalColor
    }

    fun setSelectionColor(selectionColor: Int) {
        this.selectionColor = selectionColor
    }

    fun setWheelItemSelectionListener(wheelItemSelectionListener: WheelItemSelectionListener) {
        this.wheelItemSelectionListener = wheelItemSelectionListener
    }


    private fun onScrollFinished() {
        Log.d(TAG, "Current Rotation Angle : $mPieRotation")
        val oldRotation = mPieRotation
        if (mPieRotation % ROTAION_ANGLE_OFFSET != 0.0f) {
            val choice: String
            val condition = abs(mPieRotation % ROTAION_ANGLE_OFFSET) < abs(ROTAION_ANGLE_OFFSET / 2f)
            choice = if (viewType == LEFT) {
                if (condition) {
                    "L_UP"
                } else {
                    "L_DOWN"
                }
            } else {
                if (condition) {
                    "R_DOWN"

                } else {
                    "R_UP"
                }
            }
            mPieRotation = getCorrectRotation(choice)

        }
        wheelLayout.rotation = mPieRotation
        ObjectAnimator.ofFloat(wheelLayout, "rotation", oldRotation, mPieRotation).apply {
            duration = 200
            interpolator = AccelerateInterpolator()

        }.start()

        currentPosition = if (viewType == LEFT)
            (((360 - mPieRotation) / ROTAION_ANGLE_OFFSET) % itemList.size).toInt()
        else
            ((-(mPieRotation) / ROTAION_ANGLE_OFFSET) % itemList.size).toInt()
        if (currentPosition < 0)
            currentPosition += itemList.size

        position = currentPosition

        (0 until wheelLayout.childCount).forEach {
            wheelLayout.getChildAt(it).rotation = 360 - mPieRotation
            val item = wheelLayout.getChildAt(it)
            if (item is TextView)
                if (item.text == "${itemList[position]}") {
                    Log.d(TAG, " Changed Value : ${itemList[position]}")
                    item.setTextColor(selectionColor)
                    item.setTypeface(item.typeface, Typeface.BOLD)
                } else {
                    item.setTextColor(normalColor)
                    item.setTypeface(item.typeface, Typeface.NORMAL)
                }
        }

        wheelItemSelectionListener?.onItemSelected(currentPosition)
    }

    private fun getCorrectRotation(choice: String): Float {
        return when (choice) {
            "R_UP" -> {
                ROTAION_ANGLE_OFFSET * ((mPieRotation.toInt() / ROTAION_ANGLE_OFFSET.toInt()) - 1)
            }
            "R_DOWN", "L_UP" -> {
                ROTAION_ANGLE_OFFSET * (mPieRotation.toInt() / ROTAION_ANGLE_OFFSET.toInt())
            }
            "L_DOWN" -> {
                ROTAION_ANGLE_OFFSET * ((mPieRotation.toInt() / ROTAION_ANGLE_OFFSET.toInt()) + 1)
            }
            else -> {
                mPieRotation
            }
        }
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

        Log.d(TAG, "Call to setPieRotation : ${rotation % ROTAION_ANGLE_OFFSET}")
        if (rotation.toInt() % ROTAION_ANGLE_OFFSET.toInt() == 0) {
            Log.d(TAG, "Old Position $position")
            val isIncrement = mPieRotation > rotation
            position = if (viewType == LEFT)
                (((360 - mPieRotation) / ROTAION_ANGLE_OFFSET) % itemList.size).toInt()
            else
                ((-(mPieRotation) / ROTAION_ANGLE_OFFSET) % itemList.size).toInt()
            position = if (isIncrement) {
                (position + 1) % itemList.size
            } else {
//                (position - 1) % itemList.size
                position
            }
            if (position < 0)
                position += itemList.size
            Log.d(TAG, "New Position $position")
        }
        mPieRotation = rotation
        wheelLayout.rotation = rotation
        (0 until wheelLayout.childCount).forEach {
            wheelLayout.getChildAt(it).rotation = 360 - rotation
            val item = wheelLayout.getChildAt(it)
            if (item is TextView)
                if (item.text == "${itemList[position]}") {
                    Log.d(TAG, " Changed Value : ${itemList[position]}")
                    item.setTextColor(selectionColor)
                    item.setTypeface(item.typeface, Typeface.BOLD)
                } else {
                    item.setTextColor(normalColor)
                    item.setTypeface(item.typeface, Typeface.NORMAL)
                }
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
                    e2.x - (wheelLayout.width / 2 + wheelLayout.left),
                    e2.y - (wheelLayout.height / 2 + wheelLayout.top))

            setPieRotation(getPieRotation() - scrollToRotate / FLING_VELOCITY_DOWNSCALE)
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

            val scrollToRotate = vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.x - (wheelLayout.width / 2 + wheelLayout.left),
                    e2.y - (wheelLayout.height / 2 + wheelLayout.top))

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
        fun onItemSelected(index: Int)
    }

}

