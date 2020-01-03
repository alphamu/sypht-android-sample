package com.alimuzaffar.sypht.onedrive.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alimuzaffar.sypht.onedrive.dao.AttachmentDao
import com.alimuzaffar.sypht.onedrive.dao.EmailDao
import com.alimuzaffar.sypht.onedrive.dao.SyphtDao
import com.alimuzaffar.sypht.onedrive.entity.Attachment
import com.alimuzaffar.sypht.onedrive.entity.Email
import com.alimuzaffar.sypht.onedrive.entity.SyphtResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Email::class, Attachment::class, SyphtResult::class], version = 1
)
abstract class TheDatabase : RoomDatabase() {

    // --- DAO ---
    abstract fun emailDao(): EmailDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun syphtDao(): SyphtDao

    fun clearAll() {
        CoroutineScope(Dispatchers.IO).launch {
            emailDao().clearAll()
            attachmentDao().clearAll()
            syphtDao().clearAll()
        }
    }
    companion object {
        lateinit var instance: TheDatabase

        fun get(context: Context): TheDatabase {
            if (!::instance.isInitialized) {
                instance = Room.databaseBuilder<TheDatabase>(
                    context.applicationContext,
                    TheDatabase::class.java, "sypht.db"
                ).build()
            }
            return instance
        }
    }

}
