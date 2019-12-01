package com.gitlab.lae.intellij.actions.tree.json;

import com.gitlab.lae.intellij.actions.tree.ActionNode;
import com.gitlab.lae.intellij.actions.tree.When;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import static java.util.stream.StreamSupport.stream;

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
        When when = processWhen(o.remove("when"));

        List<KeyStroke> keys =
                removeArray(o, "keys", ActionNodeParser::toKeyStroke);

        List<ActionNode> items =
                removeArray(o, "items", it -> toActionNode(it, seq));

        if (!o.keySet().isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid elements: " + o.keySet());
        }
        return ActionNode.create(id, name, sep, sticky, when, keys, items);
    }

    private static When processWhen(JsonElement element) {
        if (element == null) {
            return When.ALWAYS;
        }

        if (element.isJsonPrimitive()) {
            return When.parse(element.getAsString());
        }

        JsonObject object = element.getAsJsonObject();
        if (object.keySet().isEmpty()) {
            throw new IllegalArgumentException("'when' object is empty");
        }

        if (object.keySet().size() != 1) {
            throw new IllegalArgumentException(
                    "'when' object must only have either 'or' or 'and' " +
                            "element: " + object);
        }

        JsonElement or = object.remove("or");
        if (or != null) {
            return When.or(processWhens(or.getAsJsonArray()));
        }

        JsonElement and = object.remove("and");
        if (and != null) {
            return When.and(processWhens(and.getAsJsonArray()));
        }

        throw new IllegalArgumentException(
                "'when' object must only have either 'or' or 'and' element: " +
                        object);
    }

    private static When[] processWhens(JsonArray clauses) {
        return stream(clauses.getAsJsonArray().spliterator(), false)
                .map(ActionNodeParser::processWhen)
                .toArray(When[]::new);
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
