package com.mohil_bansal.appshark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mohil_bansal.appshark.data.AppInfo
import com.mohil_bansal.appshark.data.getCategorizedApps
import com.mohil_bansal.appshark.ui.theme.AppSharkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppSharkTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    // Hold our categorized apps (Triple<sideloaded, system, all>)
    var categorizedApps by remember {
        mutableStateOf<Triple<List<AppInfo>, List<AppInfo>, List<AppInfo>>?>(
            null
        )
    }
    val context = LocalContext.current

    // Launch a coroutine off the main thread to fetch installed apps
    LaunchedEffect(Unit) {
        categorizedApps = withContext(Dispatchers.IO) {
            getCategorizedApps(context)
        }
    }

    if (categorizedApps == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,

            ) {
            CircularProgressIndicator()
        }
    } else {
        val (sideloadedApps, systemApps, allApps) = categorizedApps!!
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("All", "System", "Side-loaded")
        Scaffold { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
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
                when (selectedTabIndex) {
                    0 -> AppList(apps = allApps)
                    1 -> AppList(apps = systemApps)
                    2 -> AppList(apps = sideloadedApps)
                }
            }
        }
    }
}