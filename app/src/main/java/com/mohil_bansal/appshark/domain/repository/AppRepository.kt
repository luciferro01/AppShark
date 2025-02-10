package com.mohil_bansal.appshark.domain.repository

import com.mohil_bansal.appshark.domain.models.AppDetails

interface AppRepository {
    suspend fun getAppDetails(packageName: String): AppDetails
}
