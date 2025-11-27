package moe.okay.nuistnotification.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.okay.nuistnotification.data.AppDatabase
import moe.okay.nuistnotification.data.News
import moe.okay.nuistnotification.repository.NotificationRepository

sealed class UiState {
    object Loading : UiState()
    data class Success(val notifications: List<News>) : UiState()
    data class Error(val message: String) : UiState()
}

class NotificationScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotificationRepository(AppDatabase.getInstance(application).notificationDao())

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeLocal()
        refresh()
    }

    private fun observeLocal() {
        viewModelScope.launch {
            repository.observeAll().collect { list ->
                _uiState.value = UiState.Success(list)
            }
        }
    }


    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val res = repository.refreshNotifications()
            if (res.isSuccess) {
                _uiState.value = UiState.Success(res.getOrNull() ?: emptyList())
            } else {
                _uiState.value = UiState.Error(res.exceptionOrNull()?.message ?: "Refresh failed")
            }
        }
    }
}
