package com.gitlab.lae.intellij.actions.tree.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

final class JsonObjects {
    private JsonObjects() {
    }

    static String removeString(
            JsonObject object,
            String field,
            Supplier<String> defaultValue
    ) {
        JsonElement element = object.remove(field);
        return element != null
                ? element.getAsString()
                : defaultValue.get();
    }

    static boolean removeBoolean(
            JsonObject object,
            String field,
            Supplier<Boolean> defaultValue
    ) {
        JsonElement element = object.remove(field);
        return element != null
                ? element.getAsBoolean()
                : defaultValue.get();
    }

    static <T> List<T> removeArray(
            JsonObject object,
            String field,
            Function<? super JsonElement, ? extends T> mapper
    ) {
        JsonElement element = object.remove(field);
        return element == null
                ? emptyList()
                : stream(element.getAsJsonArray().spliterator(), false)
                        .map(mapper).collect(toList());
    }
}
