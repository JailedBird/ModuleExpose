package cn.jailedbird.smartappsearch

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import cn.jailedbird.smartappsearch.adapter.AppListAdapter
import cn.jailedbird.smartappsearch.databinding.ActivityMainBinding
import cn.jailedbird.smartappsearch.dialog.AppSettingsPopWindow
import cn.jailedbird.smartappsearch.model.AppModel
import cn.jailedbird.smartappsearch.model.ConfigModel
import cn.jailedbird.smartappsearch.utils.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val config = ConfigModel()
    private val adapter = AppListAdapter()
    private var apps = emptyList<AppModel>()

    private val listener = object : AppSettingsPopWindow.Listener {
        override fun refreshApp() {
            "refreshApp".toast()
        }

        override fun rate() {
            "rate".toast()
        }

        override fun share() {
            "share".toast()
        }

        override fun clearHistory() {
            "clearHistory".toast()
        }

        override fun settings() {
            "settings".toast()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            // Fast path: get Apps info from Room
            apps = AppInfo.getAppsFromRoom()
            // Slow path: get Apps info from PackageManager
            if (apps.isEmpty()) {
                apps = AppInfo.refresh(this@MainActivity)
            }
            // submit info when list has init
            lifecycleScope.launchWhenStarted {
                adapter.submitList(apps)
            }
        }
        super.onCreate(savedInstanceState)
        // Please use NoActionBar theme
        window.requestFeature(Window.FEATURE_NO_TITLE)
        // prettify Window as Dialog style, Do this when Window is attached
        window.decorView.post {
            initWindowStyle(this)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun initView() {
        binding.recyclerView.adapter = adapter
        binding.search.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val result = searchFilter(apps, text.toString())
            adapter.submitList(result)
            if (result.size == 1 && config.launchRightNow) {
                result[0].launch(this@MainActivity)
            }
        })
    }

    private fun initEvent() {
        binding.ivMore.setDebouncedClick {
            AppSettingsPopWindow(this@MainActivity, listener)
                .showAsDropDown(it, -50.toPx().toInt(), 0)
        }
    }

    private fun searchFilter(origin: List<AppModel>, key: String): List<AppModel> {
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

    private fun initWindowStyle(activity: Activity) {
        val window = activity.window
        val (width, height) = activity.resources.displayMetrics.let {
            Pair(it.widthPixels, it.heightPixels)
        }
        val widthPercent = 0.8f
        val heightPercent = 0.55f
        window.setLayout((width * widthPercent).toInt(), (height * heightPercent).toInt())
        activity.setFinishOnTouchOutside(true)
        if (config.popImeWhenStart) {
            activity.showKeyboard()
        }
        window.decorView.background = GradientDrawable().apply {
            cornerRadius = 8.toPx()
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(Color.WHITE)
        }
    }
}