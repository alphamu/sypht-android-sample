package com.alimuzaffar.sypht.onedrive.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "email")
class Email constructor(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: String,
    @field:ColumnInfo(name = "subject") val subject: String,
    @field:ColumnInfo(name = "from") val from: String,
    @field:ColumnInfo(name = "received") val received: String,
    @field:ColumnInfo(name = "processing_finished") var finished: Boolean = true
)