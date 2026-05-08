package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.ConversionCategory
import model.ConversionPreset
import service.ConversionException
import service.ConversionService
import service.FileDialogService
import state.ConverterState
import java.awt.Desktop
import java.nio.file.Path

@Composable
fun ConverterApp() {
    val state = remember { ConverterState() }
    val fileDialogService = remember { FileDialogService() }
    val conversionService = remember { ConversionService() }
    val scope = rememberCoroutineScope()
    val palette = remember { DarkAppPalette }
    val supportedPresets = remember { ConversionPreset.entries.filter(conversionService::isPresetSupported).toSet() }

    if (state.selectedPreset !in supportedPresets) {
        state.selectedPreset = supportedPresets.firstOrNull() ?: ConversionPreset.VIDEO_TO_MP3
        state.resetSelections()
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(palette.backgroundTop, palette.backgroundBottom)
                    )
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1120.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Header(palette)
                        PresetSection(state, palette, supportedPresets)
                        SourceSection(
                            state = state,
                            palette = palette,
                            onChooseFiles = {
                                val files = fileDialogService.chooseInputFiles(state.selectedPreset)
                                if (files.isNotEmpty()) {
                                    state.selectedFiles = files
                                    state.statusMessage = "Ready to convert ${files.size} file${if (files.size == 1) "" else "s"}."
                                }
                            },
                            onConvert = {
                                val outputDir = fileDialogService.chooseOutputDirectory()
                                if (outputDir == null) {
                                    state.statusMessage = "Conversion canceled."
                                    return@SourceSection
                                }

                                val inputs = state.selectedFiles
                                val preset = state.selectedPreset
                                state.isConverting = true
                                state.statusMessage = "Converting ${inputs.size} file${if (inputs.size == 1) "" else "s"}..."

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val outputs = conversionService.convert(inputs, outputDir, preset)
                                        launch(Dispatchers.Main) {
                                            state.isConverting = false
                                            state.lastOutputFiles = outputs
                                            state.statusMessage = "Saved ${outputs.size} file${if (outputs.size == 1) "" else "s"}."
                                        }
                                    } catch (error: ConversionException) {
                                        launch(Dispatchers.Main) {
                                            state.isConverting = false
                                            state.statusMessage = error.message ?: "Conversion failed."
                                        }
                                    } catch (error: Exception) {
                                        launch(Dispatchers.Main) {
                                            state.isConverting = false
                                            state.statusMessage = error.message ?: "Conversion failed."
                                        }
                                    }
                                }
                            },
                            onClear = { state.resetSelections() },
                            onOpenOutputFolder = { revealOutputs(state.lastOutputFiles) },
                        )
                        StatusSection(state, palette)
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(palette: AppPalette) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = palette.heroBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Converting Media",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primaryText,
            )
            Text(
                text = "A macOS Kotlin utility for video and sound conversion with bundled ffmpeg. Image conversion is reserved for a later update.",
                fontSize = 16.sp,
                color = palette.secondaryText,
            )
        }
    }
}

@Composable
private fun PresetSection(
    state: ConverterState,
    palette: AppPalette,
    supportedPresets: Set<ConversionPreset>,
) {
    val supportedCategories = ConversionCategory.entries.filter { category ->
        ConversionPreset.entries.any { it.category == category && it in supportedPresets }
    }
    val selectedCategory = state.selectedPreset.category
    val visiblePresets = ConversionPreset.entries.filter { it.category == selectedCategory && it in supportedPresets }

    SectionCard(title = "Conversion Functions", palette = palette) {
        if (supportedCategories.isNotEmpty()) {
            CategoryTabs(
                categories = supportedCategories,
                selectedCategory = selectedCategory,
                palette = palette,
                onSelectCategory = { category ->
                    if (category != state.selectedPreset.category) {
                        val firstPreset = ConversionPreset.entries.firstOrNull {
                            it.category == category && it in supportedPresets
                        } ?: return@CategoryTabs
                        state.selectedPreset = firstPreset
                        state.resetSelections()
                    }
                },
            )

            val categoryDescription = selectedCategory.description
            Text(
                text = categoryDescription,
                fontSize = 13.sp,
                color = palette.mutedText,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                visiblePresets.forEach { preset ->
                    val selected = state.selectedPreset == preset
                    Button(
                        onClick = {
                            state.selectedPreset = preset
                            state.resetSelections()
                        },
                        colors = themedButtonColors(selected, palette),
                    ) {
                        Text(preset.label)
                    }
                }
            }
        }

        Text(
            text = state.selectedPreset.description,
            fontSize = 14.sp,
            color = palette.secondaryText,
        )
    }
}

@Composable
private fun CategoryTabs(
    categories: List<ConversionCategory>,
    selectedCategory: ConversionCategory,
    palette: AppPalette,
    onSelectCategory: (ConversionCategory) -> Unit,
) {
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceBackground),
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = palette.primaryText,
        ) {
            categories.forEach { category ->
                val selected = category == selectedCategory
                Tab(
                    selected = selected,
                    onClick = { onSelectCategory(category) },
                    text = {
                        Text(
                            text = category.label.removeSuffix(" Converting"),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    },
                    selectedContentColor = palette.primaryText,
                    unselectedContentColor = palette.mutedText,
                )
            }
        }
    }    
}

@Composable
private fun SourceSection(
    state: ConverterState,
    palette: AppPalette,
    onChooseFiles: () -> Unit,
    onConvert: () -> Unit,
    onClear: () -> Unit,
    onOpenOutputFolder: () -> Unit,
) {
    SectionCard(
        title = "Source Files",
        palette = palette,
        headerActions = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CompactActionButton(
                    text = "Convert",
                    palette = palette,
                    selected = true,
                    enabled = state.selectedFiles.isNotEmpty() && !state.isConverting,
                    onClick = onConvert,
                )
                CompactActionButton(
                    text = "Clear",
                    palette = palette,
                    selected = false,
                    enabled = !state.isConverting,
                    onClick = onClear,
                )
                CompactActionButton(
                    text = "Open Output Folder",
                    palette = palette,
                    selected = false,
                    enabled = state.lastOutputFiles.isNotEmpty() && !state.isConverting,
                    onClick = onOpenOutputFolder,
                )
            }
        },
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surfaceBackground),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (state.selectedFiles.isEmpty()) "No files selected" else "${state.selectedFiles.size} file${if (state.selectedFiles.size == 1) "" else "s"} selected",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryText,
                )

                if (state.selectedFiles.isEmpty()) {
                    Text(
                        text = state.selectedPreset.sourcePrompt,
                        fontSize = 14.sp,
                        color = palette.mutedText,
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.selectedFiles.forEach { file ->
                            FileRow(file, palette)
                        }
                    }
                }

                Button(
                    onClick = onChooseFiles,
                    enabled = !state.isConverting,
                    colors = themedButtonColors(selected = true, palette = palette),
                ) {
                    Text("Choose Files")
                }
            }
        }
    }
}

@Composable
private fun FileRow(path: Path, palette: AppPalette) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "•",
            color = palette.mutedText,
            fontSize = 18.sp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = path.fileName.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = palette.fileNameText,
            )
            Text(
                text = path.toString(),
                fontSize = 12.sp,
                color = palette.mutedText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusSection(state: ConverterState, palette: AppPalette) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.isConverting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = state.statusMessage,
                fontSize = 14.sp,
                color = palette.statusText,
            )
        }
    }
}

@Composable
private fun themedButtonColors(selected: Boolean, palette: AppPalette) = ButtonDefaults.buttonColors(
    containerColor = if (selected) palette.primaryButton else palette.secondaryButton,
    contentColor = if (selected) palette.primaryButtonText else palette.secondaryButtonText,
)

@Composable
private fun CompactActionButton(
    text: String,
    palette: AppPalette,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = themedButtonColors(selected = selected, palette = palette),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    palette: AppPalette,
    headerActions: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.primaryText,
                )
                headerActions?.invoke()
            }
            content()
        }
    }
}

private data class AppPalette(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val heroBackground: Color,
    val cardBackground: Color,
    val surfaceBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val statusText: Color,
    val fileNameText: Color,
    val primaryButton: Color,
    val primaryButtonText: Color,
    val secondaryButton: Color,
    val secondaryButtonText: Color,
)

private val DarkAppPalette = AppPalette(
    backgroundTop = Color(0xFF08111F),
    backgroundBottom = Color(0xFF132238),
    heroBackground = Color(0xB3172438),
    cardBackground = Color(0xAA162437),
    surfaceBackground = Color(0xFF1C2B40),
    primaryText = Color(0xFFF8FAFC),
    secondaryText = Color(0xFFD0DAE8),
    mutedText = Color(0xFF9FB0C4),
    statusText = Color(0xFFE2E8F0),
    fileNameText = Color(0xFFF8FAFC),
    primaryButton = Color(0xFF4D8DFF),
    primaryButtonText = Color.White,
    secondaryButton = Color(0xFF2B3B55),
    secondaryButtonText = Color(0xFFE2E8F0),
)

private fun revealOutputs(outputs: List<Path>) {
    if (outputs.isEmpty()) return

    val finder = Desktop.getDesktop()
    outputs.firstOrNull()?.parent?.toFile()?.let { directory ->
        finder.open(directory)
    }
}
