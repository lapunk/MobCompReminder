package com.mobcomp.mobcompreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_time.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.*

class TimeActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)

        time_create.setOnClickListener{

            val cal = GregorianCalendar(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.hour,
                timePicker.minute
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
