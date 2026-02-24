package com.github.thewoolleyman.jetbrains.terminalkittykeys

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.terminal.frontend.view.TerminalView
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import com.jediterm.terminal.TtyConnector
import com.intellij.terminal.ui.TerminalWidget
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendShiftEnterActionTest {

    private lateinit var action: SendShiftEnterAction
    private lateinit var event: AnActionEvent
    private lateinit var presentation: Presentation

    @Before
    fun setUp() {
        action = SendShiftEnterAction()
        event = mockk(relaxed = true)
        presentation = Presentation()
        every { event.presentation } returns presentation
    }

    // --- update() tests ---

    @Test
    fun `update enables action when project is present`() {
        every { event.project } returns mockk()

        action.update(event)

        assertTrue(presentation.isEnabled)
    }

    @Test
    fun `update disables action when project is null`() {
        every { event.project } returns null

        action.update(event)

        assertTrue(!presentation.isEnabled)
    }

    // --- actionPerformed() tests: TerminalView path ---

    @Test
    fun `sends CSI u via TerminalView when available`() {
        val terminalView = mockk<TerminalView>()
        every { event.getData(TerminalView.DATA_KEY) } returns terminalView
        every { terminalView.sendText("\u001b[13;2u") } just runs

        action.actionPerformed(event)

        verify(exactly = 1) { terminalView.sendText("\u001b[13;2u") }
    }

    @Test
    fun `falls back to TtyConnector when TerminalView sendText throws`() {
        val terminalView = mockk<TerminalView>()
        every { event.getData(TerminalView.DATA_KEY) } returns terminalView
        every { terminalView.sendText(any()) } throws RuntimeException("sendText failed")

        val project = mockk<Project>()
        every { event.project } returns project

        val toolWindowManager = mockk<TerminalToolWindowManager>()
        val toolWindow = mockk<ToolWindow>()
        val contentManager = mockk<ContentManager>()
        val content = mockk<Content>()
        val widget = mockk<TerminalWidget>()
        val connector = mockk<TtyConnector>()

        mockkStatic(TerminalToolWindowManager::class) {
            every { TerminalToolWindowManager.getInstance(project) } returns toolWindowManager
            every { toolWindowManager.toolWindow } returns toolWindow
            every { toolWindow.contentManager } returns contentManager
            every { contentManager.selectedContent } returns content
            every { TerminalToolWindowManager.findWidgetByContent(content) } returns widget
            every { widget.ttyConnector } returns connector
            every { connector.write("\u001b[13;2u") } just runs

            action.actionPerformed(event)

            verify(exactly = 1) { connector.write("\u001b[13;2u") }
        }
    }

    // --- actionPerformed() tests: TtyConnector fallback path ---

    @Test
    fun `sends CSI u via TtyConnector when TerminalView unavailable`() {
        every { event.getData(TerminalView.DATA_KEY) } returns null

        val project = mockk<Project>()
        every { event.project } returns project

        val toolWindowManager = mockk<TerminalToolWindowManager>()
        val toolWindow = mockk<ToolWindow>()
        val contentManager = mockk<ContentManager>()
        val content = mockk<Content>()
        val widget = mockk<TerminalWidget>()
        val connector = mockk<TtyConnector>()

        mockkStatic(TerminalToolWindowManager::class) {
            every { TerminalToolWindowManager.getInstance(project) } returns toolWindowManager
            every { toolWindowManager.toolWindow } returns toolWindow
            every { toolWindow.contentManager } returns contentManager
            every { contentManager.selectedContent } returns content
            every { TerminalToolWindowManager.findWidgetByContent(content) } returns widget
            every { widget.ttyConnector } returns connector
            every { connector.write("\u001b[13;2u") } just runs

            action.actionPerformed(event)

            verify(exactly = 1) { connector.write("\u001b[13;2u") }
        }
    }

    @Test
    fun `returns gracefully when project is null`() {
        every { event.getData(TerminalView.DATA_KEY) } returns null
        every { event.project } returns null

        action.actionPerformed(event) // should not throw
    }

    @Test
    fun `returns gracefully when tool window is null`() {
        every { event.getData(TerminalView.DATA_KEY) } returns null
        val project = mockk<Project>()
        every { event.project } returns project

        val toolWindowManager = mockk<TerminalToolWindowManager>()

        mockkStatic(TerminalToolWindowManager::class) {
            every { TerminalToolWindowManager.getInstance(project) } returns toolWindowManager
            every { toolWindowManager.toolWindow } returns null

            action.actionPerformed(event) // should not throw
        }
    }

    @Test
    fun `returns gracefully when selected content is null`() {
        every { event.getData(TerminalView.DATA_KEY) } returns null
        val project = mockk<Project>()
        every { event.project } returns project

        val toolWindowManager = mockk<TerminalToolWindowManager>()
        val toolWindow = mockk<ToolWindow>()
        val contentManager = mockk<ContentManager>()

        mockkStatic(TerminalToolWindowManager::class) {
            every { TerminalToolWindowManager.getInstance(project) } returns toolWindowManager
            every { toolWindowManager.toolWindow } returns toolWindow
            every { toolWindow.contentManager } returns contentManager
            every { contentManager.selectedContent } returns null

            action.actionPerformed(event) // should not throw
        }
    }

    @Test
    fun `returns gracefully when widget is null`() {
        every { event.getData(TerminalView.DATA_KEY) } returns null
        val project = mockk<Project>()
        every { event.project } returns project

        val toolWindowManager = mockk<TerminalToolWindowManager>()
        val toolWindow = mockk<ToolWindow>()
        val contentManager = mockk<ContentManager>()
        val content = mockk<Content>()

        mockkStatic(TerminalToolWindowManager::class) {
            every { TerminalToolWindowManager.getInstance(project) } returns toolWindowManager
            every { toolWindowManager.toolWindow } returns toolWindow
            every { toolWindow.contentManager } returns contentManager
            every { contentManager.selectedContent } returns content
            every { TerminalToolWindowManager.findWidgetByContent(content) } returns null

            action.actionPerformed(event) // should not throw
        }
    }

    @Test
    fun `returns gracefully when tty connector is null`() {
        every { event.getData(TerminalView.DATA_KEY) } returns null
        val project = mockk<Project>()
        every { event.project } returns project

        val toolWindowManager = mockk<TerminalToolWindowManager>()
        val toolWindow = mockk<ToolWindow>()
        val contentManager = mockk<ContentManager>()
        val content = mockk<Content>()
        val widget = mockk<TerminalWidget>()

        mockkStatic(TerminalToolWindowManager::class) {
            every { TerminalToolWindowManager.getInstance(project) } returns toolWindowManager
            every { toolWindowManager.toolWindow } returns toolWindow
            every { toolWindow.contentManager } returns contentManager
            every { contentManager.selectedContent } returns content
            every { TerminalToolWindowManager.findWidgetByContent(content) } returns widget
            every { widget.ttyConnector } returns null

            action.actionPerformed(event) // should not throw
        }
    }

    @Test
    fun `handles TtyConnector write exception gracefully`() {
        every { event.getData(TerminalView.DATA_KEY) } returns null
        val project = mockk<Project>()
        every { event.project } returns project

        val toolWindowManager = mockk<TerminalToolWindowManager>()
        val toolWindow = mockk<ToolWindow>()
        val contentManager = mockk<ContentManager>()
        val content = mockk<Content>()
        val widget = mockk<TerminalWidget>()
        val connector = mockk<TtyConnector>()

        mockkStatic(TerminalToolWindowManager::class) {
            every { TerminalToolWindowManager.getInstance(project) } returns toolWindowManager
            every { toolWindowManager.toolWindow } returns toolWindow
            every { toolWindow.contentManager } returns contentManager
            every { contentManager.selectedContent } returns content
            every { TerminalToolWindowManager.findWidgetByContent(content) } returns widget
            every { widget.ttyConnector } returns connector
            every { connector.write(any<String>()) } throws RuntimeException("write failed")

            action.actionPerformed(event) // should not throw
        }
    }

    // --- Constant verification ---

    @Test
    fun `CSI u sequence is correct ESC 13 2u`() {
        // ESC = 0x1B, followed by [13;2u
        val expected = "\u001b[13;2u"
        // Verify via the TerminalView path that the correct sequence is sent
        val terminalView = mockk<TerminalView>()
        every { event.getData(TerminalView.DATA_KEY) } returns terminalView
        every { terminalView.sendText(any()) } just runs

        action.actionPerformed(event)

        verify { terminalView.sendText(expected) }
        // Also verify the raw bytes
        val bytes = expected.toByteArray(Charsets.US_ASCII)
        assertEquals(0x1B, bytes[0].toInt())
        assertEquals('['.code, bytes[1].toInt())
        assertEquals('1'.code, bytes[2].toInt())
        assertEquals('3'.code, bytes[3].toInt())
        assertEquals(';'.code, bytes[4].toInt())
        assertEquals('2'.code, bytes[5].toInt())
        assertEquals('u'.code, bytes[6].toInt())
        assertEquals(7, bytes.size)
    }
}
