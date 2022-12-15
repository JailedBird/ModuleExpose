package cn.jailedbird.smartappsearch

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jailedbird.smartappsearch.data.AppDao
import cn.jailedbird.smartappsearch.data.AppRepository
import cn.jailedbird.smartappsearch.data.entity.AppModel
import cn.jailedbird.smartappsearch.utils.AppUtils
import cn.jailedbird.smartappsearch.utils.EMPTY
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: AppRepository,
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val APP_SEARCH = "APP_SEARCH_KEYWORD"
    }

    @Inject
    lateinit var appDao: AppDao

    // Latest memory cache for all apk
    private var apps = emptyList<AppModel>()
    private var _list = MutableStateFlow(emptyList<AppModel>())
    val list = _list.asStateFlow()
    private var keyword = EMPTY


    private var job: Job? = null
    private val appsFlow = repository.getAppsFlow().distinctUntilChanged()

    init {
        keyword = savedStateHandle.get<String>(APP_SEARCH) ?: EMPTY
        viewModelScope.launch {
            appsFlow.collectLatest {
                // Careful dead loop by observer
                if (it.isEmpty()) {
                    AppUtils.refresh(context, appDao)
                }
                apps = it
                updateResult(it, keyword)
            }
        }
    }

    private fun updateResult(apps: List<AppModel>, key: String) {
        job?.cancel(CancellationException("New job reach, cancel last job"))
        job = viewModelScope.launch((Dispatchers.IO)) {
            val res = mutableListOf<AppModel>()
            apps.forEach {
                if (it.match(key)) {
                    res.add(it)
                }
            }
            _list.emit(res)
        }
    }

    fun search(key: String) {
        keyword = key
        savedStateHandle[APP_SEARCH] = key
        updateResult(apps, keyword)
    }

}