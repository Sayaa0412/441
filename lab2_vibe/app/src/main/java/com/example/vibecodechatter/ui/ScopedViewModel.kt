package com.example.vibecodechatter.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class ScopedViewModel : ViewModel() {
    protected val viewModelScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}

