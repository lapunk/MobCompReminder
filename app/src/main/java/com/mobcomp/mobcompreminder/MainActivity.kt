package com.mobcomp.mobcompreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fabMap : FloatingActionButton = findViewById(R.id.fab_map)
        val fabTime : FloatingActionButton = findViewById(R.id.fab_time)

        val fabOpen : FloatingActionButton = findViewById(R.id.fab_open)
        var fabOpened = false

        fabOpen.setOnClickListener{

            if(!fabOpened)
            {
                fabOpened = true
                fabMap.animate().translationY(-resources.getDimension(R.dimen.standard_66))
                fabTime.animate().translationY(-resources.getDimension(R.dimen.standard_132))
            }
            else
            {
                fabOpened = false
                fabMap.animate().translationY(0f)
                fabTime.animate().translationY(0f)

            }
        }

        fabMap.setOnClickListener {
            val intent = Intent(applicationContext, MapActivity::class.java)
            startActivity(intent)
        }

        fabTime.setOnClickListener{
            val intent = Intent(applicationContext, TimeActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onResume() {
        super.onResume()

        refreshList()
    }


    private fun refreshList()
    {

        doAsync {

            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminders").build()
            val reminders = db.reminderDao().getReminders()
            db.close()

            uiThread {

                if(reminders.isNotEmpty())
                {
                    val adapter = ReminderAdapter(applicationContext, reminders)

                    list.adapter = adapter
                }
                else
                {
                    toast("No reminders")
                }
            }
        }
    }
    companion object
    {
        private const val CHANNEL_ID = "REMINDER_CHANNEL_ID"
        private const val NotificationID = 1567
        fun showNotification(context: Context, message: String) {
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_24px)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message).setStyle(NotificationCompat.BigTextStyle().bigText(message)).setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            {
                val channel = NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT).apply { description = context.getString(R.string.app_name) }

                    notificationManager.createNotificationChannel(channel)
            }
            val notification = NotificationID+ Random(NotificationID).nextInt(1, 30)
            notificationManager.notify(notification, notificationBuilder.build())

        }
    }
}