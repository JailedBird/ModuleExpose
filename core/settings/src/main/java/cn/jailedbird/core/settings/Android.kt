package cn.jailedbird.core.settings

import android.os.Build

object Android {
    val sdk: Int
        get() = Build.VERSION.SDK_INT

    val name: String
        get() = "Android ${Build.VERSION.RELEASE}"

    val platforms = Build.SUPPORTED_ABIS.toSet()

    val primaryPlatform: String?
        get() = Build.SUPPORTED_64_BIT_ABIS?.firstOrNull()
            ?: Build.SUPPORTED_32_BIT_ABIS?.firstOrNull()

    fun sdk(sdk: Int): Boolean {
        return Build.VERSION.SDK_INT >= sdk
    }

    object PackageManager {
        // GET_SIGNATURES should always present for getPackageArchiveInfo
        val signaturesFlag: Int
            get() = (if (sdk(28)) android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES else 0) or
                    @Suppress("DEPRECATION") android.content.pm.PackageManager.GET_SIGNATURES
    }
}