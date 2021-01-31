package com.example.calphoto;

import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.widget.FrameLayout
import net.steamcrafted.lineartimepicker.view.YearDialView


internal class JpnLinearDatePickerDialog : BaseLinearPickerDialog {
    private val defaultBtnCallback: ButtonCallback = object : ButtonCallback {
        override fun onPositive(dialog: DialogInterface?, year: Int, month: Int, day: Int) {
            dialog!!.dismiss()
        }

        override fun onNegative(dialog: DialogInterface?) {
            dialog!!.dismiss()
        }
    }
    private val mBtnCallback: ButtonCallback
    private val mYear: Int
    private val mMinYear: Int
    private val mMaxYear: Int

    override fun getLayoutResourceId(): Int {
        return R.layout.ltp_dialog_date
    }

    protected constructor(
        context: Context?, btnCallback: ButtonCallback, bgColor: Int,
        textColor: Int, lineColor: Int, textbgcolor: Int, buttonColor: Int,
        dialogBgColor: Int, year: Int, minYear: Int, maxYear: Int, showTutorial: Boolean
    ) : super(
        context,
        bgColor,
        textColor,
        lineColor,
        textbgcolor,
        buttonColor,
        dialogBgColor,
        showTutorial
    ) {
        mBtnCallback = btnCallback
        mYear = year
        mMinYear = minYear
        mMaxYear = maxYear
        init()
    }

    protected constructor(
        context: Context?,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener) {
        mBtnCallback = defaultBtnCallback
        mYear = 0
        mMinYear = DEFAULT_MIN_YEAR
        mMaxYear = DEFAULT_MAX_YEAR
        init()
    }

    protected constructor(context: Context?, themeResId: Int) : super(context, themeResId) {
        mBtnCallback = defaultBtnCallback
        mYear = 0
        mMinYear = DEFAULT_MIN_YEAR
        mMaxYear = DEFAULT_MAX_YEAR
        init()
    }

    private fun init() {
        val v = contentView.findViewById(R.id.ltp) as JpnLinearDatePickerView
        val yearView = YearDialView(getContext())
        toolbar.addView(
            yearView,
            0,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        yearView.setMinYear(mMinYear)
        yearView.setMaxYear(mMaxYear)
        yearView.selectedYear = mYear
        btnApply.setOnClickListener{
            mBtnCallback.onPositive(this@JpnLinearDatePickerDialog, yearView.selectedYear, v.month, v.day)
            dismiss()
        }
        btnCancel.setOnClickListener{
            mBtnCallback.onNegative(this@JpnLinearDatePickerDialog)
            dismiss()
        }
    }

    internal class Builder constructor(c: Context) : BaseLinearPickerDialog.Builder<Builder>(c) {
        private var mBtnCallback: ButtonCallback = object : ButtonCallback {
            override fun onPositive(dialog: DialogInterface?, year: Int, month: Int, day: Int) {
                dialog!!.dismiss()
            }

            override fun onNegative(dialog: DialogInterface?) {
                dialog!!.dismiss()
            }
        }
        private var mYear = 0
        private var mMinYear = DEFAULT_MIN_YEAR
        private var mMaxYear = DEFAULT_MAX_YEAR
        fun setButtonCallback(buttonCallback: ButtonCallback): Builder {
            mBtnCallback = buttonCallback
            return this
        }

        fun setYear(year: Int): Builder {
            mYear = year
            return this
        }

        fun setMinYear(year: Int): Builder {
            mMinYear = year
            return this
        }

        fun setMaxYear(year: Int): Builder {
            mMaxYear = year
            return this
        }

        fun build(): JpnLinearDatePickerDialog {
            return JpnLinearDatePickerDialog(
                mContext, mBtnCallback, mbgColor, mTextColor, mLineColor,
                mTextBgColor, mButtonColor, mDialogBgColor, mYear, mMinYear, mMaxYear, mShowTutorial
            )
        }

        companion object {
            fun with(c: Context): Builder {
                return Builder(c)
            }
        }
    }

    interface ButtonCallback {
        fun onPositive(dialog: DialogInterface?, year: Int, month: Int, day: Int)
        fun onNegative(dialog: DialogInterface?)
    }

    companion object {
        private const val DEFAULT_MIN_YEAR = 1900
        private const val DEFAULT_MAX_YEAR = 3000
    }
}