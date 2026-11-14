package com.universalpodcast.app.wearos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.universalpodcast.app.model.feed.Feed
import com.universalpodcast.app.net.sync.wearinterface.WearConnectionUtils
import com.universalpodcast.app.net.sync.wearinterface.WearDataPaths
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

data class FeedListUiState(
    val feeds: List<Feed>? = null,
    val isPhoneSupported: Boolean = true,
    val isTimedOut: Boolean = false
)

class FeedListViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FeedListUiState())
    val uiState: StateFlow<FeedListUiState> = _uiState

    init {
        viewModelScope.launch {
            WearDataRepository.feedsByPath.collect { map ->
                _uiState.update { it.copy(feeds = map[WearDataPaths.SUBSCRIPTIONS]) }
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
            WearMessageSender.send(getApplication(), WearDataPaths.SUBSCRIPTIONS)
            val received = withTimeoutOrNull(10.seconds) {
                WearDataRepository.feedsByPath.first { it.containsKey(WearDataPaths.SUBSCRIPTIONS) }
            }
            if (received == null) {
                _uiState.update { it.copy(isTimedOut = true) }
            }
        }
    }
}
