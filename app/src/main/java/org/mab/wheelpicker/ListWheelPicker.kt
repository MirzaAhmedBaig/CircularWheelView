package org.mab.wheelpicker

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import dpToPx

class ListWheelPicker : ListView {
    private val TAG = ListWheelPicker::class.java.simpleName
    private var itemsArray = ArrayList<String>()
    private var onMeasureCalled = false
    private var isDataHasSet = false
    private var isLessItems = false
    private var itemFixedHeight = dpToPx(100)
    private var marginsArray = ArrayList<Int>()
    private var mWidth = 0
    private var mHeight = 0
    private var firstVisible = 0
    private var lastVisible = 0


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
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false


        setOnScrollListener(object : OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                Log.d(TAG, "VIsible Item : $visibleItemCount")

            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                Log.d(TAG, "Scorll State : $scrollState")
            }

        })
        divider = null
//        setBackgroundColor(Color.BLACK)
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredHeight > 1) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    onMeasureCalled = true
                    mWidth = measuredWidth
                    mHeight = measuredHeight
                    if (!isDataHasSet && itemsArray.size > 0) {
                        setDataItems(itemsArray)
                    }
                }
            }
        })

    }


    fun setDataItems(itemsArray: ArrayList<String>) {
        Log.d(TAG, "setDataItems called")
        this.itemsArray = itemsArray
        if (itemsArray.size < 7) {
            isLessItems = true
        }
        if (!onMeasureCalled)
            return
        else
            isDataHasSet = true
        setConfigurationValues()
        adapter = ListAdapter(itemsArray)
    }

    private fun setConfigurationValues() {
        Log.d(TAG, "setConfigurationValues called ${mHeight} ")
        var itemCount = (mHeight / itemFixedHeight)
        Log.d(TAG, "Bfore chje $itemCount")
        if (itemCount % 2 == 0)
            itemCount += 1

        itemFixedHeight = (mHeight / itemCount)
        val widthOffset = mWidth / (itemCount / 2)
        marginsArray = ArrayList()
        marginsArray.add(0)
        (1..itemCount / 2).forEach {
            marginsArray.add(widthOffset * it)
        }
        val temp = marginsArray.lastIndex
        (1..itemCount / 2).forEach {
            marginsArray.add(marginsArray[temp - it])
        }
        marginsArray[(marginsArray.size / 2)] = (marginsArray[(marginsArray.size / 2)] - marginsArray[(marginsArray.size / 2)] * 0.3).toInt()

        Log.d(TAG, "Margin's Array Size : ${measuredHeight} , Item showing is : $itemCount")
    }


    private fun setViewAroundCircle() {
        Log.d(TAG, "setViewAroundCircle called ${marginsArray.size} , ${itemFixedHeight}")
        /*if (lastVisible == lastVisiblePosition)
            return*/

        firstVisible = firstVisiblePosition
        lastVisible = lastVisiblePosition
        (0 until childCount).forEach {
            val item = (getChildAt(it) as FrameLayout?)?.getChildAt(0) as TextView?
            item?.let { view ->
                val params = view.layoutParams as FrameLayout.LayoutParams
                val leftMargin = if (it in (firstVisible until lastVisible)) {
                    if (firstVisible < childCount) {
                        if (marginsArray[(it - firstVisible)] != 0) marginsArray[(it - firstVisible)] - item.width else 0
                    } else {
                        0
                    }
                } else {
                    0
                }
                params.setMargins(leftMargin, 0, 0, 0)
                item.layoutParams = params
            }
        }
        /*(firstVisible until lastVisible).forEach {
            if (firstVisible < childCount) {
                val item = (getChildAt(it) as FrameLayout?)?.getChildAt(0) as TextView?
                item?.let { view ->
                    val params = view.layoutParams as FrameLayout.LayoutParams
//                params.width = params.width + marginsArray[(it - firstVisible)]
                    val leftMargin = if (marginsArray[(it - firstVisible)] != 0) marginsArray[(it - firstVisible)] - item.width else 0
                    params.setMargins(leftMargin, 0, 0, 0)
                    item.layoutParams = params
                }

            }
        }*/

    }

    override fun computeVerticalScrollOffset(): Int {
        val y = super.computeVerticalScrollOffset()
        var absoluteY = y
        val pos = firstVisiblePosition
        if (pos > 0) {
            absoluteY += (pos - 1) * measuredHeight
        }
        Log.d(TAG, "Abdolute Y : $absoluteY ,y")
        //use absoluteY
        return y
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        Log.d(TAG, "OldT : $oldt , T : $t , OldL : $oldl , L : $l")
        val scrollType = if (oldl - l < 0) "Scrolling UP" else if (oldl - l > 0) "Scrolling Down" else "Idle"
        Log.d(TAG, "Scroll Type : $scrollType")
//        Log.d(TAG, "First visible : $firstVisiblePosition , Last Visible : $lastVisiblePosition")
        /*firstVisible = firstVisiblePosition
        lastVisible = lastVisiblePosition
        setViewAroundCircle()
        super.onScrollChanged(l, t, oldl, oldt)*/
    }

    inner class ListAdapter(private val dataSet: ArrayList<String>) : ArrayAdapter<String>(context, R.layout.list_wheel_picker_item, dataSet) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var row = convertView
            val holder: ItemHolder?

            if (row == null) {
                val inflater = (context as Activity).layoutInflater
                row = inflater.inflate(R.layout.list_wheel_picker_item, parent, false)

                holder = ItemHolder()
                holder.item = row.findViewById(R.id.list_item) as TextView
                val params = holder.item?.layoutParams as FrameLayout.LayoutParams
                Log.d(TAG, "Fixed Height : $itemFixedHeight")
                params.height = itemFixedHeight
                holder.item?.layoutParams = params
                row.tag = holder
            } else {
                holder = row.tag as ItemHolder
                /*val params = holder.item?.layoutParams as FrameLayout.LayoutParams?
                params?.let {
                    params.setMargins(0, 0, 0, 0)
                    holder.item?.layoutParams = params
                }*/
            }
            /*val params = holder.item?.layoutParams as FrameLayout.LayoutParams?
            params?.let {
                Log.d(TAG,"Position : $position First ; $firstVisible Last $lastVisible")
                val leftMargin = if (position in (firstVisible..lastVisible)) {
                    lastVisible = position+1
                    if (marginsArray[(position - firstVisible)] != 0) marginsArray[(position - firstVisible)] - holder.item!!.width else 0
                } else {
                    0
                }
                params.setMargins(leftMargin, 0, 0, 0)
                holder.item?.layoutParams = params
            }*/


            holder.item!!.text = dataSet[position]
            return row!!
        }
    }

    internal class ItemHolder {
        var item: TextView? = null
    }
}