package com.github.thewoolleyman.jetbrains.terminalkittykeys

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.terminal.frontend.view.TerminalView
import com.jediterm.terminal.TtyConnector
import org.jetbrains.plugins.terminal.TerminalToolWindowManager

/**
 * Intercepts Shift+Enter in the terminal and sends the kitty CSI u escape sequence
 * for Shift+Enter (ESC[13;2u) to the PTY.
 *
 * This allows TUI applications that support the kitty keyboard protocol to properly
 * distinguish Shift+Enter from plain Enter.
 */
class SendShiftEnterAction(
    private val classicTerminalConnector: (Project) -> TtyConnector? = ::selectedClassicTerminalConnector,
) : AnAction() {

    companion object {
        private val LOG: Logger = Logger.getInstance(SendShiftEnterAction::class.java)

        // Kitty CSI u encoding for Shift+Enter:
        // ESC[ = CSI, 13 = Enter keycode, ;2 = Shift modifier (1+1), u = terminator
        private const val SHIFT_ENTER_CSI_U: String = "\u001b[13;2u"
    }

    override fun actionPerformed(e: AnActionEvent) {
        // Try the Reworked Terminal API first (TerminalView from data context)
        val terminalView = e.getData(TerminalView.DATA_KEY)
        if (terminalView != null) {
            LOG.info("Sending Shift+Enter CSI u via TerminalView.sendText()")
            try {
                terminalView.sendText(SHIFT_ENTER_CSI_U)
                return
            } catch (ex: Exception) {
                LOG.warn("Failed to write via TerminalView", ex)
            }
        }

        // Fall back to Classic Terminal API (TtyConnector via TerminalToolWindowManager)
        LOG.info("TerminalView not available, falling back to TtyConnector")
        val project = e.project
        if (project == null) {
            LOG.warn("No project available")
            return
        }

        val connector = classicTerminalConnector(project)
        if (connector == null) {
            LOG.warn("No TTY connector available")
            return
        }

        try {
            LOG.info("Sending Shift+Enter CSI u via TtyConnector")
            connector.write(SHIFT_ENTER_CSI_U)
        } catch (ex: Exception) {
            LOG.warn("Failed to write to terminal PTY", ex)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}

private fun selectedClassicTerminalConnector(project: Project): TtyConnector? {
    val toolWindow = TerminalToolWindowManager.getInstance(project).toolWindow
        ?: return null
    val content = toolWindow.contentManager.selectedContent
        ?: return null
    val widget = TerminalToolWindowManager.findWidgetByContent(content)
        ?: return null
    return widget.ttyConnector
}
