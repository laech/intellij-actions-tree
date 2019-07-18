package com.gitlab.lae.intellij.actions.tree.util;

import javax.swing.*;
import java.util.AbstractList;
import java.util.List;
import java.util.stream.Stream;

public final class ListModels {
    private ListModels() {
    }

    public static <E> Stream<E> stream(ListModel<E> model) {
        return asList(model).stream();
    }

    private static <E> List<E> asList(ListModel<E> model) {
        return new AbstractList<E>() {
            @Override
            public int size() {
                return model.getSize();
            }

            @Override
            public E get(int index) {
                return model.getElementAt(index);
            }
        };
    }

}
