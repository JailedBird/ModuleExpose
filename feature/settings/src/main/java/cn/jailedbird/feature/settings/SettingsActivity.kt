package cn.jailedbird.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import cn.jailedbird.edgeutils.EdgeUtils.edgeSetSystemBarLight
import cn.jailedbird.edgeutils.EdgeUtils.edgeToEdge
import cn.jailedbird.feature.search.expose.SearchExpose
import cn.jailedbird.feature.settings.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        internal fun start(context: Context) {
            val starter = Intent(context, SettingsActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        edgeToEdge(false)
        edgeSetSystemBarLight(true)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private lateinit var navController: NavController

    private fun initView() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        navHostFragment.navController.apply {
            graph = settingGraph()
        }
    }

    @Suppress("ClassName")
    private object nav_routes {
        const val setting_main_fragment = "setting_main_fragment"
    }

    private fun NavController.settingGraph(): NavGraph = createGraph(
        startDestination = nav_routes.setting_main_fragment
    ) {
        fragment<SettingFragment>(nav_routes.setting_main_fragment) {
            label = "Settings"
        }
    }

}