package com.mohil_bansal.appshark.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppDetailsScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: AppDetailsViewModel = viewModel()
) {
    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    val appDetails by viewModel.appDetails.collectAsState()
    if (appDetails == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
//        AppDescriptionScreen(appDetails = appDetails!!, onBack = onBack)
    AppDescriptionScreen(
        packageName = packageName,
        onBack = onBack,
        viewModel = viewModel
    )
    }
}
