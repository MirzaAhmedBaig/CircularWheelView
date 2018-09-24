package org.mab.wheelpicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
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
        (1..24).forEach {
            list.add(it.toString())
        }

        circularWheelPicker_one.setDataSet(list)
        circularWheelPicker_one.setWheelItemSelectionListener(object : CircularWheelPicker.WheelItemSelectionListener {
            override fun onItemSelected(index: Int) {
                Log.d(TAG, "Selected position is : $index")
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
            }
        })
    }

}
