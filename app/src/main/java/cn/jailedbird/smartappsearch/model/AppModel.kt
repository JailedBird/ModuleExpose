package cn.jailedbird.smartappsearch.model

data class AppModel(
    /*Full package*/
    val packageName: String,
    /*App name*/
    val appName: String,
    /*App ping yin (if chinese)*/
    val appNamePy: String = "",
)