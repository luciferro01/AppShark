package com.mohil_bansal.appshark.presentation.AppDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohil_bansal.appshark.data.repository.AppRepositoryImpl
import com.mohil_bansal.appshark.domain.models.AppDetails
import com.mohil_bansal.appshark.domain.usecases.GetAppDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepositoryImpl(application)
    private val getAppDetailsUseCase = GetAppDetailsUseCase(repository)

    private val _appDetails = MutableStateFlow<AppDetails?>(null)
    val appDetails: StateFlow<AppDetails?> = _appDetails

    fun loadAppDetails(packageName: String) {
        viewModelScope.launch {
            _appDetails.value = getAppDetailsUseCase(packageName)
        }
    }
}
