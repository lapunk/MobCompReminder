package com.mobcomp.mobcompreminder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.list_view_item.view.*

class ReminderAdapter(context: Context, private val array: Array<String>): BaseAdapter() {

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {

        val row = inflater.inflate(R.layout.list_view_item, parent, false)

        row.itemMessage.text = array[position]
        row.itemTrigger.text = array[position]
        return row

    }

    override fun getItem(position: Int): Any {

        return array[position]

    }

    override fun getItemId(position: Int): Long {

        return position.toLong()

    }

    override fun getCount(): Int {

        return array.size

    }


}