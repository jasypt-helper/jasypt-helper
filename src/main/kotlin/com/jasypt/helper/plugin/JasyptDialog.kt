package com.jasypt.helper.plugin

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JComponent

class JasyptDialog(
    private val project: Project,
    private val file: VirtualFile
) : DialogWrapper(project, true) {
    private val passwordField = JBTextField()
    private val algorithmComboBox = ComboBox<String>(250)

    init {
        isResizable = false
        title = I18n.getMessage("dialog.title")
        init()

        val allAlgorithms = JasyptUtils.getAllAlgorithms()

        allAlgorithms.forEach { algorithm ->
            algorithmComboBox.addItem(algorithm)
        }

        val savedAlgorithm = getSavedAlgorithm()
        if (savedAlgorithm != null && allAlgorithms.contains(savedAlgorithm)) {
            algorithmComboBox.selectedItem = savedAlgorithm
        } else {
            val recommendedAlgorithm = JasyptUtils.getRecommendedAlgorithm()
            algorithmComboBox.selectedItem = recommendedAlgorithm
        }
    }

    private fun getSavedAlgorithm(): String? {
        return PropertiesComponent.getInstance(project).getValue("jasypt.selected.algorithm")
    }

    private fun saveSelectedAlgorithm(algorithm: String) {
        PropertiesComponent.getInstance(project).setValue("jasypt.selected.algorithm", algorithm)
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row(I18n.getMessage("dialog.password")) {
                cell(passwordField)
                    .focused()
                    .applyToComponent {
                        toolTipText = I18n.getMessage("dialog.password.tooltip")
                    }
                    .widthGroup("fields")
            }
            row(I18n.getMessage("dialog.algorithm")) {
                cell(algorithmComboBox)
                    .applyToComponent {
                        toolTipText = I18n.getMessage("dialog.algorithm.tooltip")
                    }
                    .widthGroup("fields")
            }
        }
    }

    override fun createActions(): Array<AbstractAction> {
        return arrayOf(
            EncryptAction(),
            DecryptAction()
        )
    }

    private fun saveFileContent(content: String) {
        try {
            ApplicationManager.getApplication().runWriteAction {
                file.setBinaryContent(content.toByteArray())

                file.refresh(false, false)

                val documentManager = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
                val document = documentManager.getDocument(file)
                if (document != null) {
                    documentManager.reloadFromDisk(document)
                }
            }
        } catch (e: Exception) {
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                I18n.getMessage("message.save.file.error", e.message ?: ""),
                title
            )
        }
    }

    private fun processFile(encrypt: Boolean) {
        val password = passwordField.text
        if (password.isBlank()) {
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                I18n.getMessage("message.enter.password"),
                title
            )
            return
        }

        val algorithm = algorithmComboBox.selectedItem as? String ?: "PBEWithMD5AndDES"

        saveSelectedAlgorithm(algorithm)

        val originalContent = try {
            getLatestFileContent()
        } catch (e: Exception) {
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                I18n.getMessage("message.read.file.error", e.message ?: ""),
                title
            )
            return
        }

        try {
            val processedContent = if (encrypt) {
                encryptContent(originalContent, password, algorithm)
            } else {
                decryptContent(originalContent, password, algorithm)
            }

            saveFileContent(processedContent)

            com.intellij.openapi.ui.Messages.showInfoMessage(
                project,
                if (encrypt) I18n.getMessage("message.encrypt.success") else I18n.getMessage("message.decrypt.success"),
                title
            )
        } catch (e: Exception) {
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                I18n.getMessage("message.process.error", e.message ?: e.toString()),
                title
            )
        }
    }

    private fun getLatestFileContent(): String {
        val documentManager = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
        val document = documentManager.getDocument(file)
        return document?.text ?: String(file.contentsToByteArray())
    }

    private fun encryptContent(content: String, password: String, algorithm: String): String {
        return JasyptUtils.encryptContent(content, password, algorithm)
    }

    private fun decryptContent(content: String, password: String, algorithm: String): String {
        return JasyptUtils.decryptContent(content, password, algorithm)
    }

    inner class EncryptAction : AbstractAction(I18n.getMessage("dialog.encrypt")) {
        override fun actionPerformed(e: ActionEvent?) {
            processFile(true)
        }
    }

    inner class DecryptAction : AbstractAction(I18n.getMessage("dialog.decrypt")) {
        override fun actionPerformed(e: ActionEvent?) {
            processFile(false)
        }
    }
}
