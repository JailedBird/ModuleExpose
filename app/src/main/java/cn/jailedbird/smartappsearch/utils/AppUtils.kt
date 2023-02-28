package cn.jailedbird.smartappsearch.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import cn.jailedbird.smartappsearch.data.entity.AppModel
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
            resolveInfoList.forEach { reInfo ->
                val activityName = reInfo.activityInfo.name
                val pkgName = reInfo.activityInfo.packageName
                val appLabel = reInfo.loadLabel(pm) as String

                val item = AppModel(
                    appPackageName = pkgName,
                    appName = appLabel,
                    appNamePinyin = appLabel.toPinyin()?.lowercase(Locale.ENGLISH),
                    activityName = activityName
                )
                item.toString().log()
                res.add(item)
            }
            startTime.timer("getAppsFromPackageManager", false)
            return@withContext res
        }

    suspend fun updateMeta(
        context: Context,
        old: List<AppModel>
    ): List<AppModel> {
        return mergeMemoryWithRoom(getAppsFromPackageManager(context), old)
    }

    private fun mergeMemoryWithRoom(
        new: List<AppModel>,
        old: List<AppModel>
    ): List<AppModel> {
        val newSet = new.toMutableSet()
        val now = System.currentTimeMillis()
        for (item in newSet) {
            item.count = 0
            item.timestamp = now
        }
        val mixed = mutableListOf<AppModel>()
        for (item in old) {
            if (newSet.remove(item)) {
                mixed.add(item)
            }
        }
        newSet.addAll(mixed)
        return newSet.toList()
    }


}