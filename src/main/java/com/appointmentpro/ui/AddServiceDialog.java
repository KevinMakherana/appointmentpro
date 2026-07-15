package com.appointmentpro.ui;

import com.appointmentpro.dao.ServiceDao;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for adding a new service.
 */
public class AddServiceDialog extends JDialog {

    private final JTextField nameField = new JTextField(18);
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 480, 5));
    private final JTextField priceField = new JTextField(18);

    private final ServiceDao serviceDao = new ServiceDao();
    private boolean serviceAdded = false;

    public AddServiceDialog(Frame owner) {
        super(owner, "Add Service", true);
        setLayout(new BorderLayout(0, 12));
        setResizable(false);

        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(6, 0, 6, 10);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(6, 0, 6, 0);

        String[] labels = {"Service Name *", "Duration (minutes) *", "Price (R) *"};
        JComponent[] fields = {nameField, durationSpinner, priceField};

        for (int i = 0; i < labels.length; i++) {
            labelConstraints.gridy = i;
            fieldConstraints.gridy = i;
            form.add(new JLabel(labels[i]), labelConstraints);
            form.add(fields[i], fieldConstraints);
        }

        return form;
    }

    private JComponent buildButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Save Service");
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        int durationMinutes = (int) durationSpinner.getValue();
        String priceText = priceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Service name and price are required.",
                    "Missing information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) {
                throw new NumberFormatException("Price cannot be negative.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid, non-negative price (e.g. 250.00).",
                    "Invalid price",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        serviceDao.insert(name, durationMinutes, price);
        serviceAdded = true;
        dispose();
    }

    public boolean isServiceAdded() {
        return serviceAdded;
    }
}