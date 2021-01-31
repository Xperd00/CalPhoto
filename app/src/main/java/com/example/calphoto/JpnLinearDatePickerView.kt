package com.example.calphoto;

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import net.steamcrafted.lineartimepicker.adapter.DateAdapter
import net.steamcrafted.lineartimepicker.view.LinearPickerView


class JpnLinearDatePickerView : LinearPickerView {
    private val mTextPaint = Paint()
    private var mAdapter: JpnDateAdapter? = null

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        mTextPaint.isAntiAlias = true
        mAdapter = JpnDateAdapter(context, mTextPaint)
        setAdapter(mAdapter)
        setTutorialText("previous day", "next day")
    }

    val month: Int
        get() = mAdapter!!.getMonth(index)
    val day: Int
        get() = mAdapter!!.getDay(index, invisibleStep)

    override fun setActiveLineColor(color: Int) {
        mTextPaint.color = color
        super.setActiveLineColor(color)
    }
}