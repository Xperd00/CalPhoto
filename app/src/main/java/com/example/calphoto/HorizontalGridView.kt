package com.example.calphoto

import android.content.ClipData
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class HorizontalGridView : RecyclerView {
    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, set: AttributeSet?) : super(context, set) {
        initialize(context)
    }

    constructor(context: Context, set: AttributeSet?, defaultAttr: Int) : super(context, set, defaultAttr) {
        initialize(context)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    private fun initialize(context: Context) {
        val manager = GridLayoutManager(context, 2)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        this.layoutManager = manager
    }

    private var mListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
        //adapter.mListener = mListener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setOnItemClickListener(null)
        adapter = null
        layoutManager = null
    }

    private class SimpleViewHolder(view: View) : ViewHolder(view)

    class PhotoGridAdapter(list: MutableList<Uri>) : Adapter<ViewHolder>() {
        private var mDataList: MutableList<Uri>?
        private var mViews: MutableList<View>?
        internal var mListener: OnItemClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return SimpleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false))
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView as ImageView
            val uri = getItem(position)
            Picasso.get().load(uri).into(view)
            mViews!!.add(holder.itemView)
            holder.itemView.setOnLongClickListener {
                view.alpha = 0.3f
                view.startDragAndDrop(
                        ClipData.newUri(view.context.contentResolver, "Uri", uri),
                        PhotoDragShadowBuilder(view), view, 0)
                true
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            for (view in mViews!!) view.setOnClickListener(null)
            mViews = null
            mListener = null
            mDataList = null
        }

        override fun getItemCount(): Int {
            return mDataList!!.size
        }

        private fun getItem(position: Int): Uri {
            return mDataList!![position]
        }

        fun resetAllItem(list: MutableList<Uri>) {
            mDataList?.clear()
            mDataList?.addAll(list)
            notifyDataSetChanged()
        }

        init {
            mDataList = list
            mViews = ArrayList(list.size)
        }
    }

    class PhotoDragShadowBuilder(view: View) : DragShadowBuilder(view) {
        override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
            outShadowSize.set(view.width / 2, view.height / 2)
            outShadowTouchPoint.set(view.width / 4, view.height / 4)
        }
    }
}