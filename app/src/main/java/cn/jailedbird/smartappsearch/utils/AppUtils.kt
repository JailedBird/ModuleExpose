package cn.jailedbird.smartappsearch.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import cn.jailedbird.smartappsearch.data.AppDatabase
import cn.jailedbird.smartappsearch.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


object AppUtils {

    private suspend fun getAppsFromPackageManager(context: Context): List<AppModel> =
        withContext(Dispatchers.IO) {
            val startTime = System.nanoTime()
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            @Suppress("DEPRECATION")
            val resolveInfoList: List<ResolveInfo> =
                pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
            Collections.sort(resolveInfoList, ResolveInfo.DisplayNameComparator(pm))
            val res = mutableListOf<AppModel>()
            var index = 0
            resolveInfoList.forEach { reInfo ->
                val activityName = reInfo.activityInfo.name
                val pkgName = reInfo.activityInfo.packageName
                val appLabel = reInfo.loadLabel(pm) as String

                val launchIntent = Intent()
                launchIntent.component = ComponentName(
                    pkgName,
                    activityName
                )
                val item = AppModel(
                    appId = index++,
                    appPackageName = pkgName,
                    appName = appLabel,
                    appNamePinyin = appLabel.toPinyin()?.lowercase(Locale.ENGLISH)
                )
                item.toString().log()
                res.add(item)
            }
            "getAppsFromPackageManager() cost ${(System.nanoTime() - startTime) / 1000_000} ms".apply {
                this.toast()
                this.log()
            }
            return@withContext res
        }

    private suspend fun saveAppsToRoom(apps: List<AppModel>) = withContext(Dispatchers.IO) {
        if (apps.isNotEmpty()) {
            val startTime = System.nanoTime()
            AppDatabase.getInstance().appModelDao().insertAll(apps)
            "saveAppsToRoom() cost ${(System.nanoTime() - startTime) / 1000_000} ms".apply {
                this.toast()
                this.log()
            }
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