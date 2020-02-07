package com.mobcomp.mobcompreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_time.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import java.sql.Time
import java.util.*
import kotlin.system.measureTimeMillis

class TimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)

        time_create.setOnClickListener{

            val cal = GregorianCalendar(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.currentHour,
                timePicker.currentMinute
            )

            if((et_message.text.toString() != "") &&
                (cal.timeInMillis > System.currentTimeMillis()))
            {
                val reminder = Reminder(
                    uid = null,
                    time = cal.timeInMillis,
                    location = null,
                    msg = et_message.text.toString()
                )

                doAsync {
                    val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminders").build()
                    db.reminderDao().insert(reminder)
                    db.close()

                    setAlarm(reminder.time!!, reminder.msg)

                    finish()
                }
            }
            else
            {
                toast("Wrong data")
            }
        }
    }

    private fun setAlarm( time: Long, message: String)
    {
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("message", message)

        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setExact(AlarmManager.RTC, time, pendingIntent)

        runOnUiThread{toast("Reminder is created")}

    }
}
