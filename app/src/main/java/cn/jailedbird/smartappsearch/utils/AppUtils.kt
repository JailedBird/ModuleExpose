package cn.jailedbird.smartappsearch.utils

import android.content.Context
import cn.jailedbird.smartappsearch.data.AppDatabase
import cn.jailedbird.smartappsearch.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

object AppUtils {
    private suspend fun getAppsFromPackageManager(context: Context): List<AppModel> =
        withContext(Dispatchers.IO) {
            val startTime = System.nanoTime()
            val packageManager = context.packageManager
            val apps = mutableListOf<AppModel>()
            var index = 0
            packageManager.getInstalledApplications(0).forEach {
                val packageName = it.packageName
                if (packageName.startsWith("com.google") ||
                    packageName.startsWith("com.android") ||
                    packageName.startsWith("android")
                ) {
                    "Skip $packageName".log()
                } else {
                    val appName = packageManager.getApplicationLabel(it).toString()
                    apps.add(
                        AppModel(
                            appId = index++,
                            appPackageName = it.packageName,
                            appName = appName,
                            appNamePinyin = appName.toPinyin()?.lowercase(Locale.ENGLISH)
                        )
                    )
                }
            }
            "getAppsFromPackageManager() cost ${(System.nanoTime() - startTime) / 1000_000} ms".apply {
                this.toast()
                this.log()
            }
            return@withContext apps
        }

    private suspend fun saveAppsToRoom(apps: List<AppModel>) {
        if (apps.isNotEmpty()) {
            AppDatabase.getInstance().appModelDao().insertAll(apps)
        }
    }

    suspend fun getAppsFromRoom(): List<AppModel> {
        val startTime = System.nanoTime()
        val res = AppDatabase.getInstance().appModelDao().getApps()
        "getAppsFromRoom() cost ${(System.nanoTime() - startTime) / 1000_000} ms".apply {
            this.toast()
            this.log()
        }
        return res
    }

    /**
     * Get origin PackageInfo from PackageManager, and save it into room
     * */
    suspend fun refresh(context: Context): List<AppModel> {
        val res = getAppsFromPackageManager(context)
        saveAppsToRoom(res)
        return res
    }
}