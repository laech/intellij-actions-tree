package com.gitlab.lae.intellij.actions.tree

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import javax.swing.KeyStroke.getKeyStroke

class JsonTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testDeserialization() {
        val expected = listOf(
                ActionGroup(listOf(getKeyStroke("ctrl C")), listOf(
                        ActionGroup(listOf(getKeyStroke("P")), listOf(
                                ActionRef(listOf(
                                        getKeyStroke("K"),
                                        getKeyStroke("ctrl K")), "CloseProject", false),
                                ActionRef(listOf(getKeyStroke("P")), "OpenProjectGroup", true)
                        ))
                )),
                ActionGroup(listOf(getKeyStroke("ctrl X")), listOf(
                        ActionRef(listOf(getKeyStroke("ctrl X")), "EditorSwapSelectionBoundaries", false)
                ))
        )

        val actual = JsonTest::class.java.getResourceAsStream("test.json").use {
            parseJsonActions(it.reader())
        }

        assertEquals(expected, actual)
    }

}
