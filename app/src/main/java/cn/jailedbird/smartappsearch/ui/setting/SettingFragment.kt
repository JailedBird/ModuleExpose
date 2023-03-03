package cn.jailedbird.smartappsearch.ui.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import cn.jailedbird.edgeutils.paddingTopSystemWindowInsets
import cn.jailedbird.smartappsearch.base.BaseVBFragment
import cn.jailedbird.smartappsearch.databinding.FragmentSettingMainBinding
import cn.jailedbird.smartappsearch.settings.Settings
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingFragment : BaseVBFragment<FragmentSettingMainBinding>() {
    override val inflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingMainBinding
        get() = FragmentSettingMainBinding::inflate

    override fun initView() {
        binding.appbar.paddingTopSystemWindowInsets()
        bindPreference(binding.swCenterMatch, Settings.Key.MatchCenter)
        bindPreference(binding.swAutoPopIme, Settings.Key.ImeAutoPop)
        bindPreference(binding.swDirectLaunch, Settings.Key.LaunchDirect)
    }

    override fun initEvent() {
    }

    private fun bindPreference(switch: SwitchMaterial, key: Settings.Key<Boolean>) {
        switch.isChecked = Settings[key]
        switch.setOnCheckedChangeListener { _, isChecked ->
            Settings[key] = isChecked
        }
    }

}