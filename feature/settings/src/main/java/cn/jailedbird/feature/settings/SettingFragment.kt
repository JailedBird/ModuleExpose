package cn.jailedbird.feature.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import cn.jailedbird.core.common.base.fragment.BaseVBFragment
import cn.jailedbird.edgeutils.paddingTopSystemWindowInsets
import cn.jailedbird.feature.settings.databinding.FragmentSettingMainBinding
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingFragment : BaseVBFragment<FragmentSettingMainBinding>() {
    override val inflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingMainBinding
        get() = FragmentSettingMainBinding::inflate

    override fun initView() {
        binding.appbar.paddingTopSystemWindowInsets()
        bindPreference(binding.swCenterMatch, cn.jailedbird.core.settings.Settings.Key.MatchCenter)
        bindPreference(binding.swAutoPopIme, cn.jailedbird.core.settings.Settings.Key.ImeAutoPop)
        bindPreference(binding.swDirectLaunch, cn.jailedbird.core.settings.Settings.Key.LaunchDirect)
    }

    override fun initEvent() {
    }

    private fun bindPreference(switch: SwitchMaterial, key: cn.jailedbird.core.settings.Settings.Key<Boolean>) {
        switch.isChecked = cn.jailedbird.core.settings.Settings[key]
        switch.setOnCheckedChangeListener { _, isChecked ->
            cn.jailedbird.core.settings.Settings[key] = isChecked
        }
    }

}