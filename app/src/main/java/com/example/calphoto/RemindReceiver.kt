package com.example.calphoto

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat


class RemindReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = "com.example.calphoto.remind.id"
        const val NOTIFICATION_NAME = "com.example.calphoto.remind.name"
        const val NOTIFICATION_TIME = "com.example.calphoto.remind.time"
        const val NOTIFICATION_DATABASE_ID = "com.example.calphoto.remind.datebase_id"
        const val CHANNEL_ID = "com.example.calphoto.remind"
        const val CHANNEL_NAME = "リマインダー"
        const val CHANNEL_DESCRIPTION = "登録された画像が登録日時にリマインドされます"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = intent.getLongExtra(NOTIFICATION_ID, 0)
        val name = intent.getStringExtra(NOTIFICATION_NAME)
        val time = intent.getStringExtra(NOTIFICATION_TIME)
        val database_id = intent.getLongExtra(NOTIFICATION_DATABASE_ID, -1)
        notificationManager.notify(id.toInt(), buildNotification(context, name, time, database_id))
    }

    private fun buildNotification(context: Context, name: String?, time: String?, database_id: Long?): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(name)
            .setContentText("設定日時: $time")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (database_id != null) {
            val (_name, _time, bitmap) = CalendarDBHelper.get(context).getFromId(database_id)
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
        }
        return builder.build()
    }
}