package com.tertiaryinfotech.fractalapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Hexagon, contentDescription = null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text("Fractal", style = MaterialTheme.typography.headlineSmall)
                Text("Generate infinite colorful fractal art", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }

            HorizontalDivider()
            Text("How to use", style = MaterialTheme.typography.titleMedium)
            Howto(Icons.Default.Abc, "Type a seed", "Any word or number deterministically generates a unique fractal.")
            Howto(Icons.Default.Tune, "Adjust", "Switch fractal type, pick a palette, and fine-tune detail & colour.")
            Howto(Icons.Default.PanTool, "Explore", "Drag to pan, pinch or double-tap to zoom into endless detail.")
            Howto(Icons.Default.Download, "Save & share", "Export high-resolution artwork to Photos or share it anywhere.")

            HorizontalDivider()
            Text("Privacy", style = MaterialTheme.typography.titleMedium)
            Text(
                "Everything runs on-device with OpenGL. No account, no network, no tracking. " +
                    "Storage access is requested only when you save an image.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Howto(icon: ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
