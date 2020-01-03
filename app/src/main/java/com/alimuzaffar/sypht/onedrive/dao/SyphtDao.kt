package com.alimuzaffar.sypht.onedrive.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alimuzaffar.sypht.onedrive.entity.SyphtResult

@Dao
interface SyphtDao {
    @Insert
    fun add(result: SyphtResult)

    @Update
    fun update(result: SyphtResult)

    @Query("SELECT * FROM sypht_result WHERE attachment_id = :attachmentId")
    fun getResultForAttachment(attachmentId: String): SyphtResult?

    @Query("SELECT * FROM sypht_result WHERE sypht_id = :syphtId")
    fun getResultForSyphtId(syphtId: String): SyphtResult

    @Query("SELECT * FROM sypht_result WHERE email_id = :emailId")
    fun getResultsForEmail(emailId: String): List<SyphtResult>

    @Query("SELECT * FROM sypht_result WHERE email_id = :emailId AND result IS NOT NULL")
    fun getFinalisedResultsForEmail(emailId: String): List<SyphtResult>

    @Query("DELETE FROM sypht_result")
    fun clearAll()
}