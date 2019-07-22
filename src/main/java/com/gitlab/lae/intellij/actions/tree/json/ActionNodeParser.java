package com.gitlab.lae.intellij.actions.tree.json;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import static com.gitlab.lae.intellij.actions.tree.json.JsonObjects.*;
import static java.nio.file.Files.newBufferedReader;

public final class ActionNodeParser {
    private ActionNodeParser() {
    }

    private static final Gson gson = new Gson();

    public static List<ActionNode> parseJsonActions(Path path)
            throws IOException {
        try (Reader reader = newBufferedReader(path)) {
            return parseJsonActions(reader);
        }
    }

    public static List<ActionNode> parseJsonActions(Reader reader) {
        JsonElement element = gson.fromJson(reader, JsonElement.class);
        ActionNode action = toActionNode(
                element,
                new AtomicInteger()::getAndIncrement
        );
        return action.items();
    }

    private static ActionNode toActionNode(
            JsonElement element,
            IntSupplier seq
    ) {
        JsonObject o = element.getAsJsonObject();
        String id = removeString(o, "id", () -> "ActionsTree" + seq.getAsInt());
        String sep = removeString(o, "separator-above", () -> null);
        String name = removeString(o, "name", () -> "Unnamed");
        boolean sticky = removeBoolean(o, "sticky", () -> false);

        List<KeyStroke> keys =
                removeArray(o, "keys", ActionNodeParser::toKeyStroke);

        List<ActionNode> items =
                removeArray(o, "items", it -> toActionNode(it, seq));

        if (!o.keySet().isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid elements: " + o.keySet());
        }
        return ActionNode.create(id, name, sep, sticky, keys, items);
    }

    private static KeyStroke toKeyStroke(JsonElement element) {
        KeyStroke key = KeyStroke.getKeyStroke(element.getAsString());
        if (key == null) {
            throw new IllegalArgumentException(
                    "Invalid key stroke: " + element.getAsString());
        }
        return key;
    }
}
