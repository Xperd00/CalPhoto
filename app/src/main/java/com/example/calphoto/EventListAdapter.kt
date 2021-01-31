package com.example.calphoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class EventListAdapter(context: Context) : ArrayAdapter<Int>(context, 0) {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        if (view == null)
            view = mInflater.inflate(R.layout.item_event, parent, false)

        val id = getItem(position) ?: return view!!
        val (name, time, bitmap) = CalendarDBHelper.get(context).getFromId(id.toLong())

        view!!.findViewById<TextView>(R.id.event_name).setText(name)
        view.findViewById<TextView>(R.id.event_time).setText(time)
        view.findViewById<ImageView>(R.id.event_image).setImageBitmap(bitmap)
        return view
    }

    fun refresh(idList: ArrayList<Int>) {
        clear()
        addAll(idList)
        notifyDataSetChanged()
    }
}