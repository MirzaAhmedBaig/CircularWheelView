package org.mab.wheelpicker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setWheelPicker()
    }

    private fun setWheelPicker() {


    }

    override fun onResume() {
        super.onResume()

        val list = ArrayList<String>()
        (1..12).forEach {
            list.add(it.toString())
        }
        circularWheelPicker.setDataSet(list)
    }
}
