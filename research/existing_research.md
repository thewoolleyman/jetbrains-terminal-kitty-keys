# Shift+Enter Newline in Claude Code + JetBrains Terminal

## Summary

Shift+Enter does not insert a newline in Claude Code when running inside a JetBrains IDE's built-in terminal (JediTerm). This is a widely-reported issue with no clean fix available today.

## Root Cause

JediTerm (the Java terminal emulator embedded in all JetBrains IDEs) is an xterm/VT100-compatible emulator. It does **not** support the [kitty keyboard protocol](https://sw.kovidgoyal.net/kitty/keyboard-protocol/), which is what Claude Code relies on to distinguish Shift+Enter from plain Enter.

Without the kitty keyboard protocol, both Enter and Shift+Enter send the same escape sequence (CR / `\r`), so Claude Code cannot tell them apart.

## Failed Approach: Enabling Kitty Keyboard Protocol via Shell Script

We tried a script (`~/.local/bin/fix-terminal-for-claude.sh`) that sent `\e[>1u` to enable the kitty keyboard protocol in the terminal. This was sourced from `~/.zshrc` when `$TERMINAL_EMULATOR == "JetBrains-JediTerm"`.

**Why it failed:**

1. **JediTerm ignores the escape sequence** — it doesn't implement the kitty keyboard protocol, so the `\e[>1u` sequence is silently discarded. Shift+Enter still sends plain CR.
2. **`set -euo pipefail` in the script poisoned the interactive shell** — because the script was `source`d (not run as a subprocess), these strict error-handling flags applied to the entire shell session:
   - `-u` (`nounset`) caused a crash when JetBrains' own shell integration referenced `$ZDOTDIR` (which was unset)
   - `-e` (`errexit`) caused the terminal to close on the first command that returned non-zero (e.g., tab completion failures)
3. **The script was duplicated** in `.zshrc` (two identical `if` blocks sourcing the same file)

**Lesson:** Never use `set -euo pipefail` in a script that will be `source`d into an interactive shell.

## JetBrains Official Fix (2025.3.3+)

**IJPL-221848** — Fixed in JetBrains 2025.3.3 (released week of Feb 16, 2026) and 2026.1 EAP 2.

- Shift+Enter now sends `ESC + CR` (`\x1b\r`) — the same sequence as Alt+Enter
- Configurable via: **Settings > Advanced Settings > Terminal > "Send Esc+CR on Shift+Enter"** (enabled by default)
- Works for TUIs that handle `ESC+CR` as newline (Claude Code, zsh, fish confirmed)
- **Limitation**: This is NOT the kitty CSI u standard — it's the simpler ESC+CR which aliases Shift+Enter with Alt+Enter. TUIs cannot distinguish between the two.

### JetBrains fix vs kitty CSI u

| Approach | Sequence sent | Standard | Shift+Enter ≠ Alt+Enter |
|---|---|---|---|
| JetBrains 2025.3.3 fix | `\x1b\r` (ESC+CR) | No (ad-hoc) | No (aliased) |
| Kitty CSI u protocol | `\x1b[13;2u` | Yes (formal spec) | Yes (distinct) |

## Our Plugin: Terminal Kitty Keys

**Repo**: [thewoolleyman/jetbrains-terminal-kitty-keys](https://github.com/thewoolleyman/jetbrains-terminal-kitty-keys)

A JetBrains plugin that intercepts Shift+Enter at the IDE level and sends the proper kitty CSI u escape sequence (`\x1b[13;2u`) to the PTY. Works with any TUI that supports the kitty keyboard protocol.

### How it works

1. Registers a Shift+Enter `AnAction` with `<keyboard-shortcut first-keystroke="shift ENTER"/>`
2. When triggered, gets the active terminal's `TtyConnector` via `TerminalToolWindowManager`
3. Writes `\x1b[13;2u` directly to the PTY

### Current status

- v0.1.0 builds successfully targeting IntelliJ 2025.2
- `TerminalAllowedActionsProvider` (needed for Reworked Terminal's "Override IDE shortcuts") is deferred to 2025.3+ target — the API is in the `terminal.frontend` module which is only public from 2025.3

## Existing Issues

### Claude Code (GitHub)

- **[#4796](https://github.com/anthropics/claude-code/issues/4796)** — [BUG] JetBrains Plugin Doesn't Support Shift+Enter in Reworked 2025 terminal engine (closed)
- **[#20988](https://github.com/anthropics/claude-code/issues/20988)** — [BUG] Shift+Enter executes prompt instead of creating new line
- **[#9321](https://github.com/anthropics/claude-code/issues/9321)** — Shift+Enter doesn't insert newline, outputs "OM" characters instead
- **[#7958](https://github.com/anthropics/claude-code/issues/7958)** — Shift+Enter submits prompt instead of inserting newline (contradicts documented shortcuts)
- **[#19697](https://github.com/anthropics/claude-code/issues/19697)** — Shift+Enter sometimes creates newline, sometimes sends prompt (reported on PHPStorm + Gnome terminal)
- **[#16859](https://github.com/anthropics/claude-code/issues/16859)** — Shift+Enter doesn't insert newline over SSH
- **[#25349](https://github.com/anthropics/claude-code/issues/25349)** — Shift+Enter no longer inserts newline
- **[#1259](https://github.com/anthropics/claude-code/issues/1259)** — Feature request: Support Shift+Enter for multiline input (industry standard)
- **[#2335](https://github.com/anthropics/claude-code/issues/2335)** — Feature request: configuration option to make Enter insert newline
- **[#1262](https://github.com/anthropics/claude-code/issues/1262)** — Shift+Enter in WSL on Windows (27 comments, most detailed workaround discussion)
- **[#5114](https://github.com/anthropics/claude-code/issues/5114)** — Alt+Enter doesn't work in JetBrains

### JetBrains YouTrack

- **[IJPL-221848](https://youtrack.jetbrains.com/issue/IJPL-221848)** — Make Shift+Enter insert a new line in ClaudeCode (**FIXED in 2025.3.3**)
- **[IJPL-102718](https://youtrack.jetbrains.com/issue/IJPL-102718)** — Reworked Terminal: support inserting new line in the prompt by Shift+Enter (meta-issue, still open)
- **[IDEA-379975](https://youtrack.jetbrains.com/issue/IDEA-379975)** — Reworked Terminal: Can't move to next line with Shift+Enter in PowerShell
- **[GO-19305](https://youtrack.jetbrains.com/issue/GO-19305)** — Make Shift+Enter insert a new line in Claude Code (redirected to IJPL-221848)
- **[IJPL-172083](https://youtrack.jetbrains.com/issue/IJPL-172083)** — Strange character appears every line on shells supporting kitty keyboard protocol (FIXED in 2025.3.1)
- **[FL-15917](https://youtrack.jetbrains.com/issue/FL-15917)** — Investigate the complexity of moving from JediTerm to Alacritty (Fleet-only, discontinued)
- **[IJPL-212342](https://youtrack.jetbrains.com/issue/IJPL-212342)** — [META] AI Agents in Terminal (like Claude Code) — 21 related issues

### JediTerm (GitHub)

- **[JediTerm repo](https://github.com/JetBrains/jediterm)** — No open issue specifically requesting kitty keyboard protocol support

### Google Gemini CLI (GitHub)

- **[#849](https://github.com/google-gemini/gemini-cli/issues/849)** — shift-enter should do an enter but keep user control (closed)
- **[#4161](https://github.com/google-gemini/gemini-cli/issues/4161)** — shift+enter should give a newline (closed)
- **[#15139](https://github.com/google-gemini/gemini-cli/issues/15139)** — Shift enter support on windows (open)
- **[#13431](https://github.com/google-gemini/gemini-cli/issues/13431)** — Support enabling modifyOtherKeys keyboard mode (closed/fixed)
- Other duplicates: #12812, #2668, #15282, #16916, #2532

### OpenAI Codex (GitHub)

- **[#4218](https://github.com/openai/codex/issues/4218)** — Regression: Shift+Enter sends prompt instead of inserting line break (open)
- **[#8603](https://github.com/openai/codex/issues/8603)** — Improve workflow with optional 'ctrl + enter' submission and jump a line with 'shift + enter' (open)
- **[#11559](https://github.com/openai/codex/issues/11559)** — Plan Question UI: Shift+Enter submits instead of inserting newline (open)
- Other duplicates: #8673, #2358

### OpenCode (GitHub)

- **[#4046](https://github.com/anomalyco/opencode/issues/4046)** — Shift-return does not input newline since 1.0 (open)
- **[#11983](https://github.com/anomalyco/opencode/issues/11983)** — Keyboard keybinds configuration not working - input_newline: shift+enter ignored (open)
- Other open issues: #8038, #10877, #7248

## Current Workarounds

1. **Update to JetBrains 2025.3.3+** — Shift+Enter sends ESC+CR which Claude Code interprets as newline
2. **Use the Claude Code JetBrains plugin's built-in terminal** — the plugin handles Shift+Enter at the IDE layer, bypassing the terminal emulator entirely
3. **Use an external terminal** — iTerm2, kitty, WezTerm, or any terminal that supports the kitty keyboard protocol
4. **Use `\` then Enter** — type a backslash before pressing Enter to get a continuation line in Claude Code
5. **Use Option+Enter (Alt+Enter)** — works in JetBrains terminals (sends ESC+CR which Claude Code handles)

## Deep Research Findings (2026-02-22)

### Marketplace search: No existing plugin

Exhaustive search of JetBrains Marketplace (10+ search terms) found zero plugins that fix Shift+Enter or add kitty keyboard protocol support to the terminal.

### GitHub search: No existing plugin

Searched GitHub code for `TerminalAllowedActionsProvider` implementations, CSI u sequences in JetBrains plugin contexts, and related repos. Only reference implementation found: `zeka-stack/zeka-idea-plugin` (registers AI actions, not keyboard fixes).

### Key JetBrains developer insights (from YouTrack comments)

- **Konstantin Hudyakov** (JetBrains Terminal team): "Terminal emulation standards historically don't consider Shift modifier. Same bytes sent for Shift+Enter and Enter. Nearly impossible to make it work universally."
- Confirmed Alt+Enter works in Claude Code, zsh, and fish shells
- On whether the fix works for other TUIs (Gemini, Codex): "It will work if they handle Esc+CR byte sequence as a newline."

## Date

Research conducted: 2026-02-22
