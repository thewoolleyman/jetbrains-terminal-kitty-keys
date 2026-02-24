# Remaining Issues Not Addressed by JetBrains Built-in Fix

Research into open and closed issues across JetBrains YouTrack, GitHub, and TUI tool repos where the JetBrains 2025.3.3+ built-in fix (ESC+CR) is insufficient and the Terminal Kitty Keys plugin (kitty CSI u / `ESC[13;2u`) would help.

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

### GO-19305 — "Make Shift+Enter insert a new line in Claude Code" (Fixed as duplicate of IJPL-221848)

https://youtrack.jetbrains.com/issue/GO-19305

GoLand-specific duplicate. Same thread and findings as IJPL-221848.

## GitHub — anthropics/claude-code

### #4796 — "JetBrains Plugin Doesn't Support Shift+Enter" (Closed as duplicate)

https://github.com/anthropics/claude-code/issues/4796

Closed as duplicate, pointing to #5114, #1280, and #1262.

- **@DanielMenke**: "Annoying, but alt + Enter actually works" — confirming Alt+Enter as a workaround, but not a proper fix.

### #5114 — "WebStorm: Shift+Enter Shortcut Not Working in Reworked Terminal" (Closed)

https://github.com/anthropics/claude-code/issues/5114

- **@DanielMenke**: "This is likely caused by 'Use Option as Meta key' in Preferences | Tools | Terminal on Macs"
- **@dancherb**: "Also experiencing this on Windows 11, only on Webstorm though (not on VSCode)"

### #1262 — "Shift+Enter Does Not Insert New Line in WSL on Windows 10" (Still OPEN)

https://github.com/anthropics/claude-code/issues/1262

Large comment thread with extensive workaround attempts, none satisfactory:

- **@StephenBadger**: "Same issue, very annoying. Makes it hard to talk to Claude."
- **@isi2010**: "But this isn't the solution. In every work you are using shift + enter for a new line and this is for years like this. Really hard to train yourself to use something else for a new line."
- **@pupeno**: Tried VSCode keybinding hacks, discovered Claude Code writes keybindings inside WSL instead of on the Windows host.
- **@gusbicalho**: Reported leftover `\` character when using backslash-enter workaround keybindings.
- **@JuliaBonita**: "In WSL2, something completely blocks all attempts to modify the key bindings using `bind` or any other system tools."
- **@vitmantug**: "That will add a backslash at the end of every line."

### #1280 — "Multiline Input Broken in PyCharm Claude Plugin Code Editor" (Closed)

https://github.com/anthropics/claude-code/issues/1280

- **@tim-watcha**: "I'm using classic engine" — indicating the issue existed on Classic engine for some users too.

## GitHub — google-gemini/gemini-cli

### #849 — "shift-enter should do an enter but keep user control" (Closed)

https://github.com/google-gemini/gemini-cli/issues/849

- **@mark-99**: "Please make shift-enter work, every other UI works that way (Claude Code in particular), too much muscle-memory to have to remember a different key combination just for gemini."
- **@justinfagnani**: "The first thing I did after installing and trying out Gemini CLI for the first time is look for this issue... There HAS to be a way to get this to work."
- **@gaaclarke** (Google): "Getting shift+enter is a big deal with ncurses... from a UX perspective it's still problematic since it's nonstandard behavior."

### #13431 — "Support enabling modifyOtherKeys keyboard mode" (Closed/fixed)

https://github.com/google-gemini/gemini-cli/issues/13431

Gemini CLI added modifyOtherKeys support separately.

- **@jacob314** (Google contributor): "One thing we'll want to watch out for is that terminals that support the kitty protocol should continue to use it rather than the modifyOtherKeys functionality."

### #4161 — "shift+enter should give a newline" (Closed)

https://github.com/google-gemini/gemini-cli/issues/4161

### #15139 — "Shift enter support on windows" (Open)

https://github.com/google-gemini/gemini-cli/issues/15139

Windows-specific — Shift+Enter fails entirely.

### Other duplicates: #12812, #2668, #15282, #16916, #2532

## GitHub — openai/codex

### #4218 — "Regression: Shift+Enter sends prompt instead of inserting line break (macOS)" (OPEN)

https://github.com/openai/codex/issues/4218

Reports that Shift+Enter stopped working in v0.41.0 after previously working.

### #8603 — "Improve workflow with optional 'ctrl + enter' submission and jump a line with 'shift + enter'" (OPEN)

https://github.com/openai/codex/issues/8603

### #11559 — "Plan Question UI: Shift+Enter in 'None of the above' notes submits instead of inserting newline" (OPEN)

https://github.com/openai/codex/issues/11559

### Other duplicates: #8673, #2358

## GitHub — anomalyco/opencode

### #4046 — "Shift-return does not input newline since 1.0" (OPEN)

https://github.com/anomalyco/opencode/issues/4046

Works in Ghostty normally but not since OpenCode 1.0.

### #11983 — "Keyboard keybinds configuration not working - input_newline: shift+enter ignored" (OPEN)

https://github.com/anomalyco/opencode/issues/11983

Extremely detailed bug report showing Shift+Enter submits instead of newline across Google Antigravity, Warp Terminal, and Windows Terminal, even with explicit configuration.

### Other open issues: #8038, #10877, #7248

## Summary: Why the Plugin Is Still Needed

### 1. TUI apps that only support kitty CSI u, not ESC+CR

OpenCode, supercoder, and dexto have open issues where Shift+Enter does not work. The JetBrains ESC+CR fix does not help because these apps need the kitty keyboard protocol sequence (`ESC[13;2u`), not ESC+CR.

### 2. Shift+Enter vs Alt+Enter aliasing

The JetBrains fix makes Shift+Enter and Alt+Enter send identical bytes (`\x1b\r`). Any TUI that uses both keys for different actions (e.g., Neovim `<S-CR>` vs `<M-CR>` mappings) cannot distinguish them. The plugin sends `\x1b[13;2u` for Shift+Enter, keeping them distinct.

### 3. Users on older JetBrains versions (pre-2025.3.3)

The built-in fix is not available on 2025.2 and earlier. Multiple users reported issues on these versions.

### 4. The conditional nature of the JetBrains fix

JetBrains explicitly stated the fix only works "if they handle Esc+CR byte sequence as a newline." The kitty CSI u protocol is a standard that any kitty-protocol-aware TUI understands, whereas ESC+CR is an informal convention that each TUI must separately choose to support.

### 5. The kitty keyboard protocol is the industry direction

Native support exists in kitty, WezTerm, Ghostty, iTerm2, and Alacritty. A Google contributor on gemini-cli noted that terminals supporting the kitty protocol "should continue to use it rather than the modifyOtherKeys functionality."
