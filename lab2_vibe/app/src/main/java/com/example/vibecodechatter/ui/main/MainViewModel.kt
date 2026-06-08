package com.example.vibecodechatter.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vibecodechatter.data.model.ChattMessage
import com.example.vibecodechatter.data.repository.MessageRepository
import com.example.vibecodechatter.ui.ScopedViewModel
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MessageRepository
) : ScopedViewModel() {

    private val _messages = MutableLiveData<List<ChattMessage>>(emptyList())
    val messages: LiveData<List<ChattMessage>> = _messages

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        refreshMessages()
    }

    fun refreshMessages() {
        if (_isRefreshing.value == true) {
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _messages.value = repository.getMessages()
                _errorMessage.value = null
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "Unable to load messages."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    class Factory(
        private val repository: MessageRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

