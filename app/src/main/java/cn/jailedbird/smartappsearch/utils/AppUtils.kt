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

    private fun getDiff(
        new: List<AppModel>,
        old: List<AppModel>
    ): Pair<List<AppModel>, List<AppModel>> {
        val toAdd = new.minus(old.toSet())
        val toDelete = old.minus(new.toSet())
        return Pair(toAdd, toDelete)
    }

    suspend fun updateMeta(
        context: Context,
        old: List<AppModel>
    ): Pair<List<AppModel>, List<AppModel>> {
        return getDiff(getAppsFromPackageManager(context), old)
    }


}