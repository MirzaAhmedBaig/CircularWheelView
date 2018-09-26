package org.mab.wheelpicker

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import dpToPx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setWheelPicker()
    }

    private fun setWheelPicker() {
        val list = ArrayList<String>()
        /*(0..20).forEach {
            list.add("Mirza Ahmed")
        }*/
        list.add("mirza")
        list.add("mirza ahmed")
        list.add("mirza ahmed baig")
        list.add("mirza ahmed baig")
        list.add("mirza mi")
        list.add("mirza ")
        list.add("mirza jsf")
        list.add("mirza")
        list.add("mirza ahmed")
        list.add("mirza ahmed baig")
        list.add("mirza ahmed baig")
        list.add("mirza mi")
        list.add("mirza ")
        list.add("mirza jsf")
        list.add("mirza")
        list.add("mirza ahmed")
        list.add("mirza ahmed baig")
        list.add("mirza ahmed baig")
        list.add("mirza mi")
        list.add("mirza ")
        list.add("mirza jsf")

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
            //            circularWheelPicker_one.setCurrentPosition(position.text.toString().toInt())
            val param = right.layoutParams as ConstraintLayout.LayoutParams
            param.width = dpToPx(position.text.toString().toInt())
            right.layoutParams = param
        }
        right.setOnClickListener {
            circularWheelPicker_two.setCurrentPosition(position.text.toString().toInt())
        }
    }

}
