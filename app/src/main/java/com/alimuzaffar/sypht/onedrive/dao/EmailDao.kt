package com.alimuzaffar.sypht.onedrive.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alimuzaffar.sypht.onedrive.entity.Email

@Dao
interface EmailDao {
    @Query("SELECT * FROM email")
    fun getAllEmails(): LiveData<List<Email>>

    @Query("SELECT COUNT(*) FROM email WHERE id = :id")
    fun contains(id: String): Long

    @Insert
    fun addEmail(email: Email): Long

    @Update
    fun updateEmail(email: Email)

    @@Query("UPDATE email SET processing_finished = :finished WHERE id = :emailId")
    fun updateProcessing(emailId: String, finished: Boolean)

    @Query("DELETE FROM email")
    fun clearAll()
}