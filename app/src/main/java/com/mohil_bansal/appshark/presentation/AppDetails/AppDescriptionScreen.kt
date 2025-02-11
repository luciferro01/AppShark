package com.mohil_bansal.appshark.presentation.AppDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohil_bansal.appshark.domain.models.AppDetails
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppDescriptionScreen(
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
        AppDescriptionContent(appDetails = appDetails!!, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDescriptionContent(appDetails: AppDetails, onBack: () -> Unit) {
    // Define tab titles – note many tabs now so we use ScrollableTabRow.
    val tabs = listOf(
        "Activities",
        "Meta Data",
        "Content Providers",
        "Permissions",
        "Broadcast Receivers",
        "Services",
        "Native Libraries"
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = appDetails.appName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            // Overview section with extended fields.
            AppOverviewSection(
                icon = appDetails.icon,
                appName = appDetails.appName,
                architecture = appDetails.architecture,
                language = appDetails.language,
                minSdk = appDetails.minSdkVersion,
                targetSdk = appDetails.targetSdkVersion,
                compiledVersion = appDetails.compiledVersion
            )
            // Scrollable tab row
            ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }
            // Detail area for each tab.
            when (selectedTabIndex) {
                0 -> InfoList(title = "Activities", items = appDetails.activities)
                1 -> MetaDataTab(metaData = appDetails.metaData)
                2 -> InfoList(title = "Content Providers", items = appDetails.contentProviders)
                3 -> PermissionsTab(
                    allowed = appDetails.permissionsAllowed,
                    denied = appDetails.permissionsDenied
                )
                4 -> InfoList(title = "Broadcast Receivers", items = appDetails.broadcastReceivers)
                5 -> InfoList(title = "Services", items = appDetails.services)
                6 -> InfoList(title = "Native Libraries", items = appDetails.nativeLibraries)
            }
        }
    }
}

@Composable
fun AppOverviewSection(
    icon: androidx.compose.ui.graphics.ImageBitmap,
    appName: String,
    architecture: String,
    language: String,
    minSdk: Double,
    targetSdk: Int,
    compiledVersion: String
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(bitmap = icon, contentDescription = "App Icon", modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = appName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Architecture: $architecture", fontSize = 14.sp)
                Text(text = "Language: $language", fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Additional version info.
        Text(text = "Min SDK: $minSdk", fontSize = 14.sp)
        Text(text = "Target SDK: $targetSdk", fontSize = 14.sp)
        Text(text = "Compiled Version: $compiledVersion", fontSize = 14.sp)
    }
    Divider()
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
