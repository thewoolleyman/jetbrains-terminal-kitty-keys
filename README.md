# JetBrains Terminal Kitty Keys

[![CI](https://github.com/thewoolleyman/jetbrains-terminal-kitty-keys/actions/workflows/ci.yml/badge.svg)](https://github.com/thewoolleyman/jetbrains-terminal-kitty-keys/actions/workflows/ci.yml)
[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/30338-terminal-kitty-keys.svg)](https://plugins.jetbrains.com/plugin/30338-terminal-kitty-keys)
[![JetBrains Plugin Downloads](https://img.shields.io/jetbrains/plugin/d/30338-terminal-kitty-keys.svg)](https://plugins.jetbrains.com/plugin/30338-terminal-kitty-keys)

<a href="https://www.buymeacoffee.com/thewoolleyman" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/arial-yellow.png" alt="Buy Me A Coffee" height="60"></a>
<img src="docs/buy-me-a-coffee-qr-code.png" alt="Buy Me A Coffee QR Code" height="60">

A JetBrains IDE plugin that sends [kitty keyboard protocol](https://sw.kovidgoyal.net/kitty/keyboard-protocol/) escape sequences for Shift+Enter in the built-in terminal.

## JetBrains' Built-in Fix (2025.3.3+)

As of JetBrains 2025.3.3 (February 2026), the IDE includes a built-in fix for Shift+Enter in the terminal. See [IJPL-221848](https://youtrack.jetbrains.com/issue/IJPL-221848) for details.

The built-in fix sends `ESC + CR` (`\x1b\r`) when Shift+Enter is pressed — the same byte sequence as Alt+Enter (Option+Enter on macOS). This is controlled by a new setting at **Settings > Advanced Settings > Terminal > "Send Esc+CR on Shift+Enter"** (enabled by default). Most modern TUI apps, including Claude Code, Gemini CLI, and Codex CLI, interpret `ESC+CR` as "insert newline," so **Shift+Enter works out of the box in 2025.3.3+ for the majority of use cases.**

### When you still need this plugin

The JetBrains built-in fix uses `ESC+CR` (`\x1b\r`), which is an informal convention — not the [kitty keyboard protocol](https://sw.kovidgoyal.net/kitty/keyboard-protocol/) standard. This plugin sends the proper kitty CSI u sequence `ESC[13;2u` instead, which matters in these scenarios:

1. **TUI apps that only support kitty CSI u, not ESC+CR** — Any TUI that implements the kitty keyboard protocol but does not have ad-hoc `ESC+CR` handling will not recognize the JetBrains built-in fix. Examples include [supercoder](https://github.com/huytd/supercoder), [opencode](https://github.com/anomalyco/opencode), and [dexto](https://github.com/truffle-ai/dexto).

2. **Distinguishing Shift+Enter from Alt+Enter** — With the JetBrains fix, both key combos send identical bytes (`\x1b\r`), making them permanently aliased. If a TUI uses Shift+Enter and Alt+Enter for different actions, the built-in fix cannot differentiate them. This plugin sends `\x1b[13;2u` for Shift+Enter, leaving Alt+Enter as `\x1b\r` — the TUI sees two distinct inputs.

3. **Older JetBrains IDE versions (pre-2025.3.3)** — The built-in fix is not available. This plugin provides Shift+Enter support on 2025.2 and earlier.

4. **Future-proofing** — The kitty keyboard protocol is the direction the terminal ecosystem is heading, with native support in kitty, WezTerm, Ghostty, iTerm2, and Alacritty. Using the standard escape sequence means your JetBrains terminal behaves the same way these terminals do.

## Problem

JetBrains IDEs use JediTerm as their terminal emulator, which does not support the kitty keyboard protocol. This means Shift+Enter sends the same `\r` (carriage return) as plain Enter — TUI applications cannot distinguish between them.

Without either the JetBrains built-in fix or this plugin, pressing Shift+Enter in the JetBrains terminal submits input instead of inserting a newline.

## Solution

This plugin intercepts Shift+Enter at the IDE level (before JediTerm processes it) and injects the kitty CSI u escape sequence `ESC[13;2u` directly into the PTY. Any TUI application that supports the kitty keyboard protocol will then correctly interpret Shift+Enter as a newline.

## Compatibility

- **JetBrains IDEs**: IntelliJ IDEA, GoLand, PyCharm, WebStorm, PhpStorm, RubyMine, CLion, Rider, and others
- **Terminal engines**: Classic (JediTerm) and Reworked Terminal
- **TUI apps that benefit**: Claude Code, Gemini CLI, OpenAI Codex CLI, and any TUI using the kitty keyboard protocol

## Installation

### From JetBrains Marketplace

Install directly from your IDE: **Settings > Plugins > Marketplace**, search for **Terminal Kitty Keys**, or visit the [plugin page](https://plugins.jetbrains.com/plugin/30338-terminal-kitty-keys).

### From Disk (Local Build)

1. Build the plugin: `./gradlew buildPlugin`
2. In your JetBrains IDE: **Settings > Plugins > Gear icon > Install Plugin from Disk**
3. Select the built `.zip` from `build/distributions/`
4. Restart the IDE

## Acceptance Testing

The key difference between the JetBrains built-in fix and this plugin is the escape sequence sent to the PTY. You can verify which sequence is being sent using a simple test.

### Test script

Run this in your JetBrains terminal to display the raw bytes received when you press keys:

```sh
python3 -c "
import sys, tty, termios
fd = sys.stdin.fileno()
old = termios.tcgetattr(fd)
tty.setraw(fd)
print('Press Shift+Enter (then Ctrl+C to exit):\r')
try:
    while True:
        ch = sys.stdin.read(1)
        if ord(ch) == 3: break  # Ctrl+C
        print(f'  byte: 0x{ord(ch):02x} ({repr(ch)})\r')
finally:
    termios.tcsetattr(fd, termios.TCSADRAIN, old)
"
```

### Expected results

**Without plugin or built-in fix** (Enter and Shift+Enter are identical):
```
Press Shift+Enter (then Ctrl+C to exit):
  byte: 0x0d ('\r')
```

**With JetBrains built-in fix (2025.3.3+)** — sends ESC+CR:
```
Press Shift+Enter (then Ctrl+C to exit):
  byte: 0x1b ('\x1b')
  byte: 0x0d ('\r')
```

**With this plugin** — sends kitty CSI u (`ESC[13;2u`):
```
Press Shift+Enter (then Ctrl+C to exit):
  byte: 0x1b ('\x1b')
  byte: 0x5b ('[')
  byte: 0x31 ('1')
  byte: 0x33 ('3')
  byte: 0x3b (';')
  byte: 0x32 ('2')
  byte: 0x75 ('u')
```

### Test scenarios

| Scenario | Shift+Enter sends | Test result |
|---|---|---|
| No fix (pre-2025.3.3, no plugin) | `\r` (1 byte) | Same as plain Enter — **FAIL** |
| JetBrains built-in fix only | `\x1b\r` (2 bytes) | ESC+CR — works for most TUIs but aliases with Alt+Enter |
| This plugin installed | `\x1b[13;2u` (7 bytes) | Kitty CSI u — **PASS** (proper standard, distinct from Alt+Enter) |

### Disabling the built-in fix

If you're on 2025.3.3+ and want to test that the plugin works independently of the built-in fix, disable it at **Settings > Advanced Settings > Terminal > "Send Esc+CR on Shift+Enter"**.

## Real-World Example: Neovim `<S-CR>` Mappings

[Neovim](https://neovim.io/) (0.8+) is available on both macOS (`brew install neovim`) and Linux (all major distributions). It queries the terminal for CSI u support at startup and enables modifier-aware key mappings when available.

This is the clearest real-world example where the JetBrains built-in fix (`ESC+CR`) is **not sufficient** and kitty CSI u is required.

### The problem

Neovim lets you map `<S-CR>` (Shift+Enter) and `<CR>` (Enter) to different actions. For example, a common setup in completion plugins:

- `<CR>` — confirm completion
- `<S-CR>` — confirm completion and add a newline

Without kitty CSI u, Neovim cannot detect the Shift modifier. The `<S-CR>` mapping silently falls through to `<CR>`, and the user has no way to trigger Shift+Enter–specific behavior.

JetBrains' built-in fix sends `ESC+CR` (`\x1b\r`), which Neovim interprets as `<M-CR>` (Meta/Alt+Enter) — **not** `<S-CR>` (Shift+Enter). So the built-in fix does not help here.

### How to test

1. Install neovim: `brew install neovim` (macOS) or `apt install neovim` (Debian/Ubuntu)
2. Open a JetBrains terminal and run:
   ```sh
   nvim -c 'nnoremap <S-CR> :echo "Shift+Enter detected!"<CR>' -c 'nnoremap <CR> :echo "Plain Enter detected"<CR>'
   ```
3. Press **Enter** in normal mode — you should see "Plain Enter detected"
4. Press **Shift+Enter** in normal mode:
   - **Without plugin**: Shows "Plain Enter detected" (Neovim cannot distinguish the keys)
   - **With plugin**: Shows "Shift+Enter detected!" (Neovim receives CSI u and recognizes `<S-CR>`)

### Note on fish shell

[Fish shell](https://fishshell.com/) (4.0+) is **not** a good test case for this plugin. Fish binds Alt+Enter (`\e\n`) to insert a newline, and JetBrains' built-in fix sends the same ESC+CR sequence for Shift+Enter — so fish works correctly without this plugin on JetBrains 2025.3.3+. See [fish interactive docs](https://fishshell.com/docs/current/interactive.html) for details.

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

1. Binds Shift+Enter to a custom `AnAction` via `<keyboard-shortcut first-keystroke="shift ENTER"/>`
2. Gets the active terminal's `TtyConnector` via `TerminalToolWindowManager`
3. Writes `\x1b[13;2u` (kitty CSI u: keycode 13 = Enter, modifier 2 = Shift) directly to the PTY

**Note**: `TerminalAllowedActionsProvider` (which ensures the action fires when the terminal's "Override IDE shortcuts" is enabled) requires the `terminal.frontend` API available from 2025.3+. For earlier versions, you may need to disable "Override IDE shortcuts" in **Settings > Tools > Terminal**.

## Related Issues

- [IJPL-221848](https://youtrack.jetbrains.com/issue/IJPL-221848) — Make Shift+Enter insert a new line in ClaudeCode (fixed in 2025.3.3 with ESC+CR)
- [IJPL-102718](https://youtrack.jetbrains.com/issue/IJPL-102718) — Reworked Terminal: support inserting new line in the prompt by Shift+Enter
- [anthropics/claude-code#4796](https://github.com/anthropics/claude-code/issues/4796) — JetBrains Plugin Doesn't Support Shift+Enter
- [GO-19305](https://youtrack.jetbrains.com/issue/GO-19305) — Make Shift+Enter insert a new line in Claude Code

## License

MIT
