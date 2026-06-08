package com.example.vibecodechatter.ui.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vibecodechatter.data.local.UserPreferences
import com.example.vibecodechatter.data.repository.MessageRepository
import com.example.vibecodechatter.ui.ScopedViewModel
import kotlinx.coroutines.launch

sealed interface PostEvent {
    data object Success : PostEvent
    data class Error(val message: String) : PostEvent
}

class PostViewModel(
    private val repository: MessageRepository,
    private val userPreferences: UserPreferences
) : ScopedViewModel() {

    private val _lastUsername = MutableLiveData(userPreferences.getLastUsername())
    val lastUsername: LiveData<String> = _lastUsername

    private val _isSending = MutableLiveData(false)
    val isSending: LiveData<Boolean> = _isSending

    private val _postEvent = MutableLiveData<PostEvent?>()
    val postEvent: LiveData<PostEvent?> = _postEvent

    fun send(username: String, message: String) {
        val cleanUsername = username.trim()
        val cleanMessage = message.trim()

        if (cleanUsername.isBlank() || cleanMessage.isBlank()) {
            _postEvent.value = PostEvent.Error("Username and message are required.")
            return
        }

        if (_isSending.value == true) {
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            try {
                repository.postMessage(cleanUsername, cleanMessage)
                userPreferences.saveLastUsername(cleanUsername)
                _lastUsername.value = cleanUsername
                _postEvent.value = PostEvent.Success
            } catch (error: Exception) {
                _postEvent.value = PostEvent.Error(
                    error.message ?: "Unable to send message."
                )
            } finally {
                _isSending.value = false
            }
        }
    }

    fun onEventHandled() {
        _postEvent.value = null
    }

    class Factory(
        private val repository: MessageRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
                return PostViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

