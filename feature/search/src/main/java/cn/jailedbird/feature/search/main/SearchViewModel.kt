package cn.jailedbird.feature.search.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jailedbird.feature.search.data.AppRepository
import cn.jailedbird.feature.search.data.entity.AppModel
import cn.jailedbird.core.settings.Settings
import cn.jailedbird.feature.search.utils.EMPTY
import cn.jailedbird.feature.search.utils.packageManagerAppList
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class SearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val APP_SEARCH = "APP_SEARCH_KEYWORD"
    }

    private var keyword = EMPTY

    // Latest memory cache for all apk
    private var apps = emptyList<AppModel>()

    // StateFlow
    private var _list = MutableStateFlow(emptyList<AppModel>())
    val list = _list.asStateFlow()

    // Search job
    private var searchJob: Job? = null

    // Observable Flow
    private val appsFlow = repository.getAppsFlow().distinctUntilChanged()

    init {
        keyword = savedStateHandle.get<String>(APP_SEARCH) ?: EMPTY
        // Fast path: get result from room
        viewModelScope.launch {
            appsFlow.collectLatest {
                // Careful dead loop by observer
                if (it.isEmpty()) {
                    return@collectLatest
                } else {
                    apps = it
                    updateResult(it, keyword)
                }
            }
        }
        // Ensure latest result apk
        refreshAppDatabase()
    }

    fun clearRoomHistory() {
        viewModelScope.launch {
            repository.refreshAppModelTable(context.packageManagerAppList())
        }
    }

    fun refreshAppDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshAppModelTable(
                AppModel.updateMeta(
                    context,
                    apps.ifEmpty { repository.getApps() },
                    withReduce = true
                )
            )
        }
    }

    private fun updateResult(apps: List<AppModel>, key: String) {
        searchJob?.cancel(CancellationException("New job reach, cancel last job"))
        searchJob = viewModelScope.launch((Dispatchers.IO)) {
            val matchCenter = Settings[Settings.Key.MatchCenter]
            val res = mutableListOf<AppModel>()
            apps.forEach {
                ensureActive()
                if (it.match(key, matchCenter)) {
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