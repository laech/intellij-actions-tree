package com.gitlab.lae.intellij.actions.tree;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern PATTERN =
            Pattern.compile("" +
                    "^" +
                    "(?<type>[^(]+)" +
                    "\\(" +
                    "(?<arg>[^)]+)" +
                    "\\)" +
                    "$"
            );

    public static When parse(String input) {
        Matcher matcher = PATTERN.matcher(input);
        if (matcher.matches()) {
            String type = matcher.group("type");
            String arg = matcher.group("arg");
            switch (type) {
                case "ToolWindow": return toolWindow(arg);
                case "FileExt": return fileExt(arg);
            }
        }
        throw new IllegalArgumentException(input);
    }

    public static When or(When... clauses) {
        return new AutoValue_When_Or(unmodifiableList(asList(clauses)));
    }

    public static When and(When... clauses) {
        return new AutoValue_When_And(unmodifiableList(asList(clauses)));
    }

    public static When toolWindow(String title) {
        return new AutoValue_When_ToolWindow(title);
    }

    public static When fileExt(String ext) {
        return new AutoValue_When_ToolWindow(ext);
    }

    @AutoValue
    static abstract class Or extends When {
        abstract List<When> clauses();

        @Override
        public boolean test(DataContext context) {
            return clauses().stream().anyMatch(p -> p.test(context));
        }
    }

    @AutoValue
    static abstract class And extends When {
        abstract List<When> clauses();

        @Override
        public boolean test(DataContext context) {
            return clauses().stream().allMatch(p -> p.test(context));
        }
    }

    @AutoValue
    static abstract class ToolWindow extends When {
        abstract String title();

        @Override
        public boolean test(DataContext context) {
            com.intellij.openapi.wm.ToolWindow window =
                    context.getData(TOOL_WINDOW);
            return window != null && title().equals(window.getTitle());
        }
    }

    @AutoValue
    static abstract class FileExt extends When {
        abstract String ext();

        @Override
        public boolean test(DataContext context) {
            VirtualFile file = context.getData(PlatformDataKeys.VIRTUAL_FILE);
            return file != null && ext().equals(file.getExtension());
        }
    }
}
