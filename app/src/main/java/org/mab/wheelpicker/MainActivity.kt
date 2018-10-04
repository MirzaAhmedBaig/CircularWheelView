package org.mab.wheelpicker

import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setWheelPicker()

        val f = ResourcesCompat.getFont(this, R.font.avenir_next_demi_bold)
        left.typeface = f
    }

    private fun setWheelPicker() {
        val list = ArrayList<String>()
        (0..59).forEach {
            list.add("$it")
        }
        circularWheelPicker_one.setDataSet(list)
        circularWheelPicker_one.setWheelItemSelectionListener(object : CircularWheelPicker.WheelItemSelectionListener {
            override fun onItemSelected(index: Int) {
                Log.d(TAG, "Selected position is : $index")
                Log.d(TAG, "Get Current Item : ${circularWheelPicker_one.getCurrentItem()}")
                Log.d(TAG, "Get Current Position : ${circularWheelPicker_one.getCurrentPosition()}")
            }
        })

        val list2 = ArrayList<String>()
        (0..59).forEach {
            list2.add(it.toString())
        }

        circularWheelPicker_two.setDataSet(list2)
        circularWheelPicker_two.setWheelItemSelectionListener(object : CircularWheelPicker.WheelItemSelectionListener {
            override fun onItemSelected(index: Int) {
                Log.d(TAG, "Selected position is : $index")
                Log.d(TAG, "Get Current Item : ${circularWheelPicker_two.getCurrentItem()}")
                Log.d(TAG, "Get Current Position : ${circularWheelPicker_two.getCurrentPosition()}")
            }
        })

        left.setOnClickListener {
            circularWheelPicker_one.setCurrentPosition(position.text.toString().toInt())
        }
        right.setOnClickListener {
            circularWheelPicker_two.setCurrentPosition(position.text.toString().toInt())
        }
    }

}
