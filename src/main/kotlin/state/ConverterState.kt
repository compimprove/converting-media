package state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.ConversionPreset
import java.nio.file.Path

class ConverterState {
    var selectedPreset by mutableStateOf(ConversionPreset.VIDEO_TO_MP3)
    var selectedFiles by mutableStateOf<List<Path>>(emptyList())
    var lastOutputFiles by mutableStateOf<List<Path>>(emptyList())
    var statusMessage by mutableStateOf(ConversionPreset.VIDEO_TO_MP3.sourcePrompt)
    var isConverting by mutableStateOf(false)

    fun resetSelections() {
        selectedFiles = emptyList()
        lastOutputFiles = emptyList()
        statusMessage = selectedPreset.sourcePrompt
    }
}
