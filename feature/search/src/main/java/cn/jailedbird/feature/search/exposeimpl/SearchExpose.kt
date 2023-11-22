package cn.jailedbird.feature.search.exposeimpl

import android.content.Context
import cn.jailedbird.feature.search.expose.SearchExpose
import cn.jailedbird.feature.search.main.SearchActivity
import javax.inject.Inject

class SearchExposeImpl @Inject constructor() : SearchExpose {
    override fun openSearchActivity(context: Context) {
        SearchActivity.start(context)
    }

}