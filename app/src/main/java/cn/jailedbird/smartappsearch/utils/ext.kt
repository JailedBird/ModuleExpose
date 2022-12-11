package cn.jailedbird.smartappsearch.utils

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import cn.jailedbird.smartappsearch.App
import com.github.promeg.pinyinhelper.Pinyin

internal fun String?.toast() {
    val s = this
    if (!s.isNullOrEmpty()) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(App.applicationContext, s, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(App.applicationContext, s, Toast.LENGTH_SHORT).show()
        }
    }
}

internal fun Any?.log() {
    val s = this?.toString() ?: return
    if (s.isNotEmpty()) {
        Log.d(App.appName, s)
    }
}

private fun String?.isChinese(): Boolean {
    val s = this
    if (s.isNullOrEmpty()) {
        return false
    } else {
        s.forEach {
            if (Pinyin.isChinese(it)) {
                return true
            }
        }
    }
    return false
}

internal fun String?.toPinyin(): String? {
    val s = this
    return try {
        if (s.isChinese()) {
            Pinyin.toPinyin(s, EMPTY)
        } else {
            s
        }
    } catch (e: Exception) {
        s
    }
}

internal fun Context.finishProcess() {
    android.os.Process.killProcess(android.os.Process.myPid())
}

fun Int.toPx(): Float {
    val dpValue = this
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f)
}
