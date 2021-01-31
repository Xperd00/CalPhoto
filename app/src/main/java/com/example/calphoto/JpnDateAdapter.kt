package com.example.calphoto

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import net.steamcrafted.lineartimepicker.adapter.BaseTextAdapter
import net.steamcrafted.lineartimepicker.adapter.LinearPickerAdapter


class JpnDateAdapter(context: Context?, textPaint: Paint?) : BaseTextAdapter(context, textPaint) {
    override fun getLargePipCount(): Int {
        return 13
    }

    override fun getSmallPipCount(): Int {
        return 9
    }

    override fun getInvisiblePipCount(visiblePipIndex: Int): Int {
        val month = visiblePipIndex / (smallPipCount + 1)
        val part = visiblePipIndex % (smallPipCount + 1)
        val r = if (part == smallPipCount) {
            getDaysInMonth(month) - 3 * part - 1
        } else 2
        println("visiblePipIndex:$visiblePipIndex return:$r")
        return if (r < 0) 0 else r
    }

    override fun onDraw(canvas: Canvas?, elementBounds: Array<Rect?>?, gravity: LinearPickerAdapter.Gravity?) {}

    override fun getLabelText(index: Int): String {
        return if (index == 12) "" else (index + 1).toString()
    }

    override fun getHandleText(index: Int, step: Int): String {
        println("index:$index step:$step")
        return getMonth(index).toString() + "/" + getDay(index, step)
    }

    private fun getMonthIndex(index: Int): Int {
        return index / (smallPipCount + 1)
    }

    fun getMonth(index: Int): Int {
        val m = getMonthIndex(index) + 1
        return if (m > 12) 12 else m
    }

    fun getDay(index: Int, step: Int): Int {
        val days = getDaysInMonth(getMonthIndex(index))
        val part = index % (smallPipCount + 1)
        return Math.min(index % (smallPipCount + 1) * 3 + step + 1, days)
    }

    private fun getDaysInMonth(month: Int): Int {
        return when (month % 12) {
            1 -> 28
            0, 2, 4, 6, 7, 9, 11 -> 31
            else -> 30
        }
    }
}