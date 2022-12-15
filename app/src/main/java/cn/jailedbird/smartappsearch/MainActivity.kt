package cn.jailedbird.smartappsearch

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import cn.jailedbird.smartappsearch.adapter.AppListAdapter
import cn.jailedbird.smartappsearch.data.AppDao
import cn.jailedbird.smartappsearch.databinding.ActivityMainBinding
import cn.jailedbird.smartappsearch.dialog.AppSettingsPopWindow
import cn.jailedbird.smartappsearch.model.ConfigModel
import cn.jailedbird.smartappsearch.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val config = ConfigModel()

    @Inject
    lateinit var appDao: AppDao
    private val adapter by lazy { AppListAdapter(appDao) }
    // private var apps = emptyList<AppModel>()

    private val viewModel by viewModels<MainViewModel>()

    private val listener = object : AppSettingsPopWindow.Listener {
        override fun refreshApp() {
            lifecycleScope.launch {
                AppUtils.refresh(this@MainActivity, appDao)
            }
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
            SettingsActivity.start(this@MainActivity)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        initObserver()
    }

    /**
     * TODO implement dark mode, [google dark mode doc](https://developer.android.com/develop/ui/views/theming/darktheme)
     * */
    private fun initView() {
        binding.search.requestFocus()
        binding.recyclerView.adapter = adapter
        binding.search.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.search(text.toString().trim())
        })
    }

    private fun initEvent() {
        binding.ivMore.setDebouncedClick {
            AppSettingsPopWindow.open(this, it, listener)
        }
    }

    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.list.collectLatest {
                adapter.submitList(it)
            }
        }
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