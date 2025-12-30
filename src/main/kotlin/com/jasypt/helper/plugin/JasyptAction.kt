package com.jasypt.helper.plugin

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile

class JasyptAction : AnAction() {

    init {
        templatePresentation.text = I18n.getMessage("menu.jasypt")
        templatePresentation.description = I18n.getMessage("menu.jasypt.description")
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val isSupportedFile = file?.let { isSupportedFileType(it) } ?: false
        e.presentation.isEnabledAndVisible = isSupportedFile
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val dialog = JasyptDialog(project, file)
        dialog.show()
    }

    private fun isSupportedFileType(file: VirtualFile): Boolean {
        val extension = file.extension?.lowercase()
        return extension in listOf("yaml", "yml", "properties")
    }
}
