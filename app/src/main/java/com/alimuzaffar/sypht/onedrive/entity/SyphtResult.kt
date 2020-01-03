package com.alimuzaffar.sypht.onedrive.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sypht_result")
class SyphtResult constructor(
    @field:ColumnInfo(name = "sypht_id") @field:PrimaryKey val syphtId: String,
    @field:ColumnInfo(name = "attachment_id") val attachmentId: String,
    @field:ColumnInfo(name = "email_id") val emailId: String,
    @field:ColumnInfo(name = "result") var result: String? = null)

