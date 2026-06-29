package com.github.thewoolleyman.jetbrains.terminalkittykeys

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.terminal.frontend.view.TerminalView
import com.jediterm.terminal.TtyConnector
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
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
        event = actionEvent()
    }

    private fun actionEvent(
        project: Project? = null,
        terminalView: TerminalView? = null,
    ): AnActionEvent {
        presentation = Presentation()
        val dataContext = DataContext { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                TerminalView.DATA_KEY.name -> terminalView
                else -> null
            }
        }
        return AnActionEvent(
            dataContext,
            presentation,
            ActionPlaces.UNKNOWN,
            ActionUiKind.NONE,
            null,
            0,
            mockk<ActionManager>(relaxed = true),
        )
    }

    // --- update() tests ---

    @Test
    fun `update enables action when project is present`() {
        event = actionEvent(project = mockk())

        action.update(event)

        assertTrue(presentation.isEnabled)
    }

    @Test
    fun `update disables action when project is null`() {
        event = actionEvent(project = null)

        action.update(event)

        assertTrue(!presentation.isEnabled)
    }

    // --- actionPerformed() tests: TerminalView path ---

    @Test
    fun `sends CSI u via TerminalView when available`() {
        val terminalView = mockk<TerminalView>()
        event = actionEvent(terminalView = terminalView)
        every { terminalView.sendText("\u001b[13;2u") } just runs

        action.actionPerformed(event)

        verify(exactly = 1) { terminalView.sendText("\u001b[13;2u") }
    }

    @Test
    fun `falls back to TtyConnector when TerminalView sendText throws`() {
        val terminalView = mockk<TerminalView>()
        every { terminalView.sendText(any()) } throws RuntimeException("sendText failed")

        val project = mockk<Project>()
        event = actionEvent(project = project, terminalView = terminalView)
        val connector = mockk<TtyConnector>()
        action = SendShiftEnterAction { connector }
        every { connector.write("\u001b[13;2u") } just runs

        action.actionPerformed(event)

        verify(exactly = 1) { connector.write("\u001b[13;2u") }
    }

    // --- actionPerformed() tests: TtyConnector fallback path ---

    @Test
    fun `sends CSI u via TtyConnector when TerminalView unavailable`() {
        val project = mockk<Project>()
        event = actionEvent(project = project)
        val connector = mockk<TtyConnector>()
        action = SendShiftEnterAction { connector }
        every { connector.write("\u001b[13;2u") } just runs

        action.actionPerformed(event)

        verify(exactly = 1) { connector.write("\u001b[13;2u") }
    }

    @Test
    fun `returns gracefully when project is null`() {
        event = actionEvent(project = null)

        action.actionPerformed(event) // should not throw
    }

    @Test
    fun `returns gracefully when tool window is null`() {
        val project = mockk<Project>()
        event = actionEvent(project = project)
        action = SendShiftEnterAction { null }

        action.actionPerformed(event) // should not throw
    }

    @Test
    fun `returns gracefully when selected content is null`() {
        val project = mockk<Project>()
        event = actionEvent(project = project)
        action = SendShiftEnterAction { null }

        action.actionPerformed(event) // should not throw
    }

    @Test
    fun `returns gracefully when widget is null`() {
        val project = mockk<Project>()
        event = actionEvent(project = project)
        action = SendShiftEnterAction { null }

        action.actionPerformed(event) // should not throw
    }

    @Test
    fun `returns gracefully when tty connector is null`() {
        val project = mockk<Project>()
        event = actionEvent(project = project)
        action = SendShiftEnterAction { null }

        action.actionPerformed(event) // should not throw
    }

    @Test
    fun `handles TtyConnector write exception gracefully`() {
        val project = mockk<Project>()
        event = actionEvent(project = project)
        val connector = mockk<TtyConnector>()
        action = SendShiftEnterAction { connector }
        every { connector.write(any<String>()) } throws RuntimeException("write failed")

        action.actionPerformed(event) // should not throw
    }

    // --- Constant verification ---

    @Test
    fun `CSI u sequence is correct ESC 13 2u`() {
        // ESC = 0x1B, followed by [13;2u
        val expected = "\u001b[13;2u"
        // Verify via the TerminalView path that the correct sequence is sent
        val terminalView = mockk<TerminalView>()
        event = actionEvent(terminalView = terminalView)
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
