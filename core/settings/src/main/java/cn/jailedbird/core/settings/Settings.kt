@file:Suppress("unused")

package cn.jailedbird.core.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatDelegate
import cn.jailedbird.core.resource.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.Proxy

/**
 * Best way to implement App settings, code from [Neo Store](https://github.com/NeoApplications/Neo-Store)
 * */
object Settings : OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences

    private val mutableSubject = MutableSharedFlow<Key<*>>()
    val subject = mutableSubject.asSharedFlow()

    private val keys = sequenceOf(
        Key.Language,
        Key.AutoSync,
        Key.AutoSyncInterval,
        Key.ReleasesCacheRetention,
        Key.ImagesCacheRetention,
        Key.InstallAfterSync,
        Key.IncompatibleVersions,
        Key.ShowScreenshots,
        Key.UpdatedApps,
        Key.NewApps,
        Key.ProxyHost,
        Key.ProxyPort,
        Key.ProxyType,
        Key.RootSessionInstaller,
        Key.SortOrderAscendingExplore,
        Key.SortOrderAscendingLatest,
        Key.SortOrderAscendingInstalled,
        Key.ReposFilterExplore,
        Key.ReposFilterLatest,
        Key.ReposFilterInstalled,
        Key.CategoriesFilterExplore,
        Key.CategoriesFilterLatest,
        Key.CategoriesFilterInstalled,
        Key.Theme,
        Key.UpdateNotify,
        Key.UpdateUnstable,
        Key.IgnoreIgnoreBatteryOptimization
    ).map { Pair(it.name, it) }.toMap()

    fun init(context: Context) {
        preferences = context.getSharedPreferences(
            "${context.packageName}_preferences", Context.MODE_PRIVATE
        )
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            keys[key]?.let {
                mutableSubject.emit(it)
            }
        }
    }

    sealed class Value<T> {
        abstract val value: T

        internal abstract fun get(
            preferences: SharedPreferences,
            key: String,
            defaultValue: Value<T>,
        ): T

        internal abstract fun set(preferences: SharedPreferences, key: String, value: T)

        class BooleanValue(override val value: Boolean) : Value<Boolean>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<Boolean>,
            ): Boolean {
                return preferences.getBoolean(key, defaultValue.value)
            }

            override fun set(preferences: SharedPreferences, key: String, value: Boolean) {
                preferences.edit().putBoolean(key, value).apply()
            }
        }

        class IntValue(override val value: Int) : Value<Int>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<Int>,
            ): Int {
                return preferences.getInt(key, defaultValue.value)
            }

            override fun set(preferences: SharedPreferences, key: String, value: Int) {
                preferences.edit().putInt(key, value).apply()
            }
        }

        class StringSetValue(override val value: Set<String>) : Value<Set<String>>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<Set<String>>,
            ): Set<String> {
                return preferences.getStringSet(key, defaultValue.value) ?: emptySet()
            }

            override fun set(preferences: SharedPreferences, key: String, value: Set<String>) {
                preferences.edit().putStringSet(key, value).apply()
            }
        }

        class StringValue(override val value: String) : Value<String>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<String>,
            ): String {
                return preferences.getString(key, defaultValue.value) ?: defaultValue.value
            }

            override fun set(preferences: SharedPreferences, key: String, value: String) {
                preferences.edit().putString(key, value).apply()
            }
        }

        class EnumerationValue<T : Enumeration<T>>(override val value: T) : Value<T>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<T>,
            ): T {
                val value = preferences.getString(key, defaultValue.value.valueString)
                return defaultValue.value.values.find { it.valueString == value }
                    ?: defaultValue.value
            }

            override fun set(preferences: SharedPreferences, key: String, value: T) {
                preferences.edit().putString(key, value.valueString).apply()
            }
        }
    }

    interface Enumeration<T> {
        val values: List<T>
        val valueString: String
    }

    sealed class Key<T>(val name: String, val default: Value<T>) {
        object Null : Key<Int>("", Value.IntValue(0))

        object MatchCenter : Key<Boolean>("matcher_center", Value.BooleanValue(true))

        object Language : Key<String>("languages", Value.StringValue("system"))
        object AutoSync : Key<Settings.AutoSync>(
            "auto_sync", Value.EnumerationValue(Settings.AutoSync.Wifi)
        )

        object ReleasesCacheRetention : Key<Int>("releases_cache_retention", Value.IntValue(1))

        object ImagesCacheRetention : Key<Int>("images_cache_retention", Value.IntValue(14))

        object AutoSyncInterval : Key<Int>("auto_sync_interval", Value.IntValue(60))

        object InstallAfterSync :
            Key<Boolean>("auto_sync_install", Value.BooleanValue(Android.sdk(31)))

        object IncompatibleVersions :
            Key<Boolean>("incompatible_versions", Value.BooleanValue(false))

        object ShowScreenshots : Key<Boolean>("show_screenshots", Value.BooleanValue(true))

        object ImeAutoPop : Key<Boolean>("ime_auto_pop", Value.BooleanValue(true))
        object LaunchDirect :
            Key<Boolean>("launch_directly_when_only_one_choose", Value.BooleanValue(false))

        object UpdatedApps : Key<Int>("updated_apps", Value.IntValue(100))
        object NewApps : Key<Int>("new_apps", Value.IntValue(20))

        object ProxyHost : Key<String>("proxy_host", Value.StringValue("localhost"))
        object ProxyPort : Key<Int>("proxy_port", Value.IntValue(9050))
        object ProxyType : Key<Settings.ProxyType>(
            "proxy_type", Value.EnumerationValue(Settings.ProxyType.Direct)
        )

        object RootSessionInstaller :
            Key<Boolean>("root_session_installer", Value.BooleanValue(false))

        object SortOrderAscendingExplore :
            Key<Boolean>("sort_order_ascending_explore", Value.BooleanValue(false))

        object SortOrderAscendingLatest :
            Key<Boolean>("sort_order_ascending_latest", Value.BooleanValue(false))

        object SortOrderAscendingInstalled :
            Key<Boolean>("sort_order_ascending_installed", Value.BooleanValue(true))

        object ReposFilterExplore : Key<Set<String>>(
            "repos_filter_explore", Value.StringSetValue(emptySet())
        )

        object ReposFilterLatest : Key<Set<String>>(
            "repos_filter_latest", Value.StringSetValue(emptySet())
        )

        object ReposFilterInstalled : Key<Set<String>>(
            "repos_filter_installed", Value.StringSetValue(emptySet())
        )

        object CategoriesFilterExplore : Key<Set<String>>(
            "categories_filter_explore", Value.StringSetValue(emptySet())
        )

        object CategoriesFilterLatest : Key<Set<String>>(
            "categories_filter_latest", Value.StringSetValue(emptySet())
        )

        object CategoriesFilterInstalled : Key<Set<String>>(
            "categories_filter_installed", Value.StringSetValue(emptySet())
        )

        object Theme : Key<Settings.Theme>(
            "theme", Value.EnumerationValue(Settings.Theme.System)
        )

        object UpdateNotify : Key<Boolean>("update_notify", Value.BooleanValue(true))
        object UpdateUnstable : Key<Boolean>("update_unstable", Value.BooleanValue(false))

        object IgnoreIgnoreBatteryOptimization :
            Key<Boolean>("ignore_ignore_battery_optimization", Value.BooleanValue(false))
    }

    sealed class AutoSync(override val valueString: String) : Enumeration<AutoSync> {
        override val values: List<AutoSync>
            get() = listOf(Never, Wifi, WifiBattery, Always)

        object Never : AutoSync("never")
        object Wifi : AutoSync("wifi")
        object WifiBattery : AutoSync("wifi-battery")
        object Always : AutoSync("always")
    }

    sealed class ProxyType(override val valueString: String, val proxyType: Proxy.Type) :
        Enumeration<ProxyType> {
        override val values: List<ProxyType>
            get() = listOf(Direct, Http, Socks)

        object Direct : ProxyType("direct", Proxy.Type.DIRECT)
        object Http : ProxyType("http", Proxy.Type.HTTP)
        object Socks : ProxyType("socks", Proxy.Type.SOCKS)
    }

    sealed class Theme(override val valueString: String) : Enumeration<Theme> {
        override val values: List<Theme>
            get() = if (Android.sdk(31)) listOf(System, Light, Dark)
            else if (Android.sdk(29)) listOf(System, Light, Dark)
            else listOf(Light, Dark)

        abstract val resId: Int
        abstract val nightMode: Int

        object System : Theme("system") {
            override val resId: Int
                get() = R.style.AppTheme
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        object Light : Theme("light") {
            override val resId: Int
                get() = R.style.AppTheme
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_NO
        }

        object Dark : Theme("dark") {
            override val resId: Int
                get() = R.style.AppTheme
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

    }

    operator fun <T> get(key: Key<T>): T {
        return key.default.get(preferences, key.name, key.default)
    }

    operator fun <T> set(key: Key<T>, value: T) {
        key.default.set(preferences, key.name, value)
    }
}
