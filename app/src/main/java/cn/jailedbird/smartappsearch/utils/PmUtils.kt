package cn.jailedbird.smartappsearch.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

internal fun Context.launchApk(packageName: String): Boolean {
    packageManager.getLaunchIntentForPackage(packageName)?.let {
        try {
            startActivity(it)
        } catch (e: Exception) {
            e.message.toast()
            return false
        }
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


