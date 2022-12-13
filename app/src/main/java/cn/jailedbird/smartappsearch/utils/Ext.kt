package cn.jailedbird.smartappsearch.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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
            null
        }
    } catch (e: Exception) {
        null
    }
}

@Suppress("unused")
internal fun Context.finishProcess() {
    android.os.Process.killProcess(android.os.Process.myPid())
}

internal fun Int.toPx(): Float {
    val dpValue = this
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f)
}

/**
 * Avoid view's fast-click
 * */
internal inline fun View.setDebouncedClick(
    @Suppress("UNUSED_PARAMETER") duration: Long = 1000L,
    crossinline block: (view: View) -> Unit
) {
    setOnClickListener {
        if (DebouncingUtils.isValid(it)) {
            block.invoke(this)
        }
    }
}

internal fun Context.hideKeyboard() {
    if (this is Activity) {
        val window = this.window
        WindowCompat.getInsetsController(window, window.decorView)
            .hide(WindowInsetsCompat.Type.ime())
    }
}

internal fun Context.showKeyboard() {
    if (this is Activity) {
        val window = this.window
        WindowCompat.getInsetsController(window, window.decorView)
            .show(WindowInsetsCompat.Type.ime())
    }
}

/**
 * @return the system theme is light theme
 *
 * more dark mode [doc](https://developer.android.com/guide/topics/ui/look-and-feel/darktheme?hl=zh-cn#kotlin)
 * */
private fun Context.isLightSystemTheme(): Boolean {
    val context = this
    val uiConfig = context.resources.configuration.uiMode
    return when (uiConfig and Configuration.UI_MODE_NIGHT_MASK) {
        // Night mode is not active, we're using the light theme
        Configuration.UI_MODE_NIGHT_NO -> {
            true
        }
        // Night mode is active, we're using dark theme
        Configuration.UI_MODE_NIGHT_YES -> {
            false
        }
        // Default light theme
        else -> {
            true
        }
    }
}
