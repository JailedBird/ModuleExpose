package cn.jailedbird.feature.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import cn.jailedbird.core.common.utils.toast
import cn.jailedbird.feature.about.expose.AboutEntity

/**
 * Open content with browser, to do replace it with WebView
 * */
class AboutActivity /*: AppCompatActivity()*/ {
    companion object {
        @JvmStatic
        fun start(context: Context, aboutEntity: AboutEntity) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(aboutEntity.url))
            aboutEntity.title.toast()
            context.startActivity(intent)
        }
    }

}