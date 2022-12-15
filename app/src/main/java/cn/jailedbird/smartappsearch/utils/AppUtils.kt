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

    suspend fun getAppsFromPackageManager(context: Context): List<AppModel> =
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

                val item = AppModel(
                    appId = index++,
                    appPackageName = pkgName,
                    appName = appLabel,
                    appNamePinyin = appLabel.toPinyin()?.lowercase(Locale.ENGLISH),
                    activityName = activityName
                )
                item.toString().log()
                res.add(item)
            }
            startTime.timer("getAppsFromPackageManager", true)
            return@withContext res
        }


}