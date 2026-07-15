package com.appointmentpro.ui;

import com.appointmentpro.dao.AppointmentDao;
import com.appointmentpro.dao.ClientDao;
import com.appointmentpro.dao.ServiceDao;
import com.appointmentpro.dao.StaffDao;
import com.appointmentpro.model.ClientRow;
import com.appointmentpro.model.ServiceRow;
import com.appointmentpro.model.StaffRow;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Modal dialog for booking a new appointment. Auto-calculates the
 * end time from the selected service's duration and blocks saving
 * if the chosen staff member already has an overlapping appointment.
 */
public class AddAppointmentDialog extends JDialog {

    private final JComboBox<ClientRow> clientCombo = new JComboBox<>();
    private final JComboBox<StaffRow> staffCombo = new JComboBox<>();
    private final JComboBox<ServiceRow> serviceCombo = new JComboBox<>();
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
    private final JLabel endTimeLabel = new JLabel();

    private final ClientDao clientDao = new ClientDao();
    private final StaffDao staffDao = new StaffDao();
    private final ServiceDao serviceDao = new ServiceDao();
    private final AppointmentDao appointmentDao = new AppointmentDao();

    private boolean appointmentAdded = false;

    public AddAppointmentDialog(Frame owner) {
        super(owner, "New Appointment", true);
        setLayout(new BorderLayout(0, 12));
        setResizable(false);

        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd MMM yyyy"));
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

        loadComboData();

        serviceCombo.addActionListener(e -> updateEndTimeLabel());
        timeSpinner.addChangeListener(e -> updateEndTimeLabel());
        updateEndTimeLabel();

        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void loadComboData() {
        for (ClientRow client : clientDao.findAll()) {
            clientCombo.addItem(client);
        }
        for (StaffRow staff : staffDao.findActive()) {
            staffCombo.addItem(staff);
        }
        for (ServiceRow service : serviceDao.findAll()) {
            serviceCombo.addItem(service);
        }
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

        String[] labels = {"Client *", "Staff *", "Service *", "Date *", "Start Time *", "End Time"};
        JComponent[] fields = {clientCombo, staffCombo, serviceCombo, dateSpinner, timeSpinner, endTimeLabel};

        for (int i = 0; i < labels.length; i++) {
            labelConstraints.gridy = i;
            fieldConstraints.gridy = i;
            form.add(new JLabel(labels[i]), labelConstraints);
            form.add(fields[i], fieldConstraints);
        }

        clientCombo.setPreferredSize(new Dimension(220, clientCombo.getPreferredSize().height));
        return form;
    }

    private JComponent buildButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Book Appointment");
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private LocalTime extractTime(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private LocalDate extractDate(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void updateEndTimeLabel() {
        ServiceRow selectedService = (ServiceRow) serviceCombo.getSelectedItem();
        if (selectedService == null) {
            endTimeLabel.setText("—");
            return;
        }
        LocalTime endTime = extractTime(timeSpinner).plusMinutes(selectedService.durationMinutes());
        endTimeLabel.setText(endTime.toString());
    }

    private void handleSave() {
        ClientRow selectedClient = (ClientRow) clientCombo.getSelectedItem();
        StaffRow selectedStaff = (StaffRow) staffCombo.getSelectedItem();
        ServiceRow selectedService = (ServiceRow) serviceCombo.getSelectedItem();

        if (selectedClient == null || selectedStaff == null || selectedService == null) {
            JOptionPane.showMessageDialog(this,
                    "You need at least one client, staff member, and service before booking.",
                    "Missing data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date = extractDate(dateSpinner);
        LocalTime startTime = extractTime(timeSpinner);
        LocalTime endTime = startTime.plusMinutes(selectedService.durationMinutes());

        if (appointmentDao.hasConflict(selectedStaff.id(), date, startTime, endTime)) {
            JOptionPane.showMessageDialog(this,
                    selectedStaff.fullName() + " already has an appointment that overlaps this time slot.",
                    "Scheduling conflict",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        appointmentDao.insert(selectedClient.id(), selectedStaff.id(), selectedService.id(), date, startTime, endTime);
        appointmentAdded = true;
        dispose();
    }

    public boolean isAppointmentAdded() {
        return appointmentAdded;
    }
}