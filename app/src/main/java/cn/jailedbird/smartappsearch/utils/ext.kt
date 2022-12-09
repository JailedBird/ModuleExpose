package cn.jailedbird.smartappsearch.utils

import android.util.Log
import android.widget.Toast
import cn.jailedbird.smartappsearch.App

internal fun toast(s: String?) {
    if (!s.isNullOrEmpty()) {
        Toast.makeText(App.applicationContext, s, Toast.LENGTH_SHORT).show()
    }
}

internal fun log(s: String?) {
    if (!s.isNullOrEmpty()) {
        Log.d(App.appName, s)
    }
}