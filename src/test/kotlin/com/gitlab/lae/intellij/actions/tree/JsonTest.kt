package com.gitlab.lae.intellij.actions.tree

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import javax.swing.KeyStroke.getKeyStroke

class JsonTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testDeserialization() {
        val expected = listOf(
                ActionNode("node0", null, false, listOf(getKeyStroke("ctrl C")), listOf(
                        ActionNode("node1", null, false, listOf(getKeyStroke("P")), listOf(
                                ActionNode("CloseProject", null, false,
                                        listOf(getKeyStroke("K"), getKeyStroke("ctrl K")), emptyList()),
                                ActionNode("OpenProjectGroup", null, true,
                                        listOf(getKeyStroke("P")), emptyList())
                        ))
                )),
                ActionNode("node2", "b", false, listOf(getKeyStroke("ctrl X")), listOf(
                        ActionNode("EditorSwapSelectionBoundaries", null, false,
                                listOf(getKeyStroke("ctrl X")), emptyList())
                ))
        )

        val actual = JsonTest::class.java.getResourceAsStream("test.json").use {
            parseJsonActions(it.reader())
        }

        assertEquals(expected, actual)
    }

}
