package com.gitlab.lae.intellij.actions.tree;

import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.SeparatorWithText;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class ActionPresentationRenderer
        implements ListCellRenderer<ActionPresentation> {

    private final SeparatorWithText separator = new SeparatorWithText();
    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel content = new JPanel(new BorderLayout(30, 0));
    private final JLabel nameLabel = new JLabel();

    private final List<KeyStrokeLabel> keyLabels = new ArrayList<>();
    private final JPanel keyLabelsPanel = new JPanel(
            new FlowLayout(FlowLayout.TRAILING, 0, 0));

    private boolean emptyIconInit;
    private EmptyIcon emptyIcon;

    {
        nameLabel.setBackground(null);
        keyLabelsPanel.setBackground(null);

        content.add(nameLabel, BorderLayout.CENTER);
        content.add(keyLabelsPanel, BorderLayout.LINE_END);
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

        while (keyLabels.size() < value.keys().size()) {
            keyLabels.add(new KeyStrokeLabel());
        }

        setColors(list, isSelected);

        Presentation p = value.presentation();
        nameLabel.setEnabled(p.isEnabled());
        nameLabel.setText(p.getText());
        nameLabel.setDisabledIcon(p.getDisabledIcon() != null
                ? p.getDisabledIcon()
                : emptyIcon);

        Icon icon = isSelected ? p.getSelectedIcon() : p.getIcon();
        if (icon == null) icon = p.getIcon();
        if (icon == null) icon = emptyIcon;
        nameLabel.setIcon(icon);

        keyLabelsPanel.removeAll();
        for (int i = 0; i < value.keys().size(); i++) {
            KeyStrokeLabel label = keyLabels.get(keyLabels.size() - i - 1);
            label.setEnabled(p.isEnabled());
            label.setTextFromKeyStroke(value.keys().get(i));
            keyLabelsPanel.add(label.getComponent());
        }

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
            if (icon == null) icon = p.getDisabledIcon();
            if (icon == null) icon = p.getSelectedIcon();
            if (icon == null) continue;
            emptyIcon = EmptyIcon.create(
                    icon.getIconWidth(),
                    icon.getIconHeight());
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
            keyLabels.forEach(label -> label.setForeground(UIManager.getColor(
                    "MenuItem.acceleratorSelectionForeground")));
        } else {
            content.setBackground(list.getBackground());
            nameLabel.setForeground(list.getForeground());
            keyLabels.forEach(it -> it.setForeground(UIManager.getColor(
                    "MenuItem.acceleratorForeground")));
        }
    }

}
