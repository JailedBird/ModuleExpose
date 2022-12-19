package cn.jailedbird.smartappsearch.ui.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import cn.jailedbird.edgeutils.paddingTopSystemWindowInsets
import cn.jailedbird.smartappsearch.base.BaseVBFragment
import cn.jailedbird.smartappsearch.config.Preferences
import cn.jailedbird.smartappsearch.databinding.FragmentSettingMainBinding
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingFragment : BaseVBFragment<FragmentSettingMainBinding>() {
    override val inflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingMainBinding
        get() = FragmentSettingMainBinding::inflate

    override fun initView() {
        binding.appbar.paddingTopSystemWindowInsets()
        bindPreference(binding.swCenterMatch, Preferences.Key.MatchCenter)
        bindPreference(binding.swAutoPopIme, Preferences.Key.ImeAutoPop)
        bindPreference(binding.swDirectLaunch, Preferences.Key.LaunchDirect)
    }

    override fun initEvent() {
    }

    private fun bindPreference(switch: SwitchMaterial, key: Preferences.Key<Boolean>) {
        switch.isChecked = Preferences[key]
        switch.setOnCheckedChangeListener { _, isChecked ->
            Preferences[key] = isChecked
        }
    }

}