package com.gitlab.lae.intellij.actions.tree

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import javax.swing.KeyStroke.getKeyStroke

class JsonTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testDeserialization() {
        val expected = listOf(
                ActionNode("ActionsTree1", "Unnamed", false, listOf(getKeyStroke("ctrl C")), listOf(
                        ActionNode("ActionsTree2", "Unnamed", false, listOf(getKeyStroke("P")), listOf(
                                ActionNode("CloseProject", "Unnamed", false,
                                        listOf(getKeyStroke("K"), getKeyStroke("ctrl K")), emptyList()),
                                ActionNode("OpenProjectGroup", "Unnamed", true,
                                        listOf(getKeyStroke("P")), emptyList())
                        ))
                )),
                ActionNode("ActionsTree3", "b", false, listOf(getKeyStroke("ctrl X")), listOf(
                        ActionNode("EditorSwapSelectionBoundaries", "Unnamed", false,
                                listOf(getKeyStroke("ctrl X")), emptyList())
                ))
        )

        val actual = JsonTest::class.java.getResourceAsStream("test.json").use {
            parseJsonActions(it.reader())
        }

        assertEquals(expected, actual)
    }

}
