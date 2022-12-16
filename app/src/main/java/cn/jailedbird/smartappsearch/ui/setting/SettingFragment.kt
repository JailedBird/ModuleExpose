package cn.jailedbird.smartappsearch.ui.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import cn.jailedbird.edgeutils.paddingTopSystemWindowInsets
import cn.jailedbird.smartappsearch.base.BaseVBFragment
import cn.jailedbird.smartappsearch.databinding.FragmentSettingMainBinding

class SettingFragment : BaseVBFragment<FragmentSettingMainBinding>() {
    override val inflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingMainBinding
        get() = FragmentSettingMainBinding::inflate

    override fun initView() {
        binding.appbar.paddingTopSystemWindowInsets()

    }


}