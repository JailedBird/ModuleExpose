package cn.jailedbird.smartappsearch

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import cn.jailedbird.smartappsearch.databinding.ActivityMainBinding
import cn.jailedbird.smartappsearch.model.ConfigModel
import cn.jailedbird.smartappsearch.utils.toast
import com.github.promeg.pinyinhelper.Pinyin


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val config = ConfigModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.decorView.post {
            prettifyDialogStyle()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toast("中国: ${Pinyin.toPinyin("中国", "")}")
    }

    private fun prettifyDialogStyle() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        window.setLayout((width * 0.9f).toInt(), (height * 0.6f).toInt())
        this.setFinishOnTouchOutside(true)
        if (config.popImeWhenStart) {
            binding.search.requestFocus()

        }
    }

    private fun getAllPackage() {


    }

}