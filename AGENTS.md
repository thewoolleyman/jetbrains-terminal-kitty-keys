# Agents Guide

## Project Overview

This is a JetBrains IDE plugin (Kotlin/JVM) that intercepts Shift+Enter in the built-in terminal and sends the kitty keyboard protocol CSI u escape sequence (`\x1b[13;2u`) to the PTY.

## Architecture

- **Language**: Kotlin
- **Build**: Gradle with `org.jetbrains.intellij.platform` plugin
- **Target**: JetBrains Platform (IntelliJ-based IDEs)
- **Dependencies**: `org.jetbrains.plugins.terminal` (bundled terminal plugin)

## Key Files

- `src/main/kotlin/com/github/thewoolleyman/jetbrains/terminalkittykeys/SendShiftEnterAction.kt` — The action that sends `\x1b[13;2u` to the terminal PTY
- `src/main/kotlin/com/github/thewoolleyman/jetbrains/terminalkittykeys/ShiftEnterAllowedActionsProvider.kt` — Registers the action so it works inside the terminal despite "Override IDE shortcuts"
- `src/main/resources/META-INF/plugin.xml` — Plugin descriptor, action registration, keybinding, and extension point declarations
- `build.gradle.kts` — Gradle build configuration targeting IntelliJ Platform

## Key APIs

- `TerminalAllowedActionsProvider` — Extension point to allow custom actions in the terminal
- `TerminalView.DATA_KEY` — DataContext key to get the terminal view from an action event
- `TerminalView.sendText()` — Sends text/escape sequences to the terminal PTY (Reworked Terminal)
- `JediTermWidget.getTtyConnector().write()` — Sends raw bytes to PTY (Classic Terminal)

## Conventions

- Plugin ID: `com.github.thewoolleyman.jetbrains.terminalkittykeys`
- Package: `com.github.thewoolleyman.jetbrains.terminalkittykeys`
- Follow JetBrains plugin development conventions
- Target minimum IDE version that supports `TerminalAllowedActionsProvider`
- Always include a newline at the end of all source code files

## Testing

- `./gradlew buildPlugin` — Build the plugin
- `./gradlew runIde` — Launch a sandbox IDE instance with the plugin installed
- Manual testing: Open terminal in sandbox IDE, run a TUI app, press Shift+Enter
