package com.universalpodcast.app.wearos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.universalpodcast.app.model.feed.FeedItem
import com.universalpodcast.app.net.sync.wearinterface.WearConnectionUtils
import com.universalpodcast.app.wearos.sync.WearDataRepository
import com.universalpodcast.app.wearos.sync.WearMessageSender
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class EpisodeListUiState(
    val episodes: List<FeedItem>? = null,
    val isPhoneSupported: Boolean = true,
    val isTimedOut: Boolean = false,
    val titleRes: Int = 0
)

class EpisodeListViewModel(application: Application, private val path: String) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(EpisodeListUiState(titleRes = NavigationNames.getTitleResForPath(path)))
    val uiState: StateFlow<EpisodeListUiState> = _uiState

    init {
        viewModelScope.launch {
            WearDataRepository.episodesByPath.collect { map ->
                _uiState.update { it.copy(episodes = map[path]) }
            }
        }

        viewModelScope.launch {
            val isSupported = withContext(Dispatchers.IO) {
                WearConnectionUtils.isPhoneSupported(getApplication())
            }
            if (!isSupported) {
                _uiState.update { it.copy(isPhoneSupported = false) }
                return@launch
            }
            WearMessageSender.send(getApplication(), path)
            val received = withTimeoutOrNull(10.seconds) {
                WearDataRepository.episodesByPath.first { it.containsKey(path) }
            }
            if (received == null) {
                _uiState.update { it.copy(isTimedOut = true) }
            }
        }
    }

    companion object {
        fun factory(path: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EpisodeListViewModel(checkNotNull(get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY)), path)
            }
        }
    }
}
