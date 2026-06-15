package com.tertiaryinfotech.fractalapp.ui

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tertiaryinfotech.fractalapp.gl.FractalGLSurfaceView
import com.tertiaryinfotech.fractalapp.util.PhotoSaver
import com.tertiaryinfotech.fractalapp.vm.FractalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: FractalViewModel = viewModel(), initialPreset: String? = null) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { initialPreset?.let { vm.applyByName(it) } }
    val scope = rememberCoroutineScope()
    var showControls by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var pendingSave by remember { mutableStateOf(false) }

    val glView = remember {
        FractalGLSurfaceView(context).apply {
            setParameters(vm.parameters)
            onParametersChanged = { vm.onParametersChanged(it) }
        }
    }
    // Keep the GL view in sync with externally-driven parameter changes.
    LaunchedEffect(vm.parameters) { glView.setParameters(vm.parameters) }
    DisposableEffect(Unit) {
        vm.captureProvider = { size, cb -> glView.capture(size, cb) }
        onDispose { vm.captureProvider = null }
    }

    fun doSave() {
        isSaving = true
        vm.saveToPhotos { bmp ->
            scope.launch {
                val ok = bmp != null && withContext(Dispatchers.IO) {
                    PhotoSaver.save(context, bmp, "Fractal-${System.currentTimeMillis()}")
                }
                isSaving = false
                vm.showToast(if (ok) "Saved to Photos" else "Save failed")
            }
        }
    }

    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingSave) doSave()
        else if (!granted) vm.showToast("Storage permission needed to save")
        pendingSave = false
    }

    fun saveWithPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            pendingSave = true
            storagePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else doSave()
    }

    fun doShare() {
        vm.prepareShare { bmp ->
            if (bmp == null) { vm.showToast("Couldn't render image"); return@prepareShare }
            scope.launch {
                val uri = withContext(Dispatchers.IO) {
                    PhotoSaver.cacheForShare(context, bmp, "fractal-share")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share fractal"))
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { glView }, modifier = Modifier.fillMaxSize())

        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
                    Text(
                        vm.parameters.type.title,
                        Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        color = Color.White, style = MaterialTheme.typography.titleSmall
                    )
                }
                Spacer(Modifier.weight(1f))
                CircleIcon(Icons.Default.GridView) { showGallery = true }
                Spacer(Modifier.width(8.dp))
                CircleIcon(Icons.Default.Info) { showAbout = true }
            }

            Spacer(Modifier.weight(1f))

            // Seed bar
            Surface(color = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(16.dp)) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = vm.seedText,
                        onValueChange = { vm.seedText = it },
                        placeholder = { Text("Type a seed — e.g. nebula") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { vm.applySeed() }),
                    )
                    Button(
                        onClick = { vm.applySeed() },
                        enabled = vm.seedText.isNotBlank(),
                        shape = RoundedCornerShape(50),
                    ) { Text("Generate") }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action bar
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(Icons.Outlined.Casino, "Surprise", Modifier.weight(1f)) { vm.randomize() }
                ActionButton(Icons.Default.Tune, "Adjust", Modifier.weight(1f)) { showControls = true }
                ActionButton(if (isSaving) Icons.Default.HourglassEmpty else Icons.Default.Download,
                    "Save", Modifier.weight(1f), enabled = !isSaving) { saveWithPermission() }
                ActionButton(Icons.Default.Share, "Share", Modifier.weight(1f)) { doShare() }
            }
        }

        // Toast
        vm.toast?.let { msg ->
            LaunchedEffect(msg) { delay(2000); vm.clearToast() }
            Surface(
                color = Color(0xFF222028).copy(alpha = 0.95f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 150.dp)
            ) {
                Text(msg, Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = Color.White)
            }
        }
    }

    if (showControls) ControlsSheet(vm) { showControls = false }
    if (showGallery) GallerySheet(vm) { showGallery = false }
    if (showAbout) AboutSheet { showAbout = false }
}

@Composable
private fun CircleIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(color = Color.White.copy(alpha = 0.12f), shape = CircleShape, onClick = onClick) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(9.dp).size(20.dp))
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        color = Color.White.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Column(
            Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}
