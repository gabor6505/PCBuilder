package net.gabor6505.java.pcbuilder.elements;

import net.gabor6505.java.pcbuilder.components.ComponentManager;
import net.gabor6505.java.pcbuilder.components.StateChangeListener;
import net.gabor6505.java.pcbuilder.types.TypeManager;
import net.gabor6505.java.pcbuilder.utils.Utils;
import sun.awt.PeerEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparisonPane extends ScrollPane2D implements ActionListener, StateChangeListener {

    private final Map<String, Integer> categoryIndexMap = new HashMap<>();

    private final JPanel mainPanel;
    private final JPanel headerPanel;

    public ComparisonPane(int windowWidth, int windowHeight, JFrame frame) {
        super(windowWidth, windowHeight);

        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setBackground(Color.DARK_GRAY);
        headerPanel.setBorder(BorderFactory.createMatteBorder(8, 4, 8, 4, Color.DARK_GRAY));

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(this);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        EventQueue.invokeLater(() -> {
            JButton btn = new JButton("Reload");
            btn.addActionListener(e -> {
                TypeManager.reload();
                ComponentManager.reload();
            });
            mainPanel.add(btn, BorderLayout.SOUTH);
        });

        if (frame != null) frame.setContentPane(mainPanel);
        ComponentManager.addStateChangeListener(this);
    }

    public ComparisonPane(int windowWidth, int windowHeight) {
        this(windowWidth, windowHeight, null);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public int addCategory(ComponentCategory category) {
        int index;
        if (!categoryIndexMap.containsKey(category.getDisplayName())) {
            index = addRow(category.getItemComponents(), category.getPreviewPanel());
            categoryIndexMap.put(category.getDisplayName(), index);

            JCheckBox checkBox = new JCheckBox(category.getDisplayName());
            checkBox.setSelected(true);
            checkBox.setForeground(Color.WHITE);
            checkBox.setBackground(Color.DARK_GRAY);
            checkBox.setBorder(BorderFactory.createMatteBorder(0, 8, 0, 8, Color.DARK_GRAY));
            checkBox.addActionListener(this);
            checkBox.setVisible(false);
            headerPanel.add(checkBox);

            EventQueue.invokeLater(() -> {
                checkBox.setVisible(true);
                //System.out.println(System.currentTimeMillis());
            });
        } else {
            index = categoryIndexMap.get(category.getDisplayName());
            clearRow(index);
            setPreviewPanel(index, category.getPreviewPanel());
            addComponents(index, category.getItemComponents());
        }
        headerPanel.revalidate();
        return index;
    }

    public void removeCategory(String categoryName) {
        if (categoryIndexMap.containsKey(categoryName)) {
            int index = categoryIndexMap.get(categoryName);
            removeRow(index);
            headerPanel.remove(findCheckBoxByName(categoryName));
            headerPanel.revalidate();

            categoryIndexMap.remove(categoryName);
        }
    }

    public void enableCategory(String categoryName) {
        EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        eq.postEvent(new PeerEvent(Toolkit.getDefaultToolkit(), () -> {
            JCheckBox cb = findCheckBoxByName(categoryName);
            if (cb == null) return;
            if (!cb.isSelected()) actionPerformed(new ActionEvent(cb, 0, cb.getText()));
            cb.setSelected(true);
        }, PeerEvent.LOW_PRIORITY_EVENT));
    }

    public void disableCategory(String categoryName) {
        EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        eq.postEvent(new PeerEvent(Toolkit.getDefaultToolkit(), () -> {
            JCheckBox cb = findCheckBoxByName(categoryName);
            if (cb == null) return;
            if (cb.isSelected()) actionPerformed(new ActionEvent(cb, 0, cb.getText()));
            cb.setSelected(false);
        }, PeerEvent.LOW_PRIORITY_EVENT));
    }

    private JCheckBox findCheckBoxByName(String categoryName) {
        for (Component component : headerPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (checkBox.getText().equals(categoryName)) return checkBox;
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = categoryIndexMap.get(e.getActionCommand());
        toggleRowVisible(index);
    }

    @Override
    public void loaded(String type, String displayName, List<net.gabor6505.java.pcbuilder.components.Component> affectedComponents) {
        addCategory(new ComponentCategory(type, displayName, affectedComponents));
    }

    @Override
    public void reloaded(String type, String displayName, List<net.gabor6505.java.pcbuilder.components.Component> affectedComponents) {
        addCategory(new ComponentCategory(type, displayName, affectedComponents));
    }

    @Override
    public void removed(String type) {
        removeCategory(type);
    }
}
