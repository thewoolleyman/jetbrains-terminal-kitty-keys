# JetBrains Terminal Kitty Keys

A JetBrains IDE plugin that sends [kitty keyboard protocol](https://sw.kovidgoyal.net/kitty/keyboard-protocol/) escape sequences for Shift+Enter in the built-in terminal.

## Problem

JetBrains IDEs use JediTerm as their terminal emulator, which does not support the kitty keyboard protocol. This means Shift+Enter sends the same `\r` (carriage return) as plain Enter — TUI applications like Claude Code, Gemini CLI, and Codex cannot distinguish between them.

Without this plugin, pressing Shift+Enter in the JetBrains terminal submits input instead of inserting a newline.

## Solution

This plugin intercepts Shift+Enter at the IDE level (before JediTerm processes it) and injects the kitty CSI u escape sequence `ESC[13;2u` directly into the PTY. Any TUI application that supports the kitty keyboard protocol will then correctly interpret Shift+Enter as a newline.

## Compatibility

- **JetBrains IDEs**: IntelliJ IDEA, GoLand, PyCharm, WebStorm, PhpStorm, RubyMine, CLion, Rider, and others
- **Terminal engines**: Classic (JediTerm) and Reworked Terminal
- **TUI apps that benefit**: Claude Code, Gemini CLI, OpenAI Codex CLI, and any TUI using the kitty keyboard protocol

## Installation

### From Disk (Local Build)

1. Build the plugin: `./gradlew buildPlugin`
2. In your JetBrains IDE: **Settings > Plugins > Gear icon > Install Plugin from Disk**
3. Select the built `.zip` from `build/distributions/`
4. Restart the IDE

## Development

### Prerequisites

- JDK 17+
- The project uses the Gradle IntelliJ Plugin for building

### Build

```sh
./gradlew buildPlugin
```

### Run in Sandbox IDE

```sh
./gradlew runIde
```

## How It Works

1. Registers a `TerminalAllowedActionsProvider` so the Shift+Enter action is not blocked by the terminal's "Override IDE shortcuts" feature
2. Binds Shift+Enter to a custom `AnAction`
3. When triggered, sends `\x1b[13;2u` (kitty CSI u: keycode 13 = Enter, modifier 2 = Shift) to the terminal PTY

## Related Issues

- [IJPL-102718](https://youtrack.jetbrains.com/issue/IJPL-102718) — Reworked Terminal: support inserting new line in the prompt by Shift+Enter
- [anthropics/claude-code#4796](https://github.com/anthropics/claude-code/issues/4796) — JetBrains Plugin Doesn't Support Shift+Enter
- [GO-19305](https://youtrack.jetbrains.com/issue/GO-19305) — Make Shift+Enter insert a new line in Claude Code

## License

MIT
