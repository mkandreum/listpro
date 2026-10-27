package com.listpro.xyon.pro.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.listpro.xyon.pro.ListProApp
import java.util.concurrent.TimeUnit

/** Baja los precios nuevos en segundo plano para que la app abra ya actualizada, incluso sin cobertura en el súper. */
class CatalogWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = (applicationContext as ListProApp).repository
        repo.refreshCatalog()
        repo.pullSync()
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<CatalogWorker>(12, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("catalog", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
