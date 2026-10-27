package com.listpro.xyon.pro

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.listpro.xyon.pro.data.CatalogWorker
import com.listpro.xyon.pro.data.Repository

class ListProApp : Application() {
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = Repository(this)
        WorkManager.initialize(this, Configuration.Builder().build())
        CatalogWorker.schedule(this)
    }
}
