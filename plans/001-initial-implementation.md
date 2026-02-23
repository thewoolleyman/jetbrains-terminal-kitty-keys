# Plan: Initial Plugin Implementation

## Goal

Build a minimal JetBrains plugin that intercepts Shift+Enter in the terminal and sends `\x1b[13;2u` (kitty CSI u) to the PTY, enabling Shift+Enter newline support in any TUI app.

## Steps

### 1. Set up Gradle build (`build.gradle.kts`)

- Use the `org.jetbrains.intellij.platform` Gradle plugin
- Target a recent IntelliJ Platform version (2024.2+ where Reworked Terminal exists)
- Declare dependency on the bundled `org.jetbrains.plugins.terminal` plugin
- Set JVM target to 17
- Configure plugin metadata (name, version, vendor)

### 2. Create plugin descriptor (`src/main/resources/META-INF/plugin.xml`)

- Plugin ID: `com.github.thewoolleyman.jetbrains.terminalkittykeys`
- Declare dependency on `org.jetbrains.plugins.terminal`
- Register the Shift+Enter action with `<keyboard-shortcut first-keystroke="shift ENTER"/>`
- Register the `TerminalAllowedActionsProvider` extension so the action works inside the terminal

### 3. Implement `SendShiftEnterAction.kt`

- Extend `AnAction` (or `DumbAwareAction` for simplicity)
- In `actionPerformed()`:
  - Get the terminal from the `AnActionEvent` data context
  - Send `"\u001b[13;2u"` to the terminal PTY
- Support both terminal engines:
  - **Reworked Terminal**: Use `TerminalView.sendText()` if available
  - **Classic Terminal (JediTerm)**: Fall back to `JediTermWidget.getTtyConnector().write()`
- In `update()`: Enable the action only when the terminal is focused

### 4. Implement `ShiftEnterAllowedActionsProvider.kt`

- Implement `TerminalAllowedActionsProvider`
- Return the action ID from `getActionIds()` so the terminal doesn't block it

### 5. Add Gradle wrapper

- Include `gradle/wrapper/gradle-wrapper.jar` and `gradle-wrapper.properties`
- Add `gradlew` and `gradlew.bat` scripts

### 6. Build and test

- `./gradlew buildPlugin` — verify clean build
- `./gradlew runIde` — launch sandbox IDE
- Manual test: open terminal in sandbox IDE, run `claude` or a test TUI, press Shift+Enter

## Key Technical Decisions

### Escape sequence: `\x1b[13;2u`

This is the kitty CSI u encoding for Shift+Enter:
- `\x1b[` — CSI (Control Sequence Introducer)
- `13` — Unicode codepoint for Enter/CR
- `;2` — Modifier: Shift (modifier value = 1, encoded as modifier+1 = 2)
- `u` — CSI u terminator

### Supporting both terminal engines

The Classic terminal (JediTerm) and Reworked Terminal have different APIs:
- Reworked: `TerminalView` with `sendText()`
- Classic: `JediTermWidget` with `getTtyConnector().write()`

We'll try the Reworked API first and fall back to Classic. This ensures the plugin works regardless of which terminal engine the user has enabled.

### Why `TerminalAllowedActionsProvider` is required

JetBrains' terminal has an "Override IDE shortcuts" feature that blocks most IDE actions when the terminal is focused. `TerminalAllowedActionsProvider` is the official way to exempt specific actions from this blocking.

## Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| `TerminalView.sendText()` API may not be available in older IDE versions | Fall back to JediTerm `TtyConnector.write()` |
| Reworked Terminal may intercept Shift+Enter before our action fires | Test on both terminal engines; if Reworked blocks it, document the limitation |
| `TerminalAllowedActionsProvider` may not fully work in Reworked Terminal | JetBrains is actively fixing terminal action support; our plugin will benefit from future fixes |
| Plugin may conflict with Claude Code JetBrains plugin's own Shift+Enter handling | Users should disable the Claude Code plugin's terminal feature if using this plugin |

## Out of Scope (for now)

- Publishing to JetBrains Marketplace (can be added later)
- Supporting other key combinations beyond Shift+Enter
- Configurable escape sequences
- Automated tests (manual testing is sufficient for v1)
