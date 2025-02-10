package com.mohil_bansal.appshark.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohil_bansal.appshark.data.AppInfo
import com.mohil_bansal.appshark.data.getCategorizedApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppListScreen(onAppClick: (String) -> Unit) {
    // Holds the Triple(sideloadedApps, systemApps, allApps)
    var categorizedApps by remember { mutableStateOf<Triple<List<AppInfo>, List<AppInfo>, List<AppInfo>>?>(null) }
    val context = LocalContext.current

    // Launch data fetching off the main thread.
    LaunchedEffect(Unit) {
        categorizedApps = withContext(Dispatchers.IO) {
            getCategorizedApps(context)
        }
    }

    // While data is loading, show a loader.
    if (categorizedApps == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Destructure the results.
        val (sideloadedApps, systemApps, allApps) = categorizedApps!!
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("All", "System", "Side-loaded")

        Column(modifier = Modifier.fillMaxSize()) {
            // Tab row for switching between categories.
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index)
                                    FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                    )
                }
            }
            // Show the appropriate list depending on the selected tab.
            when (selectedTabIndex) {
                0 -> AppList(apps = allApps, onAppClick = onAppClick)
                1 -> AppList(apps = systemApps, onAppClick = onAppClick)
                2 -> AppList(apps = sideloadedApps, onAppClick = onAppClick)
            }
        }
    }
}

@Composable
fun AppList(apps: List<AppInfo>, onAppClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(apps) { app ->
            AppTile(appInfo = app, onClick = { onAppClick(app.packageName) })
            HorizontalDivider()
        }
    }
}

@Composable
fun AppTile(appInfo: AppInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display the app icon
        Image(
            bitmap = appInfo.icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Display the app name with ellipsis if it's too long
        Text(
            text = if (appInfo.appName.length > 10) "${appInfo.appName.take(10)}..." else appInfo.appName,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp
        )
        Text(text = appInfo.technology, fontSize = 14.sp)
    }
}