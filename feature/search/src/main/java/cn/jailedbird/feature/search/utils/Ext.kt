package cn.jailedbird.feature.search.utils

import com.github.promeg.pinyinhelper.Pinyin

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
