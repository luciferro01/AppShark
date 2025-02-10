import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohil_bansal.appshark.AppList
import com.mohil_bansal.appshark.presentation.AppListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohil_bansal.appshark.data.AppInfo

@Composable
fun AppList(apps: List<AppInfo>, onAppClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()){
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
            text = if (appInfo.appName.length > 15) "${appInfo.appName.take(15)}..." else appInfo.appName,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp
        )
        Text(text = appInfo.technology, fontSize = 14.sp)
    }
}



@Composable
fun AppListScreen(onAppClick: (String) -> Unit, viewModel: AppListViewModel = viewModel()) {
    val categorizedAppsState by viewModel.categorizedApps.collectAsState()

    if (categorizedAppsState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val (sideloadedApps, systemApps, allApps) = categorizedAppsState!!
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("All", "System", "Side-loaded")

        Column(modifier = Modifier.fillMaxSize()) {
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
                0 -> AppList(apps = allApps, onAppClick = onAppClick)
                1 -> AppList(apps = systemApps, onAppClick = onAppClick)
                2 -> AppList(apps = sideloadedApps, onAppClick = onAppClick)
            }
        }
    }
}
