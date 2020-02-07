package com.mobcomp.mobcompreminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab_map : FloatingActionButton = findViewById(R.id.fab_map)
        val fab_time : FloatingActionButton = findViewById(R.id.fab_time)

        val fab_open : FloatingActionButton = findViewById(R.id.fab_open)
        var fabOpened = false

        fab_open.setOnClickListener{

            if(!fabOpened)
            {
                fabOpened = true
                fab_map.animate().translationY(-resources.getDimension(R.dimen.standard_66))
                fab_time.animate().translationY(-resources.getDimension(R.dimen.standard_132))
            }
            else
            {
                fabOpened = false
                fab_map.animate().translationY(0f)
                fab_time.animate().translationY(0f)

            }
        }



        fab_map.setOnClickListener {
            val intent = Intent(applicationContext, MapActivity::class.java)
            startActivity(intent)
        }

        fab_time.setOnClickListener{
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
}
