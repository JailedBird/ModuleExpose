package cn.jailedbird.feature.search.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import cn.jailedbird.core.common.utils.log
import cn.jailedbird.core.common.utils.timer
import cn.jailedbird.core.common.utils.toast
import cn.jailedbird.feature.search.data.entity.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.Locale

/**
 * launch via package name
 * */
private fun Context.launchApk(packageName: String): Boolean {
    packageManager.getLaunchIntentForPackage(packageName)?.let {
        try {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        } catch (e: Exception) {
            e.message.toast()
            return false
        }
    }
    return true
}

/**
 * launch via package name and activity name (Launcher)
 * */
internal fun Context.launchApk(packageName: String, activityName: String?): Boolean {
    if (activityName.isNullOrEmpty()) {
        return launchApk(packageName)
    }
    val launchIntent = Intent(Intent.ACTION_MAIN)
    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    val cn = ComponentName(
        packageName,
        activityName
    )
    launchIntent.component = cn
    // New stack
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(launchIntent)
    } catch (e: Exception) {
        e.message?.toast()
        return false
    }

    return true
}

internal fun Context.gotoApkSettings(packageName: String?) {
    if (!packageName.isNullOrEmpty()) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Remove apk method [link](https://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents)
 * */
internal fun Context.uninstallApk(packageName: String?) {
    if (!packageName.isNullOrEmpty()) {
        try {
            startActivity(Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
            })
        } catch (e: Exception) {
            e.message?.toast()
        }
    }
}


@SuppressLint("QueryPermissionsNeeded")
internal suspend fun Context.packageManagerAppList(): List<AppModel> =
    withContext(Dispatchers.IO) {
        val startTime = System.nanoTime()
        val pm = this@packageManagerAppList.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList: List<ResolveInfo> =
            pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        Collections.sort(resolveInfoList, ResolveInfo.DisplayNameComparator(pm))
        val res = mutableListOf<AppModel>()
        val now = System.currentTimeMillis()
        resolveInfoList.forEach { reInfo ->
            val activityName = reInfo.activityInfo.name
            val pkgName = reInfo.activityInfo.packageName
            val appLabel = reInfo.loadLabel(pm) as String
            val item = AppModel(
                appPackageName = pkgName,
                appName = appLabel,
                appNamePinyin = appLabel.toPinyin()?.lowercase(Locale.ENGLISH),
                activityName = activityName,
                count = 0,
                timestamp = now,
            )
            // item.toString().log()
            res.add(item)
        }
        startTime.timer("getAppsFromPackageManager", false)
        return@withContext res
    }