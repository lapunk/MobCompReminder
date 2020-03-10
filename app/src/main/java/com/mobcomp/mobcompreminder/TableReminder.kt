package com.mobcomp.mobcompreminder

import androidx.room.*

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name = "time") var time: Long?,
    @ColumnInfo(name = "location") var location: String?,
    @ColumnInfo(name = "message") var msg: String
)

@Dao
interface ReminderDao{
    @Transaction @Insert
    fun insert(reminder: Reminder): Long

    @Query("DELETE FROM reminders WHERE uid= :id")
    fun delete(id: Int)

    @Query("Select * FROM reminders")
    fun getReminders(): List<Reminder>
}
