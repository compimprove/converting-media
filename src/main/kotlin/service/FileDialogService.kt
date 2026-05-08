package service

import model.ConversionPreset
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import java.nio.file.Path
import javax.swing.JFileChooser

class FileDialogService {
    fun chooseInputFiles(preset: ConversionPreset): List<Path> {
        val dialog = FileDialog(null as Frame?, preset.label, FileDialog.LOAD)
        dialog.isMultipleMode = true
        dialog.filenameFilter = FilenameFilter { _, name ->
            val normalizedName = name.lowercase()
            preset.inputExtensions.any { extension -> normalizedName.endsWith(".$extension") }
        }
        dialog.isVisible = true

        return dialog.files
            ?.map { it.toPath() }
            ?.filter { path ->
                val normalizedName = path.fileName.toString().lowercase()
                preset.inputExtensions.any { extension -> normalizedName.endsWith(".$extension") }
            }
            ?: emptyList()
    }

    fun chooseOutputDirectory(): Path? {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.isMultiSelectionEnabled = false
        chooser.dialogTitle = "Choose Output Folder"

        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile?.toPath() else null
    }
}
