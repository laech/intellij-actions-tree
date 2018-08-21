package com.gitlab.lae.intellij.actions.tree

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import javax.swing.KeyStroke.getKeyStroke

class JsonTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testDeserialization() {
        val expected = listOf(
                ActionContainer(listOf(getKeyStroke("ctrl C")), null, listOf(
                        ActionContainer(listOf(getKeyStroke("P")), null, listOf(
                                ActionRef(listOf(
                                        getKeyStroke("K"),
                                        getKeyStroke("ctrl K")), "CloseProject"),
                                ActionRef(listOf(getKeyStroke("P")), "OpenProjectGroup")
                        ))
                )),
                ActionContainer(listOf(getKeyStroke("ctrl X")), "b", listOf(
                        ActionRef(listOf(getKeyStroke("ctrl X")), "EditorSwapSelectionBoundaries")
                ))
        )

        val actual = JsonTest::class.java.getResourceAsStream("test.json").use {
            parseJsonActions(it.reader())
        }

        assertEquals(expected, actual)
    }

}
