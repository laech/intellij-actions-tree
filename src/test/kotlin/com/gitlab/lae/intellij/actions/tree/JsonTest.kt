package com.gitlab.lae.intellij.actions.tree

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import javax.swing.KeyStroke.getKeyStroke

class JsonTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testDeserialization() {
        val expected = listOf(
                ActionGroup(getKeyStroke("ctrl C"), listOf(
                        ActionGroup(getKeyStroke("P"), listOf(
                                ActionRef(getKeyStroke("K"), "CloseProject", false),
                                ActionRef(getKeyStroke("P"), "OpenProjectGroup", true)
                        ))
                )),
                ActionGroup(getKeyStroke("ctrl X"), listOf(
                        ActionRef(getKeyStroke("ctrl X"), "EditorSwapSelectionBoundaries", false)
                ))
        )

        val actual = JsonTest::class.java.getResourceAsStream("test.json").use {
            parseJsonActions(it.reader())
        }

        assertEquals(expected, actual)
    }

}
