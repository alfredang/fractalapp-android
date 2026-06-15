package com.tertiaryinfotech.fractalapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tertiaryinfotech.fractalapp.model.FractalPreset
import com.tertiaryinfotech.fractalapp.vm.FractalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySheet(vm: FractalViewModel, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text("Gallery", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 520.dp),
            ) {
                items(FractalPreset.all) { preset ->
                    Column(Modifier.clickable { vm.apply(preset); onDismiss() }) {
                        Box(
                            Modifier
                                .fillMaxWidth().height(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(preset.parameters.palette.swatch))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(preset.name, style = MaterialTheme.typography.titleSmall)
                        Text(preset.parameters.type.title, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
