package com.gitlab.lae.intellij.actions.tree;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.text.JTextComponent;
import java.awt.*;
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

    static final When INPUT_FOCUSED = new When() {
        @Override
        public boolean test(DataContext context) {
            IdeFocusManager focusManager =
                    IdeFocusManager.findInstanceByContext(context);
            Component component = focusManager.getFocusOwner();
            return component instanceof JTextComponent &&
                    ((JTextComponent) component).isEditable();
        }

        @Override
        public String toString() {
            return "When.INPUT_FOCUSED";
        }
    };

    public static When parse(String input) {
        return input.startsWith("!")
                ? When.not(doParse(input.substring(1)))
                : doParse(input);
    }

    private static When doParse(String input) {
        if (input.equals("InputFocused")) {
            return INPUT_FOCUSED;
        }
        String[] parts = input.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid 'when' pattern: '" + input + "'");
        }
        String type = parts[0];
        String arg = parts[1];
        switch (type) {
            case "ToolWindowActive": return toolWindowActive(arg);
            case "ToolWindowTabActive": return toolWindowTabActive(arg);
            case "FileExtension": return fileExtension(arg);
        }
        throw new IllegalArgumentException(input);
    }

    public static When any(When... clauses) {
        return new AutoValue_When_Any(unmodifiableList(asList(clauses)));
    }

    public static When all(When... clauses) {
        return new AutoValue_When_All(unmodifiableList(asList(clauses)));
    }

    public static When toolWindowActive(String titleRegex) {
        return new AutoValue_When_ToolWindowActive(Pattern.compile(titleRegex));
    }

    public static When toolWindowTabActive(String tabTitleRegex) {
        return new AutoValue_When_ToolWindowTabActive(
                Pattern.compile(tabTitleRegex));
    }

    public static When fileExtension(String extRegex) {
        return new AutoValue_When_FileExtension(Pattern.compile(extRegex));
    }

    public static When not(When when) {
        return new AutoValue_When_Not(when);
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

    @AutoValue
    static abstract class Not extends When {
        abstract When when();

        @Override
        public boolean test(DataContext context) {
            return !when().test(context);
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
    static abstract class ToolWindowActive extends Regex {
        @Override
        String value(DataContext context) {
            ToolWindow window = context.getData(TOOL_WINDOW);
            return window != null && window.isActive()
                    ? window.getStripeTitle()
                    : null;
        }
    }

    @AutoValue
    static abstract class ToolWindowTabActive extends Regex {
        @Override
        String value(DataContext context) {
            ToolWindow window = context.getData(TOOL_WINDOW);
            return window != null && window.isActive()
                    ? window.getTitle()
                    : null;
        }
    }

    @AutoValue
    static abstract class FileExtension extends Regex {
        @Override
        String value(DataContext context) {
            VirtualFile file = context.getData(VIRTUAL_FILE);
            return file != null ? file.getExtension() : null;
        }
    }
}
