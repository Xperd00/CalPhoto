package com.example.calphoto

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.view.DragEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList


const val PERMISSION_REQUEST_CODE: Int = 2

class MainActivity : AppCompatActivity() {
    companion object {
        const val DATA_PHOTO_URI = "com.example.calphoto.DATA_PHOTO_URI"
        const val DATA_DAY = "com.example.calphoto.DATA_DAY"
    }

    private lateinit var calendarView: CalendarView
    private lateinit var eventsView: ListView
    private lateinit var photosView: HorizontalGridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.dayBinder = object: DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view, this@MainActivity)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.textView.text = day.date.dayOfMonth.toString()
                container.textView.setTextColor(if (day.owner == DayOwner.THIS_MONTH) Color.BLACK else Color.GRAY)
                container.textView.setOnDragListener(DragListener(this@MainActivity, day))
                container.setStatus(CalendarDBHelper.get(this@MainActivity).getIds(day).size > 0)
            }
        }
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                @SuppressLint("SetTextI18n") // Concatenation warning for `setText` call.
                container.textView.text = "${month.year}年 ${month.yearMonth.month.value}月"
            }
        }

        photosView = findViewById(R.id.photosView)
        photosView.adapter = HorizontalGridView.PhotoGridAdapter(mutableListOf())
        
        eventsView = findViewById(R.id.eventsView)
        eventsView.scrollBarStyle = ListView.SCROLLBARS_OUTSIDE_OVERLAY
        //listView.addHeaderView(header, null, false)
        //listView.addFooterView(footer, null, false)
        eventsView.adapter = EventListAdapter(applicationContext)
        eventsView.onItemClickListener = AdapterView.OnItemClickListener { listView, _, pos, _ ->
            run {
                AlertDialog.Builder(this)
                    .setTitle("この画像を削除しますか？")
                    .setMessage("リマインダーも削除されます")
                    .setPositiveButton("OK") { _, _ ->
                        val adapter = listView.adapter as EventListAdapter
                        adapter.getItem(pos)?.let {
                            CalendarDBHelper.get(this).delete(it)
                        }
                        val ids = currentShowDay?.let { CalendarDBHelper.get(this).getIds(it) }
                        if (ids != null) adapter.refresh(ids)
                        if (ids == null || ids.size <= 0) {
                            currentShowDayNotifyView?.visibility = View.INVISIBLE
                            currentShowDay?.let { backGallery(it) }
                        }
                    }
                    .setNegativeButton("キャンセル", null)
                    .show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestReadStorage()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ((findViewById<View>(R.id.photosView) as HorizontalGridView).adapter as HorizontalGridView.PhotoGridAdapter).resetAllItem(
                getAllImageList()
            )
        }

        findViewById<CalendarView>(R.id.calendarView).notifyCalendarChanged()
        /*currentShowDay?.let {
            CalendarDBHelper.get(this).getIds(it) }?.let {
                (findViewById<ListView>(R.id.eventsView).adapter as EventListAdapter).refresh(it)
        }*/
    }

    @SuppressLint("ShowToast")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestReadStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                makeText(this, "このアプリを使うにはストレージへのアクセスを許可してください", Toast.LENGTH_LONG)
                //requestReadStorage()
            }
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
        else {
            // ここに許可済みの時の動作を書く
            (photosView.adapter as HorizontalGridView.PhotoGridAdapter).resetAllItem(getAllImageList())
        }
    }

    @SuppressLint("Recycle")
    private fun getAllImageList(): MutableList<Uri> {
        val listOfAllImages: MutableList<Uri> = mutableListOf()
        val projection = arrayOf(Media._ID, Media.DATE_ADDED)
        var imageId: Long
        val cursor = contentResolver.query(
            Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            Media.DATE_ADDED + " DESC"
        )
        if (cursor != null) {
            val columnIndexID = cursor.getColumnIndexOrThrow(Media._ID)
            while (cursor.moveToNext()) {
                imageId = cursor.getLong(columnIndexID)
                val uriImage = Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI, "" + imageId)
                listOfAllImages.add(uriImage)
            }
            cursor.close()
        }
        return listOfAllImages
    }

    private var currentShowDay: CalendarDay? = null
    private var currentShowDayView: TextView? = null
    private var currentShowDayNotifyView: TextView? = null
    fun handleDayClick(view: View, day: CalendarDay) {
        val idList: ArrayList<Int> = ArrayList()
        idList.addAll(CalendarDBHelper.get(this).getIds(day))
        if (idList.size <= 0 || currentShowDay != null && day.date.isEqual(currentShowDay?.date)) {
            if (idList.size <= 0) makeText(this, "この日には画像が登録されていません", Toast.LENGTH_SHORT).show()
            backGallery(day)
        }
        else showEvents(view, day)
        (eventsView.adapter as EventListAdapter).refresh(idList)
    }
    
    private fun backGallery(day: CalendarDay) {
        if (currentShowDayView != null) {
            currentShowDayView!!.setBackgroundColor(Color.TRANSPARENT)
            currentShowDayView!!.setTextColor(if (day.owner == DayOwner.THIS_MONTH) Color.BLACK else Color.GRAY)
        }

        currentShowDay = null
        currentShowDayView = null
        currentShowDayNotifyView = null

        eventsView.visibility = View.GONE
        photosView.visibility = View.VISIBLE
    }
    
    private fun showEvents(view: View, day: CalendarDay) {
        if (currentShowDayView != null) {
            currentShowDayView!!.setBackgroundColor(Color.TRANSPARENT)
            currentShowDayView!!.setTextColor(if (day.owner == DayOwner.THIS_MONTH) Color.BLACK else Color.GRAY)
        }

        currentShowDay = day
        currentShowDayView = view.findViewById(R.id.day_text)
        currentShowDayNotifyView = view.findViewById(R.id.day_notify)

        view.findViewById<TextView>(R.id.day_text).setTextColor(Color.WHITE)
        view.findViewById<TextView>(R.id.day_text).setBackgroundColor(resources.getColor(R.color.purple_500))

        eventsView.visibility = View.VISIBLE
        photosView.visibility = View.INVISIBLE
    }

    class DayViewContainer(view: View, activity: MainActivity) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.day_text)
        // Will be set when this container is bound
        lateinit var day: CalendarDay

        fun setStatus(boolean: Boolean) {
            view.findViewById<TextView>(R.id.day_notify).visibility = if (boolean) View.VISIBLE else View.INVISIBLE
        }

        init {
            view.setOnClickListener {
                activity.handleDayClick(view, day)
            }
        }
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.monthName)
    }


    class DragListener(context: Context, day: CalendarDay) : View.OnDragListener {
        private var mContext: Context = context
        private var mDay: CalendarDay = day
        private var res: Resources = context.resources

        override fun onDrag(v: View, event: DragEvent): Boolean {
            val textView : TextView = v as TextView
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    textView.setBackgroundColor(res.getColor(R.color.purple_200))
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    textView.setBackgroundColor(Color.TRANSPARENT)
                }
                DragEvent.ACTION_DROP -> {
                    val intent = Intent(mContext, NewEventActivity::class.java)
                    intent.putExtra(DATA_PHOTO_URI, event.clipData.getItemAt(0).uri)
                    intent.putExtra(DATA_DAY, mDay.date.toEpochDay())
                    mContext.startActivity(intent)
                    textView.setBackgroundColor(Color.TRANSPARENT)
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    val vw = event.localState as View
                    vw.alpha = 1f
                }
                else -> {
                }
            }
            return true
        }
    }
}
