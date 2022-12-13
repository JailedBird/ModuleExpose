package cn.jailedbird.smartappsearch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.jailedbird.edgeutils.EdgeUtils.edgeSetSystemBarLight
import cn.jailedbird.edgeutils.EdgeUtils.edgeToEdge
import cn.jailedbird.smartappsearch.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, SettingsActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge(false)
        edgeSetSystemBarLight(true)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun initView() {
    }

    private fun initEvent() {

    }


}