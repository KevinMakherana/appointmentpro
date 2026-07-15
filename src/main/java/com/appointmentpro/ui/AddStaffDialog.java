package com.appointmentpro.ui;

import com.appointmentpro.dao.StaffDao;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for adding a new staff member.
 */
public class AddStaffDialog extends JDialog {

    private static final String[] ROLE_OPTIONS = {"Stylist", "Therapist", "Receptionist", "Manager", "Other"};

    private final JTextField firstNameField = new JTextField(18);
    private final JTextField lastNameField = new JTextField(18);
    private final JComboBox<String> roleCombo = new JComboBox<>(ROLE_OPTIONS);
    private final JTextField phoneField = new JTextField(18);

    private final StaffDao staffDao = new StaffDao();
    private boolean staffAdded = false;

    public AddStaffDialog(Frame owner) {
        super(owner, "Add Staff Member", true);
        setLayout(new BorderLayout(0, 12));
        setResizable(false);
        roleCombo.setEditable(true);

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

        String[] labels = {"First Name *", "Last Name *", "Role *", "Phone"};
        JComponent[] fields = {firstNameField, lastNameField, roleCombo, phoneField};

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

        JButton saveButton = new JButton("Save Staff Member");
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String role = roleCombo.getEditor().getItem().toString().trim();
        String phone = phoneField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First name, last name, and role are required.",
                    "Missing information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        staffDao.insert(firstName, lastName, role, phone.isEmpty() ? null : phone);
        staffAdded = true;
        dispose();
    }

    public boolean isStaffAdded() {
        return staffAdded;
    }
}