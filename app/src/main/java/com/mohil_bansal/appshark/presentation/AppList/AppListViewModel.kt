package com.mohil_bansal.appshark.presentation.AppList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohil_bansal.appshark.data.getCategorizedApps
import com.mohil_bansal.appshark.data.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val _categorizedApps = MutableStateFlow<Triple<List<AppInfo>, List<AppInfo>, List<AppInfo>>?>(null)
    val categorizedApps: StateFlow<Triple<List<AppInfo>, List<AppInfo>, List<AppInfo>>?> = _categorizedApps

    init {
        // Load data only once.
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                getCategorizedApps(application)
            }
            _categorizedApps.value = apps
        }
    }
}
