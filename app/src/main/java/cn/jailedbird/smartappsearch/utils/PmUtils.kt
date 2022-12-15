package cn.jailedbird.smartappsearch.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

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


