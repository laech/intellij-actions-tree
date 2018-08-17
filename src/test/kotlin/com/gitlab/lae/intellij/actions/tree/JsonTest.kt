package com.gitlab.lae.intellij.actions.tree

import com.google.gson.reflect.TypeToken
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import javax.swing.KeyStroke.getKeyStroke

class JsonTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testDeserialization() {
        val expected = listOf(
                ActionNode(getKeyStroke("ctrl C"), null, listOf(
                        ActionNode(getKeyStroke("P"), "Project", listOf(
                                ActionLeaf(getKeyStroke("K"), "CloseProject"),
                                ActionLeaf(getKeyStroke("P"), "OpenProjectGroup")
                        ))
                )),
                ActionNode(getKeyStroke("ctrl X"), "test", listOf(
                        ActionLeaf(getKeyStroke("ctrl X"), "EditorSwapSelectionBoundaries")
                ))
        )

        val actual = JsonTest::class.java.getResourceAsStream("test.json").use {
            gson.fromJson<List<ActionTree>>(it.reader(),
                    object : TypeToken<List<ActionTree>>() {}.type)
        }

        assertEquals(expected, actual)
    }

}
