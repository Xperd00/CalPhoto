package com.example.calphoto

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.squareup.picasso.Picasso
import net.steamcrafted.lineartimepicker.dialog.LinearTimePickerDialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class NewEventActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        val uri = intent.getParcelableExtra<Uri>(MainActivity.DATA_PHOTO_URI)
        val dayLong = intent.getLongExtra(MainActivity.DATA_DAY, 0)
        val date = LocalDate.ofEpochDay(dayLong)
        //Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, date.toString(), Toast.LENGTH_SHORT).show()

        Picasso.get().load(uri).into(findViewById<ImageView>(R.id.edit_image))
        findViewById<EditText>(R.id.edit_date).setText(
            String.format(
                "%d/%02d/%02d",
                date.year,
                date.monthValue,
                date.dayOfMonth
            )
        )

        val context = this
        val datePicker = JpnLinearDatePickerDialog.Builder.with(this)
            // Year that will be selected when the dialog is shown
            .setYear(date.year)
            // Minimum year that is allowed to be selected (inclusive)
            .setMinYear(LocalDate.now().year)
            // Maximum year that is allowed to be selected (inclusive)
            //.setMaxYear(int year)
            // Set the background color of the dialog (1)
            .setDialogBackgroundColor(resources.getColor(R.color.LightGray))
            // Set the background color of the picker inside the dialog (2)
            .setPickerBackgroundColor(resources.getColor(R.color.white))
            // Set the color of the unselected lines in the linear dial (3)
            .setLineColor(resources.getColor(R.color.black))
            // Set the color of all the displayed text
            .setTextColor(resources.getColor(R.color.black))
            // Show a short 10 second automated tutorial to onboard the user
            .setShowTutorial(false)
            // Set the background color of the "handle" (4)
            .setTextBackgroundColor(resources.getColor(R.color.purple_200))
            // Set the color of the two buttons at the top of the dialog (5)
            .setButtonColor(resources.getColor(R.color.white))
            // Register a callback when the selection process is completed or canceled
            .setButtonCallback(object : JpnLinearDatePickerDialog.ButtonCallback {
                override fun onPositive(dialog: DialogInterface?, year: Int, month: Int, day: Int) {
                    context.findViewById<EditText>(R.id.edit_date).setText(
                        String.format(
                            "%d/%02d/%02d",
                            year,
                            month,
                            day
                        )
                    )
                }

                override fun onNegative(dialog: DialogInterface?) {

                }
            }).build()
        findViewById<Button>(R.id.edit_date_button).setOnClickListener { datePicker.show() }

        val timePicker = LinearTimePickerDialog.Builder.with(this)
            // Set the background color of the dialog (1)
            .setDialogBackgroundColor(resources.getColor(R.color.LightGray))
            // Set the background color of the picker inside the dialog (2)
            .setPickerBackgroundColor(resources.getColor(R.color.white))
            // Set the color of the unselected lines in the linear dial (3)
            .setLineColor(resources.getColor(R.color.black))
            // Set the color of all the displayed text
            .setTextColor(resources.getColor(R.color.black))
            // Show a short 10 second automated tutorial to onboard the user
            .setShowTutorial(false)
            // Set the background color of the "handle" (4)
            .setTextBackgroundColor(resources.getColor(R.color.purple_200))
            // Set the color of the two buttons at the top of the dialog (5)
            .setButtonColor(resources.getColor(R.color.white))
            // Register a callback when the selection process is completed or canceled
            .setButtonCallback(object : LinearTimePickerDialog.ButtonCallback {
                override fun onPositive(dialog: DialogInterface, hour: Int, minute: Int) {
                    context.findViewById<EditText>(R.id.edit_time).setText(
                        String.format(
                            "%02d:%02d",
                            hour,
                            minute
                        )
                    )
                }

                override fun onNegative(dialog: DialogInterface) {

                }
            }).build()
        findViewById<Button>(R.id.edit_time_button).setOnClickListener { timePicker.show() }

        findViewById<Button>(R.id.button_save).setOnClickListener {
            val datetime = findViewById<EditText>(R.id.edit_date).text.toString() + " " + findViewById<EditText>(R.id.edit_time).text.toString()
            val dt: LocalDateTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
            val database_id = saveEvent(dt)
            if (database_id >= 0) {
                val notificationIntent = Intent(this, RemindReceiver::class.java)
                notificationIntent.putExtra(RemindReceiver.NOTIFICATION_ID, System.currentTimeMillis())
                notificationIntent.putExtra(RemindReceiver.NOTIFICATION_NAME, findViewById<EditText>(R.id.edit_name).text.toString())
                notificationIntent.putExtra(RemindReceiver.NOTIFICATION_TIME, datetime)
                notificationIntent.putExtra(RemindReceiver.NOTIFICATION_DATABASE_ID, database_id)
                val pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pendingIntent)
                finish()
            }
        }
        findViewById<Button>(R.id.button_cancel).setOnClickListener { finish() }
    }

    private fun saveEvent(datetime: LocalDateTime): Long {
        if (findViewById<EditText>(R.id.edit_date).text.toString() == "" || findViewById<EditText>(R.id.edit_time).text.toString() == "") {
            Toast.makeText(this, "日付と時刻は必須です", Toast.LENGTH_SHORT).show()
            return -1
        }
        return CalendarDBHelper.get(this).create(
            findViewById<EditText>(R.id.edit_name).text.toString(), datetime,//datetime.replace('/', '-') + ":00",
            findViewById<ImageView>(R.id.edit_image).drawable.toBitmap()
        )
    }
}