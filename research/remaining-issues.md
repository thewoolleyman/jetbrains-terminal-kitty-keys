# Remaining Issues Not Addressed by JetBrains Built-in Fix

Research into open and closed issues across JetBrains YouTrack and GitHub where the JetBrains 2025.3.3+ built-in fix (ESC+CR) is insufficient and the Terminal Kitty Keys plugin (kitty CSI u / `ESC[13;2u`) would help.

Last updated: 2026-02-24

## JetBrains YouTrack

### IJPL-221848 — "Make Shift+Enter insert a new line in ClaudeCode" (Fixed in 2025.3.3)

https://youtrack.jetbrains.com/issue/IJPL-221848

Key comments not addressed by the built-in fix:

- **@Sewer56**: "They might eat Alt+Enter too." — The ESC+CR fix makes Shift+Enter and Alt+Enter send identical bytes (`\x1b\r`). Any TUI that uses both keys for different actions cannot distinguish them. The plugin sends `\x1b[13;2u` for Shift+Enter, keeping them distinct.
- **@patrickoshaughnessey** (WebStorm 2025.2.1) and **@KSDaemon** (multiple IDEs) — Users on older JetBrains versions have no built-in fix available.
- **@avernet**: "The issue isn't caused by the Claude Code plugin. It occurs even without that plugin, and affects not only `claude` but also `codex` (and I'm sure other tools as well). The problem lies with the JetBrains terminal emulator."
- **@Sewer56**: "Doesn't matter if it's `claude`, `codex`, `opencode`, or whatever it be."
- **@bric3**: "Will it work with other AI agents cli, codex, gemini, mistral, etc.?" — JetBrains answered: only if they handle ESC+CR as newline.

### IJPL-102718 — "Reworked Terminal: support inserting new line in the prompt by Shift+Enter" (Still OPEN)

https://youtrack.jetbrains.com/issue/IJPL-102718

This issue remains open. JetBrains considers the general problem unsolved.

- **Konstantin Hudyakov** (JetBrains): "It is nearly impossible to make Shift+Enter insert a new line in any environment because every CLI/shell might expect different bytes. So, we can only try to address specific cases."
- **Konstantin Hudyakov** acknowledged the fix only works "if they handle Esc+CR byte sequence as a newline" — TUIs that only support kitty CSI u remain broken.
- **Konstantin Hudyakov** confirmed the Classic terminal's Shift+Enter support came from the Terminal Kitty Keys plugin, not from JetBrains itself: "the Shift+Enter in the Classic Terminal actually works because of Claude Code IntelliJ plugin - it adds a custom handler for this key binding and sends custom bytes to the PTY."
- **@bric3**: "Terminal emulators appear to behave inconsistently, Ghostty, iTerm2, and JetBrains classic terminal do allow shift + enter when claude is running. Apple's stock Terminal app does not."

## GitHub — anthropics/claude-code

### #4796 — "JetBrains Plugin Doesn't Support Shift+Enter" (Closed as duplicate)

https://github.com/anthropics/claude-code/issues/4796

Closed as duplicate, pointing to #5114, #1280, and #1262.

- **@DanielMenke**: "Annoying, but alt + Enter actually works" — confirming Alt+Enter as a workaround, but not a proper fix.

### #5114 — "WebStorm: Shift+Enter Shortcut Not Working in Reworked Terminal" (Closed)

https://github.com/anthropics/claude-code/issues/5114

- **@DanielMenke**: "This is likely caused by 'Use Option as Meta key' in Preferences | Tools | Terminal on Macs"
- **@dancherb**: "Also experiencing this on Windows 11, only on Webstorm though (not on VSCode)"

### #1280 — "Multiline Input Broken in PyCharm Claude Plugin Code Editor" (Closed)

https://github.com/anthropics/claude-code/issues/1280

- **@tim-watcha**: "I'm using classic engine" — indicating the issue existed on Classic engine for some users too.

## Summary: Why the Plugin Is Still Needed

### 1. TUI apps that only support kitty CSI u, not ESC+CR

The JetBrains ESC+CR fix only works for TUIs that specifically handle ESC+CR as a newline. TUI apps that only support the kitty keyboard protocol (e.g., supercoder, opencode, dexto) remain broken in JetBrains terminals without this plugin.

### 2. Shift+Enter vs Alt+Enter aliasing

The JetBrains fix makes Shift+Enter and Alt+Enter send identical bytes (`\x1b\r`). Any TUI that uses both keys for different actions (e.g., Neovim `<S-CR>` vs `<M-CR>` mappings) cannot distinguish them. The plugin sends `\x1b[13;2u` for Shift+Enter, keeping them distinct.

### 3. Users on older JetBrains versions (pre-2025.3.3)

The built-in fix is not available on 2025.2 and earlier. Multiple users reported issues on these versions.

### 4. The conditional nature of the JetBrains fix

JetBrains explicitly stated the fix only works "if they handle Esc+CR byte sequence as a newline." The kitty CSI u protocol is a standard that any kitty-protocol-aware TUI understands, whereas ESC+CR is an informal convention that each TUI must separately choose to support.

### 5. The kitty keyboard protocol is the industry direction

Native support exists in kitty, WezTerm, Ghostty, iTerm2, and Alacritty. The kitty keyboard protocol is the standard the terminal ecosystem is converging on for modifier-aware key handling.
