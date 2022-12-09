package cn.jailedbird.smartappsearch

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import cn.jailedbird.smartappsearch.databinding.ActivityMainBinding
import cn.jailedbird.smartappsearch.model.AppModel
import cn.jailedbird.smartappsearch.model.ConfigModel
import cn.jailedbird.smartappsearch.utils.log
import cn.jailedbird.smartappsearch.utils.toPinyin
import cn.jailedbird.smartappsearch.utils.toast
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val config = ConfigModel()
    private val adapter = AppListAdapter()
    private var apps = emptyList<AppModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            apps = getAllPackage()
            lifecycleScope.launchWhenStarted {
                adapter.submitList(apps)
            }
        }
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.decorView.post {
            prettifyDialogStyle()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.recyclerView.adapter = adapter
        binding.search.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val result = filter(apps, text.toString())
            adapter.submitList(result)
            if (result.size == 1 && config.launchRightNow) {
                result[0].launch(this@MainActivity)
            }
        })

    }


    private fun filter(origin: List<AppModel>, key: String): List<AppModel> {
        val result = mutableListOf<AppModel>()
        for (i in origin.indices) {
            origin[i].let {
                if (it.match(key)) {
                    result.add(it)
                }
            }
        }
        return result

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

    private suspend fun getAllPackage(): List<AppModel> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<AppModel>()
        var index = 0
        packageManager.getInstalledApplications(0).forEach {
            val packageName = it.packageName
            if (packageName.startsWith("com.google") || packageName.startsWith("com.android")) {
                "Skip $packageName".log()
            } else {
                val appName = packageManager.getApplicationLabel(it).toString()
                apps.add(
                    AppModel(
                        id = index++,
                        packageName = it.packageName,
                        appName = appName.lowercase(Locale.ENGLISH),
                        appNamePinyin = appName.toPinyin()?.lowercase(Locale.ENGLISH)
                    )
                )
            }
        }
        return@withContext apps
    }


}