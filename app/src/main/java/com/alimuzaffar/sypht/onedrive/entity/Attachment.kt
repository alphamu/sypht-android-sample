package com.alimuzaffar.sypht.onedrive.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "attachment")
class Attachment @Ignore constructor(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: String,
    @field:ColumnInfo(name = "email_id") @field:PrimaryKey val emailId: String,
    @field:ColumnInfo(name = "sypht_id") @field:PrimaryKey var syphtId: String? = null,
    @field:ColumnInfo(name = "name") @field:PrimaryKey val name: String,
    @field:ColumnInfo(name = "content_type") val contentType: String,
    @field:ColumnInfo(name = "content_bytes") val contentBytes: String,
    @field:ColumnInfo(name = "uploaded") var uploaded: Boolean = false,
    @field:ColumnInfo(name = "skip") var skip: Boolean = false
)