package com.gitlab.lae.intellij.actions.tree;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

final class Json {
    private Json() {
    }

    private static final Gson gson = new Gson();

    static List<ActionNode> parseJsonActions(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return parseJsonActions(reader);
        }
    }

    static List<ActionNode> parseJsonActions(Reader reader) {
        JsonElement element = gson.fromJson(reader, JsonElement.class);
        return toActionNode(element, new AtomicInteger()::getAndIncrement).items();
    }

    private static String removeString(
            JsonObject element,
            String field,
            Supplier<String> defaultValue
    ) {
        return Optional.ofNullable(element.remove(field))
                .map(JsonElement::getAsString)
                .orElseGet(defaultValue);
    }

    private static boolean removeBoolean(
            JsonObject element,
            String field,
            Supplier<Boolean> defaultValue
    ) {
        return Optional.ofNullable(element.remove(field))
                .map(JsonElement::getAsBoolean)
                .orElseGet(defaultValue);
    }

    private static <T> List<T> removeArray(
            JsonObject element,
            String field,
            Function<? super JsonElement, ? extends T> mapper
    ) {
        return Optional.ofNullable(element.remove(field))
                .map(JsonElement::getAsJsonArray)
                .map(it -> StreamSupport.stream(it.spliterator(), false))
                .orElse(Stream.empty())
                .map(mapper)
                .collect(toList());
    }

    private static ActionNode toActionNode(JsonElement element, IntSupplier seq) {
        JsonObject o = element.getAsJsonObject();
        String id = removeString(o, "id", () -> "ActionsTree" + seq.getAsInt());
        String sep = removeString(o, "separator-above", () -> null);
        String name = removeString(o, "name", () -> "Unnamed");
        boolean sticky = removeBoolean(o, "sticky", () -> false);
        List<KeyStroke> keys = removeArray(o, "keys", Json::toKeyStroke);
        List<ActionNode> items = removeArray(o, "items", it -> toActionNode(it, seq));
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
