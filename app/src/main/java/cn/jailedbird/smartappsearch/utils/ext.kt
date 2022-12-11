package cn.jailedbird.smartappsearch.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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

@Suppress("unused")
internal fun Context.finishProcess() {
    android.os.Process.killProcess(android.os.Process.myPid())
}

fun Int.toPx(): Float {
    val dpValue = this
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f)
}


/**
 * Avoid view's fast-click
 * */
inline fun View.setDebouncedClick(
    @Suppress("UNUSED_PARAMETER") duration: Long = 1000L,
    crossinline block: (view: View) -> Unit
) {
    setOnClickListener {
        if (DebouncingUtils.isValid(it)) {
            block.invoke(this)
        }
    }
}


fun Context.hideKeyboard() {
    if (this is Activity) {
        val window = this.window
        WindowCompat.getInsetsController(window, window.decorView)
            .hide(WindowInsetsCompat.Type.ime())
    }
}

fun Context.showKeyboard() {
    if (this is Activity) {
        val window = this.window
        WindowCompat.getInsetsController(window, window.decorView)
            .show(WindowInsetsCompat.Type.ime())
    }
}