package com.example.calphoto

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.kizitonwose.calendarview.model.CalendarDay
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CalendarDBHelper(context: Context, databaseName:String, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, databaseName, factory, version) {
    companion object {
        const val DATABASE_NAME = "com.example.calphoto"
        const val TABLE_NAME = "events"
        fun get(context: Context): CalendarDBHelper {
            return CalendarDBHelper(context, DATABASE_NAME, null, 1)
        }
    }

    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL("create table if not exists events (id integer primary key autoincrement, name text, time text, image BLOB)");
    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        /*if (oldVersion < newVersion) {
            database?.execSQL("alter table SampleTable add column deleteFlag integer default 0")
        }*/
    }

    fun getIds(day: CalendarDay): ArrayList<Int> {
        val idList: ArrayList<Int> = ArrayList()
        try {
            val cursor = readableDatabase.rawQuery(
                "select id from $TABLE_NAME where '" + day.date.format(
                    DateTimeFormatter.ISO_LOCAL_DATE
                ) + "' <= time and time < '" + day.date.plusDays(1)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE) + "'", null
            )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    idList.add(cursor.getInt(0))
                    cursor.moveToNext()
                }
            }
        } catch (exception: Exception) {
            println("select error: $exception");
        }
        return idList
    }

    fun getFromId(id: Long): Triple<String, String, Bitmap?> {
        var name = ""
        var time = ""
        var bitmap: Bitmap? = null
        try {
            val cursor = readableDatabase.rawQuery("select * from events where id == $id", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    name = cursor.getString(1)
                    time = LocalDateTime.parse(cursor.getString(2), DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(DateTimeFormatter.ofPattern("HH:mm"))
                    val blob: ByteArray = cursor.getBlob(3)
                    bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)
                    cursor.moveToNext()
                }
            }
        } catch(exception: Exception) {
            println("select error: $exception");
        }
        return Triple(name, time, bitmap)
    }

    fun create(name: String, time: LocalDateTime, bitmap: Bitmap): Long {
        try {
            val values = ContentValues()
            values.put("name", name)
            values.put("time", time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            values.put("image", bytes)

            return writableDatabase.insertOrThrow("events", null, values)
        }
        catch (exception: Exception) {
            println("insert error: $exception")
            return -1
        }
    }

    fun delete(whereId: Int) {
        try {
            writableDatabase.delete(TABLE_NAME, "id = ?", arrayOf(whereId.toString()))
        }
        catch(exception: Exception) {
            println("delete error: $exception")
        }
    }
}