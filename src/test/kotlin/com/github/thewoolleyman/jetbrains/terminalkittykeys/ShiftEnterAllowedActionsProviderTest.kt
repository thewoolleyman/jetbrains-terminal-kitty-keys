package com.github.thewoolleyman.jetbrains.terminalkittykeys

import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftEnterAllowedActionsProviderTest {

    @Test
    fun `returns correct action ID`() {
        val provider = ShiftEnterAllowedActionsProvider()
        val actionIds = provider.getActionIds()
        assertEquals("TerminalKittyKeys.SendShiftEnter", actionIds.first())
    }

    @Test
    fun `returns exactly one action ID`() {
        val provider = ShiftEnterAllowedActionsProvider()
        val actionIds = provider.getActionIds()
        assertEquals(1, actionIds.size)
    }
}
