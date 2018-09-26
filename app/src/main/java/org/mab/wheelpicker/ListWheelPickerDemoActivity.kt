package org.mab.wheelpicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_list_wheel_picker_demo.*

class ListWheelPickerDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_wheel_picker_demo)
        val list = ArrayList<String>()
        (0..59).forEach {
            list.add(it.toString())
        }
        list_wheel.setDataItems(list)
    }
}
