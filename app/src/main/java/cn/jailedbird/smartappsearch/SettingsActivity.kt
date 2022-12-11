package cn.jailedbird.smartappsearch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.jailedbird.smartappsearch.databinding.ActivitySettingsBinding
import cn.jailedbird.smartappsearch.dialog.AppSettingsPopWindow
import cn.jailedbird.smartappsearch.utils.setDebouncedClick
import cn.jailedbird.smartappsearch.utils.toPx
import kotlinx.coroutines.launch

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