package cn.jailedbird.feature.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import cn.jailedbird.core.common.base.fragment.BaseVBFragment
import cn.jailedbird.core.common.utils.setDebouncingClick
import cn.jailedbird.core.settings.Settings
import cn.jailedbird.edgeutils.paddingTopSystemWindowInsets
import cn.jailedbird.feature.search.expose.SearchExpose
import cn.jailedbird.feature.settings.databinding.FragmentSettingMainBinding
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseVBFragment<FragmentSettingMainBinding>() {
    @Inject
    lateinit var searchExpose: SearchExpose

    override val inflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingMainBinding
        get() = FragmentSettingMainBinding::inflate

    override fun initView() {
        binding.appbar.paddingTopSystemWindowInsets()
        bindPreference(binding.swCenterMatch, Settings.Key.MatchCenter)
        bindPreference(binding.swAutoPopIme, Settings.Key.ImeAutoPop)
        bindPreference(
            binding.swDirectLaunch,
            Settings.Key.LaunchDirect
        )
    }

    override fun initEvent() {
        binding.btReturnSearch.setDebouncingClick {
            requireActivity().finish()
            searchExpose.openSearchActivity(this@SettingFragment.requireContext())
        }
    }

    private fun bindPreference(switch: SwitchMaterial, key: Settings.Key<Boolean>) {
        switch.isChecked = Settings[key]
        switch.setOnCheckedChangeListener { _, isChecked ->
            Settings[key] = isChecked
        }
    }

}