package com.appointmentpro.ui;

import com.appointmentpro.dao.ClientDao;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Modal dialog for adding a new client. Validates required fields
 * and email format before saving.
 */
public class AddClientDialog extends JDialog {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final JTextField firstNameField = new JTextField(18);
    private final JTextField lastNameField = new JTextField(18);
    private final JTextField phoneField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextArea notesArea = new JTextArea(3, 18);

    private final ClientDao clientDao = new ClientDao();
    private boolean clientAdded = false;

    public AddClientDialog(Frame owner) {
        super(owner, "Add Client", true);
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

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        String[] labels = {"First Name *", "Last Name *", "Phone *", "Email", "Notes"};
        JComponent[] fields = {firstNameField, lastNameField, phoneField, emailField, new JScrollPane(notesArea)};

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

        JButton saveButton = new JButton("Save Client");
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String notes = notesArea.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First name, last name, and phone are required.",
                    "Missing information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address, or leave it blank.",
                    "Invalid email",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        clientDao.insert(firstName, lastName, phone, email, notes);
        clientAdded = true;
        dispose();
    }

    public boolean isClientAdded() {
        return clientAdded;
    }
}