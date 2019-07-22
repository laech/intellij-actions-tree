package com.gitlab.lae.intellij.actions.tree.ui;

import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.SeparatorWithText;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static java.util.stream.Collectors.joining;

public final class ActionPresentationRenderer
        implements ListCellRenderer<ActionPresentation> {

    private final SeparatorWithText separator = new SeparatorWithText();
    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel content = new JPanel(new BorderLayout(30, 0));
    private final JLabel nameLabel = new JLabel();

    private final JLabel keyLabel = new JLabel();

    private boolean emptyIconInit;
    private EmptyIcon emptyIcon;

    {
        nameLabel.setBackground(null);

        content.add(nameLabel, BorderLayout.CENTER);
        content.add(keyLabel, BorderLayout.LINE_END);
        content.setBorder(new EmptyBorder(UIUtil.getListCellPadding()));

        root.setBackground(null);
        root.add(separator, BorderLayout.PAGE_START);
        root.add(content, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends ActionPresentation> list,
            ActionPresentation value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {

        initEmptyIcon(list);

        separator.setCaption(value.separatorAbove());
        separator.setVisible(value.separatorAbove() != null);

        setColors(list, isSelected);

        Presentation presentation = value.presentation();
        nameLabel.setEnabled(presentation.isEnabled());
        nameLabel.setText(presentation.getText());
        nameLabel.setDisabledIcon(presentation.getDisabledIcon() != null
                ? presentation.getDisabledIcon()
                : emptyIcon);

        Icon icon = isSelected
                ? presentation.getSelectedIcon()
                : presentation.getIcon();
        if (icon == null) {
            icon = presentation.getIcon();
        }
        if (icon == null) {
            icon = emptyIcon;
        }
        nameLabel.setIcon(icon);

        keyLabel.setEnabled(presentation.isEnabled());
        keyLabel.setText(value.keys()
                .stream()
                .map(KeyStrokeLabel::getKeyText)
                .collect(joining(", ")));

        return root;
    }

    private void initEmptyIcon(JList<? extends ActionPresentation> list) {
        if (emptyIconInit) {
            return;
        }
        emptyIconInit = true;
        for (int i = 0; i < list.getModel().getSize(); i++) {
            Presentation p = list.getModel().getElementAt(i).presentation();
            Icon icon = p.getIcon();
            if (icon == null) {
                icon = p.getDisabledIcon();
            }
            if (icon == null) {
                icon = p.getSelectedIcon();
            }
            if (icon == null) {
                continue;
            }
            emptyIcon = EmptyIcon.create(
                    icon.getIconWidth(),
                    icon.getIconHeight()
            );
            break;
        }
    }

    private void setColors(
            JList<? extends ActionPresentation> list,
            boolean isSelected
    ) {
        if (isSelected) {
            content.setBackground(list.getSelectionBackground());
            nameLabel.setForeground(list.getSelectionForeground());
            keyLabel.setForeground(UIManager.getColor(
                    "MenuItem.acceleratorSelectionForeground"));
        } else {
            content.setBackground(list.getBackground());
            nameLabel.setForeground(list.getForeground());
            keyLabel.setForeground(UIManager.getColor(
                    "MenuItem.acceleratorForeground"));
        }
    }

}
