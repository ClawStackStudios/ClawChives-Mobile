package com.example.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.FilterStateDao
import com.example.data.repository.AuthRepository

class DashboardViewModelFactory(
    private val authRepository: AuthRepository,
    private val filterStateDao: FilterStateDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(authRepository, filterStateDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
