package com.mohil_bansal.appshark.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohil_bansal.appshark.domain.models.AppDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDescriptionScreen(
    appDetails: AppDetails,
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Activities", "Meta Data", "Content Providers", "Permissions")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = appDetails.appName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Overview section
            AppOverviewSection(
                icon = appDetails.icon,
                appName = appDetails.appName,
                architecture = appDetails.architecture,
                language = appDetails.language
            )
            // Tabs for details
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> InfoList(title = "Activities", items = appDetails.activities)
                1 -> MetaDataTab(metaData = appDetails.metaData)
                2 -> InfoList(title = "Content Providers", items = appDetails.contentProviders)
                3 -> PermissionsTab(
                    allowed = appDetails.permissionsAllowed,
                    denied = appDetails.permissionsDenied
                )
            }
        }
    }
}

@Composable
fun AppOverviewSection(
    icon: androidx.compose.ui.graphics.ImageBitmap,
    appName: String,
    architecture: String,
    language: String
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Image(bitmap = icon, contentDescription = "App Icon", modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = appName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Architecture: $architecture", fontSize = 14.sp)
            Text(text = "Language: $language", fontSize = 14.sp)
        }
    }
    HorizontalDivider()
}

@Composable
fun InfoList(title: String, items: List<String>) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        item { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        items(items) { item ->
            Text(text = item, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun MetaDataTab(metaData: Map<String, String>) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        item { Text(text = "Meta Data", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        metaData.forEach { (key, value) ->
            item {
                Text(text = "$key: $value", fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun PermissionsTab(allowed: List<String>, denied: List<String>) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        item { Text(text = "Permissions Allowed", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        items(allowed) { permission ->
            Text(text = permission, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { Text(text = "Permissions Denied", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        items(denied) { permission ->
            Text(text = permission, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}
