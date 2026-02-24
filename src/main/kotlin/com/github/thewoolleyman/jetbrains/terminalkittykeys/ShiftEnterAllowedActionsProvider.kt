package com.github.thewoolleyman.jetbrains.terminalkittykeys

import com.intellij.terminal.frontend.view.TerminalAllowedActionsProvider

/**
 * Registers the Shift+Enter action so it is not blocked by the terminal's
 * "Override IDE shortcuts" feature in the Reworked Terminal.
 */
class ShiftEnterAllowedActionsProvider : TerminalAllowedActionsProvider {
    override fun getActionIds(): List<String> = listOf("TerminalKittyKeys.SendShiftEnter")
}
