package cn.jailedbird.smartappsearch.model

data class AppConfig(
    /** Auto pop ime when App start*/
    val popImeWhenStart: Boolean = true,
    /** Right now launch app when only one search result*/
    val launchRightNow: Boolean = false,
) {
    companion object {
        const val LAUNCH_DELAY_TIME = 800L
    }
}
