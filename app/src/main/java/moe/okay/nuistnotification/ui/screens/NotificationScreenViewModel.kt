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
    var currentPage: Int = 1
    var errorMessage: String? = null
    object Loading : UiState()
    data class Success(val notifications: List<News>) : UiState()
}

class NotificationScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotificationRepository(application)

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        initializeFirstPage()
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return
        viewModelScope.launch {
            val nextPage = currentState.currentPage + 1
            val res = repository.loadMoreNotifications(nextPage)
            if (res.isSuccess) {
                val newNotifications = res.getOrNull() ?: emptyList()
                if (newNotifications.isNotEmpty()) {
                    val updatedList = currentState.notifications + newNotifications
                    val newState = UiState.Success(updatedList)
                    newState.currentPage = nextPage
                    _uiState.value = newState
                }
            } else {
                currentState.errorMessage = res.exceptionOrNull()?.localizedMessage
                _uiState.value = currentState
            }
        }
    }

    fun initializeFirstPage() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val res = repository.refreshNotifications()
            if (res.isSuccess) {
                val success = UiState.Success(res.getOrNull() ?: emptyList())
                success.currentPage = 1
                _uiState.value = success
                if (success.notifications.isNotEmpty())
                    repository.setLastSeenId(success.notifications.maxBy { n->n.id }.id)
            } else {
                val errorState = UiState.Success(emptyList())
                errorState.errorMessage = res.exceptionOrNull()?.localizedMessage
                errorState.currentPage = 1
                _uiState.value = errorState
            }
        }
    }
}
