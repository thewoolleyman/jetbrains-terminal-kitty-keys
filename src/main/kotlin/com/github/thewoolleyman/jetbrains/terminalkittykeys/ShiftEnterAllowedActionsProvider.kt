package com.github.thewoolleyman.jetbrains.terminalkittykeys

// TerminalAllowedActionsProvider is in the terminal.frontend module (com.intellij.terminal.frontend.view)
// which is only available as public API from 2025.3+.
//
// For 2025.2 and earlier, we rely on the fact that Shift+Enter is not a shortcut that
// the terminal explicitly captures, so our IDE action should fire without needing
// to be registered as an allowed action.
//
// TODO: When targeting 2025.3+, implement TerminalAllowedActionsProvider to ensure
// the action works even with "Override IDE shortcuts" enabled for all keys.
