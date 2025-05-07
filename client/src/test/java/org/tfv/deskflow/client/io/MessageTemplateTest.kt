package org.tfv.deskflow.client.io

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
class MessageTemplateTest {
    @Test
    fun testSpecifiers() {

        MessageTemplate.DKeyDown.let {
            assertTrue(it.specifiers.size == 3)
            assertTrue(it.specifiers.all { s -> s.size == 2  })
        }

        MessageTemplate.CClipboard.let {
            assertTrue(it.specifiers.size == 2)
            assertTrue(it.specifiers[0].size == 1)
            assertTrue(it.specifiers[1].size == 4)
        }

    }

}