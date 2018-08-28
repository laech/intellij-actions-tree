package com.gitlab.lae.intellij.actions.tree

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import javax.swing.KeyStroke.getKeyStroke

class JsonTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testDeserialization() {
        val expected = listOf(
                ActionNode("ActionsTree1", "Unnamed", null, listOf(getKeyStroke("ctrl C")), listOf(
                        ActionNode("ActionsTree2", "Unnamed", null, listOf(getKeyStroke("P")), listOf(
                                ActionNode("CloseProject", "Unnamed", null,
                                        listOf(getKeyStroke("K"), getKeyStroke("ctrl K")), emptyList()),
                                ActionNode("OpenProjectGroup", "Unnamed", "SEP",
                                        listOf(getKeyStroke("P")), emptyList())
                        ))
                )),
                ActionNode("ActionsTree3", "b", null, listOf(getKeyStroke("ctrl X")), listOf(
                        ActionNode("EditorSwapSelectionBoundaries", "Unnamed", null,
                                listOf(getKeyStroke("ctrl X")), emptyList())
                ))
        )

        val actual = JsonTest::class.java.getResourceAsStream("test.json").use {
            parseJsonActions(it.reader())
        }

        assertEquals(expected, actual)
    }

}
