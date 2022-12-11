package cn.jailedbird.smartappsearch.model

data class ConfigModel(
    /** Auto pop ime when App start*/
    val popImeWhenStart: Boolean = true,
    /** Right now launch app when only one search result*/
    val launchRightNow: Boolean = true,
)
