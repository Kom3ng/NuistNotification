package moe.okay.nuistnotification.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.okay.nuistnotification.data.Notification
import moe.okay.nuistnotification.repository.NotificationRepository

sealed class UiState {
    data object Loading : UiState()
    data class Success(val notifications: List<Notification>) : UiState()
    data class Error(val message: String) : UiState()
}

class NotificationScreenViewModel : ViewModel() {
    private val repository = NotificationRepository()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            repository.fetchAndParseNotifications()
                .onSuccess { items ->
                    _uiState.value = UiState.Success(items)
                }
                .onFailure { error ->
                    _uiState.value = UiState. Error(
                        error.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun refresh() {
        loadData()
    }
}