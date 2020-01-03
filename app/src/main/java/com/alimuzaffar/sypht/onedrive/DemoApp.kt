package com.alimuzaffar.sypht.onedrive

import android.app.Application
import com.alimuzaffar.sypht.onedrive.database.TheDatabase
import com.alimuzaffar.sypht.onedrive.repo.SyphtRepo

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TheDatabase.get(this)
        SyphtRepo.init(getString(R.string.sypht_client_id), getString(R.string.sypht_client_secret))

    }
}