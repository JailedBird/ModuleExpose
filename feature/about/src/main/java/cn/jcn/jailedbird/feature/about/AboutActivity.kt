package cn.jcn.jailedbird.feature.about

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import cn.jcn.jailedbird.feature.about.expose.AboutEntity

class AboutActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        fun start(context: Context, aboutEntity: AboutEntity) {
            val starter = Intent(context, AboutActivity::class.java)
                .putExtra("bean", aboutEntity)
            context.startActivity(starter)
        }
    }



}