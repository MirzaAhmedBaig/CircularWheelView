package org.mab.wheelpicker

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Scroller
import android.widget.TextView
import zoomIn
import zoomOut
import kotlin.math.abs
import kotlin.math.max
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
    private var wheelBackgroundColor = Color.BLACK
    private var wheelItemSelectionListener: WheelItemSelectionListener? = null

    private var DEFAULT_IN_BETWEEN_SPACE = 0
    private var maxElementsCount = 0
    private var runTimeWidth = 0
    private var lastIdlePosition = 0
    private var customPosition = -1

    /**
     * The initial fling velocity is divided by this amount.
     */
    private val FLING_VELOCITY_DOWNSCALE = 20


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
        viewType = a.getInteger(R.styleable.CircularWheelPicker_wheel_position, LEFT)
        textSize = a.getDimensionPixelSize(R.styleable.CircularWheelPicker_wheel_item_text_size, 20).toFloat()
        normalColor = a.getColor(R.styleable.CircularWheelPicker_wheel_item_text_color, Color.GRAY)
        selectionColor = a.getColor(R.styleable.CircularWheelPicker_wheel_item_selected_text_color, Color.WHITE)
        wheelBackgroundColor = a.getColor(R.styleable.CircularWheelPicker_wheel_background_color, Color.WHITE)
        a.recycle()
    }

    private fun init() {
        addMainWheelLayout()
        addDummyView()
        configureWheel()
    }

    private fun addMainWheelLayout() {
        val params = ConstraintLayout.LayoutParams(0, 0).apply {
            bottomToBottom = id
            topToTop = id
            if (viewType == RIGHT) startToStart = id else endToEnd = id
        }
        wheelLayout.layoutParams = params
        addView(wheelLayout)
    }

    private fun addDummyView() {
        val params = ConstraintLayout.LayoutParams(0, 0).apply {
            bottomToBottom = wheelLayout.id
            topToTop = wheelLayout.id
            startToStart = wheelLayout.id
            endToEnd = wheelLayout.id
        }
        dummyView.layoutParams = params
        wheelLayout.addView(dummyView)
    }

    private fun configureWheel() {
        wheelLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (wheelLayout.measuredHeight > 1) {
                    wheelLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val param = wheelLayout.layoutParams as ConstraintLayout.LayoutParams
                    runTimeWidth = (max(wheelLayout.measuredHeight, wheelLayout.width) * 0.9).toInt()
                    with(param) {
                        height = runTimeWidth
                        width = runTimeWidth
                        if (this@CircularWheelPicker.measuredWidth > width / 2) {
                            if (viewType == LEFT)
                                marginEnd = (this@CircularWheelPicker.measuredWidth - width * 0.4).toInt()
                            else
                                marginStart = (this@CircularWheelPicker.measuredWidth - width * 0.4).toInt()
                        }
                    }
                    wheelLayout.layoutParams = param

                    val param2 = dummyView.layoutParams as ConstraintLayout.LayoutParams
                    with(param2) {
                        width = runTimeWidth
                        height = runTimeWidth
                    }
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
        maxElementsCount = min(((runTimeWidth / getBiggestTextSize().first) * 4).toInt(), itemList.size)
        if (maxElementsCount % 2 != 0)
            maxElementsCount--
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
        return itemList.indexOf(getChildOfWheelByIndex(currentPosition).text.toString())
    }

    fun getCurrentItem(): String {
        return getChildOfWheelByIndex(currentPosition).text.toString()
    }

    fun setCurrentPosition(index: Int) {
        if (getChildOfWheelByIndex(currentPosition).text.toString() == itemList[index])
            return
        if (index > itemList.lastIndex)
            throw IndexOutOfBoundsException()
        mPieRotation = 360 - (index * ROTAION_ANGLE_OFFSET)
        customPosition = index
        onScrollFinished(200)

    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
    }

    fun setTextFont(typeface: Typeface) {
        this.typeface = typeface
    }

    fun setTexColor(normalColor: Int) {
        this.normalColor = normalColor
    }

    fun setSelectionTextColor(selectionColor: Int) {
        this.selectionColor = selectionColor
    }

    fun setWheelBackground(color: Int) {
        wheelBackgroundColor = color
    }

    fun setWheelItemSelectionListener(wheelItemSelectionListener: WheelItemSelectionListener) {
        this.wheelItemSelectionListener = wheelItemSelectionListener
    }

    private fun getBiggestElement(arrayList: ArrayList<String>): String {
        var bigSting = arrayList[0]
        (0 until arrayList.size).forEach {
            if (bigSting.length < arrayList[it].length)
                bigSting = arrayList[it]
        }
        return bigSting
    }

    private fun getBiggestTextSize(): Pair<Float, Float> {
        val size = getTextViewSize(context, getBiggestElement(itemList), textSize, width, typeface)
        return Pair(sqrt((size.first * size.first + size.second * size.second).toFloat()) + DEFAULT_IN_BETWEEN_SPACE, size.second.toFloat())
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

    private fun getNextPositionFor(index: Int, size: Int): Int {
        return (index + 1) % size
    }

    private fun getPreviousPositionFor(index: Int, size: Int): Int {
        var previous = (index - 1) % size
        if (previous < 0)
            previous += size
        return previous
    }

    private fun roundToCorrectIndex(index: Int, size: Int): Int {
        return index % size
    }

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

    private fun getChildOfWheelByIndex(index: Int): TextView {
        if (index >= wheelLayout.childCount - 1)
            throw ArrayIndexOutOfBoundsException()
        return wheelLayout.getChildAt(index + 1) as TextView
    }

    private fun performUpScrollingTask(gap: Int) {
        var start = roundToCorrectIndex(lastIdlePosition + (maxElementsCount / 2) - gap, maxElementsCount)
        val item = getChildOfWheelByIndex(start)
        var startValueIndex = getNextPositionFor(itemList.indexOf(item.text.toString()), itemList.size)
        (0 until gap).forEach {
            start = getNextPositionFor(start, maxElementsCount)
            getChildOfWheelByIndex(start).text = itemList[startValueIndex]
            startValueIndex = getNextPositionFor(startValueIndex, itemList.size)
        }
    }

    private fun performDownScrollingTask(gap: Int) {
        var start = roundToCorrectIndex(lastIdlePosition - (maxElementsCount / 2) + gap, maxElementsCount)
        if (start < 0)
            start += maxElementsCount
        val item = getChildOfWheelByIndex(start)
        var startValueIndex = getNextPositionFor(itemList.indexOf(item.text.toString()), itemList.size)
        (0 until gap).forEach {
            start = getPreviousPositionFor(start, maxElementsCount)
            getChildOfWheelByIndex(start).text = itemList[startValueIndex]
            startValueIndex = getPreviousPositionFor(startValueIndex, itemList.size)
        }
    }

    private fun onScrollFinished(animationDuration: Long = 100) {
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
                    wheelItemSelectionListener?.onItemSelected(itemList.indexOf(getChildOfWheelByIndex(currentPosition).text.toString()))
                    lastIdlePosition = currentPosition
                    reArrangeWheel()
                }

                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}

            })

        }.start()
    }

    private fun reArrangeElements(gap: Int, side: String) {
        when (side) {
            "UP" -> {
                if (viewType == LEFT)
                    performUpScrollingTask(gap)
                else
                    performDownScrollingTask(gap)
            }
            "DOWN" -> {
                if (viewType == LEFT)
                    performDownScrollingTask(gap)
                else
                    performUpScrollingTask(gap)
            }
        }
    }

    private fun reArrangeWheel() {
        var startIndex = if (customPosition == -1)
            itemList.indexOf(getChildOfWheelByIndex(currentPosition).text.toString())
        else {
            customPosition
        }
        var startElementIndex = currentPosition
        var endElementIndex = (currentPosition + maxElementsCount - 1) % maxElementsCount
        var endIndex = itemList.indexOf(getChildOfWheelByIndex(endElementIndex).text.toString())

        (0 until (maxElementsCount / 2) - 1).forEach {
            getChildOfWheelByIndex(startElementIndex).text = itemList[startIndex % itemList.size]
            startIndex = getNextPositionFor(startIndex, itemList.size)
            startElementIndex = getNextPositionFor(startElementIndex, maxElementsCount)
        }
        (0 until (maxElementsCount / 2) - 1).forEach {
            getChildOfWheelByIndex(endElementIndex).text = itemList[endIndex % itemList.size]
            endIndex = getPreviousPositionFor(endIndex, itemList.size)
            endElementIndex = getPreviousPositionFor(endElementIndex, maxElementsCount)
        }
        customPosition = -1
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
        wheelLayout.rotation = rotation
        val positionIndex = getCorrectPosition()

        val gap = abs(lastIdlePosition - positionIndex)
        if (gap < 3 && maxElementsCount != itemList.size) {
            if (lastIdlePosition < positionIndex) {
                reArrangeElements(gap, "UP")
            } else if (lastIdlePosition > positionIndex) {
                reArrangeElements(gap, "DOWN")
            }
        }
        lastIdlePosition = positionIndex
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

