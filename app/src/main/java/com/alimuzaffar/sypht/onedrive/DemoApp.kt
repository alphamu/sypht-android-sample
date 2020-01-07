package com.alimuzaffar.sypht.onedrive

import android.app.Application
import android.content.Context
import com.alimuzaffar.sypht.onedrive.database.TheDatabase
import com.alimuzaffar.sypht.onedrive.repo.SyphtRepo

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
        TheDatabase.get(this).clearAll()
        SyphtRepo.init(getString(R.string.sypht_client_id), getString(R.string.sypht_client_secret))
    }
    companion object {
        lateinit var ctx: Context
        fun init(c: Context) {
            if (!::ctx.isInitialized)
                ctx = c
        }
    }
}