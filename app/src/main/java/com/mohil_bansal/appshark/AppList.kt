package com.mohil_bansal.appshark

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohil_bansal.appshark.data.AppInfo

@Composable
fun AppList(apps: List<AppInfo>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        (items(apps) { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.appName,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp
                )
                Text(text = app.technology, fontSize = 14.sp)
            }
            HorizontalDivider()
        })

    }
}
