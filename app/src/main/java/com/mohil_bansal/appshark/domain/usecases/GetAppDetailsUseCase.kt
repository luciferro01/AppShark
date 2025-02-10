package com.mohil_bansal.appshark.domain.usecases

import com.mohil_bansal.appshark.domain.models.AppDetails
import com.mohil_bansal.appshark.domain.repository.AppRepository

class GetAppDetailsUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(packageName: String): AppDetails {
        return repository.getAppDetails(packageName)
    }
}
