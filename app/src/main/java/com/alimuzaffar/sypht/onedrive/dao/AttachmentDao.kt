package com.alimuzaffar.sypht.onedrive.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alimuzaffar.sypht.onedrive.entity.Attachment

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachment where email_id = :emailId")
    fun getAllAttachments(emailId: String): List<Attachment>

    @Query("SELECT COUNT(*) FROM attachment")
    fun getAttachmentCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM attachment where uploaded = 0")
    fun getAttachmentNotUploadedCount(): Integer

    @Insert
    fun addAttachment(attachment: Attachment): Long

    @Update
    fun updateAttachment(attachment: Attachment)

    @Query("UPDATE attachment SET uploaded = 1 WHERE id = :id")
    fun updateUploaded(id: String)

    @Query("DELETE FROM attachment")
    fun clearAll()
}