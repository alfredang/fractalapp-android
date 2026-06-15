package com.tertiaryinfotech.fractalapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tertiaryinfotech.fractalapp.model.ColorPalette
import com.tertiaryinfotech.fractalapp.model.FractalType
import com.tertiaryinfotech.fractalapp.vm.FractalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ControlsSheet(vm: FractalViewModel, onDismiss: () -> Unit) {
    val p = vm.parameters
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Adjust", style = MaterialTheme.typography.titleLarge)

            // Fractal type
            SectionLabel("Fractal")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FractalType.entries.forEach { t ->
                    FilterChip(
                        selected = p.type == t,
                        onClick = { vm.update { it.copy(type = t) } },
                        label = { Text(t.title) },
                    )
                }
            }
            Text(p.type.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Palette
            SectionLabel("Palette")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorPalette.entries.forEach { pal ->
                    val selected = p.palette == pal
                    Box(
                        Modifier
                            .width(108.dp).height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(pal.swatch))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable { vm.update { it.copy(palette = pal) } },
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Text(pal.title, Modifier.padding(6.dp), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (p.type.usesSeedPoint) {
                SectionLabel("Seed point (c)")
                LabeledSlider("Real", p.seedX, -1.2..1.2) { v -> vm.update { it.copy(seedX = v) } }
                LabeledSlider("Imaginary", p.seedY, -1.2..1.2) { v -> vm.update { it.copy(seedY = v) } }
            }

            SectionLabel("Detail")
            LabeledSlider("Iterations", p.iterations.toDouble(), 60.0..700.0, decimals = 0) { v ->
                vm.update { it.copy(iterations = v.toInt()) }
            }

            SectionLabel("Colour")
            LabeledSlider("Hue shift", p.colorShift, 0.0..1.0) { v -> vm.update { it.copy(colorShift = v) } }
            LabeledSlider("Density", p.colorDensity, 0.2..4.0) { v -> vm.update { it.copy(colorDensity = v) } }
            LabeledSlider("Interior glow", p.interior, 0.0..1.0) { v -> vm.update { it.copy(interior = v) } }

            OutlinedButton(onClick = { vm.resetView() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Adjust, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reset position & zoom")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun LabeledSlider(
    title: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    decimals: Int = 3,
    onChange: (Double) -> Unit,
) {
    Column {
        Row {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(
                if (decimals == 0) value.toInt().toString() else "%.3f".format(value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
        )
    }
}
