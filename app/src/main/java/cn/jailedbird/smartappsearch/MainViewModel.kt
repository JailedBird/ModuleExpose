package cn.jailedbird.smartappsearch

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jailedbird.smartappsearch.data.AppDao
import cn.jailedbird.smartappsearch.data.AppRepository
import cn.jailedbird.smartappsearch.data.entity.AppModel
import cn.jailedbird.smartappsearch.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    @Inject
    lateinit var appDao: AppDao

    @SuppressLint("StaticFieldLeak")
    @ApplicationContext
    lateinit var context: Context

    private var _apps = emptyList<AppModel>()

    private val appsFlow = repository.getAppsFlow().distinctUntilChanged()

    init {
        viewModelScope.launch {
            appsFlow.collectLatest {
                // Careful dead loop by observer
                if (it.isEmpty()) {
                    AppUtils.refresh(context, appDao)
                }
                _apps = it
            }
        }
    }
}