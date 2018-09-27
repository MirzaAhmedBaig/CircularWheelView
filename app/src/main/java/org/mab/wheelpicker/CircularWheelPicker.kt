package org.mab.wheelpicker

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Scroller
import android.widget.TextView
import zoomIn
import zoomOut
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt


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

    private var DEFAULT_IN_BETWEEN_SPACE = 0
    private var maxElementsCount = 0
    private var runTimeWidth = 0
    private var centerChildIndex = 0
    private var lastIdlePosition = 0

    /**
     * The initial fling velocity is divided by this amount.
     */
    private val FLING_VELOCITY_DOWNSCALE = 15


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

    private val displayMetrics by lazy {
        DisplayMetrics().apply {
            (context as Activity).windowManager.defaultDisplay.getMetrics(this)
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
                    runTimeWidth = (wheelLayout.measuredHeight * 0.9).toInt()
                    param.height = runTimeWidth
                    param.width = runTimeWidth
                    wheelLayout.layoutParams = param

                    val param2 = dummyView.layoutParams as ConstraintLayout.LayoutParams
                    param2.width = runTimeWidth
                    param2.height = runTimeWidth
                    dummyView.layoutParams = param2
                    DEFAULT_IN_BETWEEN_SPACE = (param.width * 0.15f).toInt()

                    isChideDone = true
                    if (!isDataHasSet) {
                        setDataSet(itemList)
                    }
                    setGestures()
                }
            }
        })


    }

    private fun configureWheelElements() {
        maxElementsCount = min(((runTimeWidth / getBiggestTextSize()) * 4).toInt(), itemList.size)
        if (maxElementsCount % 2 != 0)
            maxElementsCount--
        centerChildIndex = maxElementsCount / 2
        ROTAION_ANGLE_OFFSET = if (viewType == LEFT) 360.0f / maxElementsCount.toFloat() else (360.0f / maxElementsCount.toFloat()) * -1f
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

        configureWheelElements()

        /*itemList.forEachIndexed { index, value ->
            val textView = TextView(context).apply {
                id = View.generateViewId()
                text = value
                textSize = this@CircularWheelPicker.textSize
                typeface = this@CircularWheelPicker.typeface
                setTextColor(normalColor)
                layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    circleConstraint = dummyView.id
                    circleRadius = (((dummyView.measuredHeight / 2) - (dummyView.measuredHeight / 2) * 0.2f).toInt())
                    circleAngle = if (viewType == RIGHT)
                        (((ROTAION_ANGLE_OFFSET * index) - 90f) % 360f)
                    else
                        (((ROTAION_ANGLE_OFFSET * index) + 90f) % 360f)
                    rotation = circleAngle
                }
            }
            wheelLayout.addView(textView)
            if (index == 0)
                textView.zoomIn(onAnimationStart = {
                    textView.setTextColor(selectionColor)
                }, duration = 200)
        }*/
        var multiplier = 0
        (0 until maxElementsCount / 2).forEach {
            val textView = TextView(context).apply {
                id = View.generateViewId()
                text = itemList[it]
                textSize = this@CircularWheelPicker.textSize
                typeface = this@CircularWheelPicker.typeface
                setTextColor(normalColor)
                layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    circleConstraint = dummyView.id
                    circleRadius = (((dummyView.measuredHeight / 2) - (dummyView.measuredHeight / 2) * 0.2f).toInt())
                    circleAngle = if (viewType == RIGHT)
                        (((ROTAION_ANGLE_OFFSET * multiplier) - 90f) % 360f)
                    else
                        (((ROTAION_ANGLE_OFFSET * multiplier) + 90f) % 360f)
                    rotation = circleAngle
                }
            }
            wheelLayout.addView(textView)
            if (it == 0)
                textView.zoomIn(onAnimationStart = {
                    textView.setTextColor(selectionColor)
                }, duration = 200)
            multiplier++
        }

        (itemList.size - (maxElementsCount / 2) until itemList.size).forEach {
            val textView = TextView(context).apply {
                id = View.generateViewId()
                text = itemList[it]
                textSize = this@CircularWheelPicker.textSize
                typeface = this@CircularWheelPicker.typeface
                setTextColor(normalColor)
                layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    circleConstraint = dummyView.id
                    circleRadius = (((dummyView.measuredHeight / 2) - (dummyView.measuredHeight / 2) * 0.2f).toInt())
                    circleAngle = if (viewType == RIGHT)
                        (((ROTAION_ANGLE_OFFSET * multiplier) - 90f) % 360f)
                    else
                        (((ROTAION_ANGLE_OFFSET * multiplier) + 90f) % 360f)
                    rotation = circleAngle
                }
            }
            wheelLayout.addView(textView)
            multiplier++
        }

    }

    fun getCurrentPosition(): Int {
        return currentPosition
    }

    fun getCurrentItem(): String {
        return itemList[currentPosition]
    }

    fun setCurrentPosition(index: Int) {
        if (index == currentPosition)
            return
        if (index > itemList.lastIndex)
            throw IndexOutOfBoundsException()
        mPieRotation = 360 - (index * ROTAION_ANGLE_OFFSET)
        onScrollFinished(200)

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

    private fun getBiggestTextWidth(): Int {
        val text = getBiggestElement(itemList)
        Log.d(TAG, "Biggest Text : $text")
        val textPaint = TextPaint()
        textPaint.textSize = textSize
        textPaint.typeface = typeface
        textPaint.color = normalColor
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        Log.d(TAG, "Bounds Width : ${bounds.width()} Measure Width : ${textPaint.measureText(text)}")
        return bounds.width()
    }

    private fun getBiggestElement(arrayList: ArrayList<String>): String {
        var bigSting = arrayList[0]
        (0 until arrayList.size).forEach {
            if (bigSting.length < arrayList[it].length)
                bigSting = arrayList[it]
        }
        return bigSting
    }


    private fun onScrollFinished(animationDuration: Long = 100) {
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
            duration = animationDuration
            interpolator = AccelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    val oldItem = getChildOfWheelByIndex(currentPosition)
                    oldItem.zoomOut(onAnimationStart = {
                        oldItem.setTextColor(normalColor)
                    }, duration = 200)
                    currentPosition = getCorrectPosition()

                    position = currentPosition

                    val newItem = getChildOfWheelByIndex(currentPosition)
                    newItem.zoomIn(onAnimationStart = {
                        newItem.setTextColor(selectionColor)
                    }, duration = 200)
                    wheelItemSelectionListener?.onItemSelected(currentPosition)
                }

                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}

            })

        }.start()
        centerChildIndex = (currentPosition + (maxElementsCount / 2)) % maxElementsCount
    }

    //Changed
    private fun getCorrectPosition(): Int {
        var position = if (viewType == LEFT)
            (((360 - mPieRotation) / ROTAION_ANGLE_OFFSET) % maxElementsCount).toInt()
        else
            ((-(mPieRotation) / ROTAION_ANGLE_OFFSET) % maxElementsCount).toInt()
        if (position < 0)
            position += maxElementsCount
        return position
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
//
        var rotation = rotation
        rotation = (rotation % 360 + 360) % 360
        mPieRotation = rotation
        wheelLayout.rotation = rotation
        val positionIndex = getCorrectPosition()
        /*if(lastIdlePosition)
        if (lastIdlePosition < positionIndex) {
        } else if (lastIdlePosition > currentPosition) {

        }*/
        val gap = abs(lastIdlePosition - positionIndex)
        if (gap < 3 && maxElementsCount != itemList.size) {
            if (lastIdlePosition < positionIndex) {
                reArrangeElements(gap, "UP")
            } else if (lastIdlePosition > positionIndex) {
                reArrangeElements(gap, "DOWN")
            }
        }
//        Log.d(TAG, "getCorrectPosition ${positionIndex}")
        lastIdlePosition = positionIndex
    }

    private fun reArrangeElements(gap: Int, side: String) {
        Log.d(TAG, "###$side")
        when (side) {
            "UP" -> {
                var start = centerChildIndex - 1
                //First Child of other end
                val item = getChildOfWheelByIndex(start)
                //Last Child value of start
                var startValueIndex = (itemList.indexOf(item.text) + 1) % itemList.size
                (0 until gap).forEach {
                    var viewIndex = (start + 1) % maxElementsCount
                    val child = getChildOfWheelByIndex(viewIndex)
                    child.text = itemList[startValueIndex]
                    start = (start + 1) % maxElementsCount
                    startValueIndex = (startValueIndex + 1) % itemList.size
                }
                centerChildIndex = start
            }
            "DOWN" -> {
                var start = centerChildIndex - 1
                /*if(start==0)
                    start++*/
                val item = wheelLayout.getChildAt(start + 1) as TextView
                var startValueIndex = itemList.indexOf(item.text) - 1
                (0 until gap).forEach {
                    var viewIndex = (start + 1) % wheelLayout.childCount
                    if (viewIndex == 0)
                        viewIndex++
                    val child = wheelLayout.getChildAt(viewIndex) as TextView
                    child.text = itemList[startValueIndex]
                    start = (start - 1) % maxElementsCount
                    if (start < 0)
                        start += maxElementsCount
                    startValueIndex = (startValueIndex - 1) % itemList.size
                    if (startValueIndex < 0)
                        startValueIndex += itemList.size
                }
                centerChildIndex = start
            }
        }
    }

    private fun getChildOfWheelByIndex(index: Int): TextView {
        if (index >= wheelLayout.childCount - 1)
            throw ArrayIndexOutOfBoundsException()
        return wheelLayout.getChildAt(index + 1) as TextView
    }

    private fun rearrangeElements(startPosition: Int) {


        /*var i = startPosition
        (0 until maxElementsCount / 2).forEach {
            val item = wheelLayout.getChildAt(it + 1) as TextView
            item.text = (itemList[i])
            i = (i + 1) % itemList.size
        }
        i = startPosition
        (maxElementsCount..maxElementsCount / 2).forEach {
            val item = wheelLayout.getChildAt(it) as TextView
            i -= 1
            if (i < 0)
                i += itemList.size
            item.text = (itemList[i])
        }*/
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


    private fun getBiggestTextSize(): Float {
        val size = getTextViewSize(context, getBiggestElement(itemList), textSize, width, typeface)
        return sqrt((size.first * size.first + size.second * size.second).toFloat()) + DEFAULT_IN_BETWEEN_SPACE
    }

    private fun getTextViewSize(context: Context, text: CharSequence, textSize: Float, deviceWidth: Int, typeface: Typeface?): Pair<Int, Int> {
        val textView = TextView(context)
        textView.typeface = typeface
        textView.setText(text, TextView.BufferType.SPANNABLE)
        textView.textSize = textSize
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        textView.measure(widthMeasureSpec, heightMeasureSpec)
        return Pair(textView.measuredWidth, textView.measuredHeight)
    }

}

