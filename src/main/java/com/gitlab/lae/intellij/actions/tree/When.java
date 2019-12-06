package com.gitlab.lae.intellij.actions.tree;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE;
import static com.intellij.openapi.actionSystem.PlatformDataKeys.TOOL_WINDOW;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public abstract class When implements Predicate<DataContext> {
    When() {
    }

    public static final When ALWAYS = new When() {
        @Override
        public boolean test(DataContext context) {
            return true;
        }

        @Override
        public String toString() {
            return "When.ALWAYS";
        }
    };

    public static final When NEVER = new When() {
        @Override
        public boolean test(DataContext context) {
            return false;
        }

        @Override
        public String toString() {
            return "When.NEVER";
        }
    };

    public static When parse(String input) {
        String[] parts = input.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid 'when' pattern: " + input);
        }
        String type = parts[0];
        String arg = parts[1];
        switch (type) {
            case "ToolWindow": return toolWindow(arg);
            case "FileExt": return fileExt(arg);
        }
        throw new IllegalArgumentException(input);
    }

    public static When any(When... clauses) {
        return new AutoValue_When_Any(unmodifiableList(asList(clauses)));
    }

    public static When all(When... clauses) {
        return new AutoValue_When_All(unmodifiableList(asList(clauses)));
    }

    public static When toolWindow(String titleRegex) {
        return new AutoValue_When_ToolWindow(Pattern.compile(titleRegex));
    }

    public static When fileExt(String extRegex) {
        return new AutoValue_When_FileExt(Pattern.compile(extRegex));
    }

    @AutoValue
    static abstract class Any extends When {
        abstract List<When> clauses();

        @Override
        public boolean test(DataContext context) {
            return clauses().stream().anyMatch(p -> p.test(context));
        }
    }

    @AutoValue
    static abstract class All extends When {
        abstract List<When> clauses();

        @Override
        public boolean test(DataContext context) {
            return clauses().stream().allMatch(p -> p.test(context));
        }
    }

    static abstract class Regex extends When {

        abstract Pattern regex();

        abstract String value(DataContext context);

        @Override
        public boolean test(DataContext context) {
            String value = value(context);
            if (value == null) {
                return false;
            }
            return regex().matcher(value).matches();
        }

        @Override
        public int hashCode() {
            return regex().pattern().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            return regex().pattern().equals(((Regex) obj).regex().pattern());
        }
    }

    @AutoValue
    static abstract class ToolWindow extends Regex {
        @Override
        String value(DataContext context) {
            com.intellij.openapi.wm.ToolWindow window =
                    context.getData(TOOL_WINDOW);
            return window != null ? window.getStripeTitle() : null;
        }
    }

    @AutoValue
    static abstract class FileExt extends Regex {
        @Override
        String value(DataContext context) {
            VirtualFile file = context.getData(VIRTUAL_FILE);
            return file != null ? file.getExtension() : null;
        }
    }
}
