package com.example.linkgrab

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                Surface(Modifier.fillMaxSize()) { Screen() }
            }
        }
    }
}

private enum class Kind(val label: String, val folder: String) {
    Video("Video", Environment.DIRECTORY_MOVIES),
    Audio("Audio", Environment.DIRECTORY_MUSIC),
    File("Other", Environment.DIRECTORY_DOWNLOADS)
}


private fun start(context: Context, link: String, kind: Kind): String {
    val url = link.trim()
    val uri = Uri.parse(url)
    val host = uri.host?.lowercase()
    if (host == null || uri.scheme != "https") return "Enter a valid https:// link."
    return try {
        val name = URLUtil.guessFileName(url, null, null)
        val request = DownloadManager.Request(uri)
            .setTitle(name)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(kind.folder, name)
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        "Download started. Check your notifications."
    } catch (e: Exception) {
        "Couldn't start the download: ${e.message ?: "unknown error"}"
    }
}

@Composable
private fun Screen() {
    val context = LocalContext.current
    var link by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(Kind.Video) }
    var message by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("LinkGrab", style = MaterialTheme.typography.displaySmall)
        Text(
            "Paste a direct link to a video or audio file.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text("Link") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Save as", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Kind.values().forEach {
                FilterChip(selected = kind == it, onClick = { kind = it }, label = { Text(it.label) })
            }
        }
        Button(
            onClick = { message = start(context, link, kind) },
            enabled = link.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Download") }
        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
