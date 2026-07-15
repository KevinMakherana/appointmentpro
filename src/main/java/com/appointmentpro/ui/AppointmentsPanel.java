package com.appointmentpro.ui;

import com.appointmentpro.dao.AppointmentDao;
import com.appointmentpro.model.AppointmentRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Displays all appointments in a sortable table, joined with
 * client, staff, and service names. Supports booking, status
 * updates, and deleting appointments.
 */
public class AppointmentsPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {
            "Client", "Staff", "Service", "Date", "Start", "End", "Status"
    };
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentDao appointmentDao = new AppointmentDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerPanel = new JPanel(centerLayout);
    private List<AppointmentRow> currentRows;

    public AppointmentsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JLabel emptyLabel = new JLabel("No appointments yet. Click \"New Appointment\" to book one.", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyLabel.setForeground(Color.GRAY);

        centerPanel.add(new JScrollPane(table), "TABLE");
        centerPanel.add(emptyLabel, "EMPTY");
        add(centerPanel, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);

        loadAppointments();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Appointments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newAppointmentButton = new JButton("New Appointment");
        newAppointmentButton.addActionListener(e -> openNewAppointmentDialog());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAppointments());
        buttonGroup.add(newAppointmentButton);
        buttonGroup.add(refreshButton);

        header.add(title, BorderLayout.WEST);
        header.add(buttonGroup, BorderLayout.EAST);
        return header;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton completeButton = new JButton("Mark Completed");
        completeButton.addActionListener(e -> updateSelectedStatus("Completed"));

        JButton noShowButton = new JButton("Mark No-Show");
        noShowButton.addActionListener(e -> updateSelectedStatus("No-Show"));

        JButton cancelButton = new JButton("Cancel Appointment");
        cancelButton.addActionListener(e -> updateSelectedStatus("Cancelled"));

        JButton deleteButton = new JButton("Delete Appointment");
        deleteButton.addActionListener(e -> deleteSelectedAppointment());

        footer.add(completeButton);
        footer.add(noShowButton);
        footer.add(cancelButton);
        footer.add(deleteButton);
        return footer;
    }

    private void openNewAppointmentDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        AddAppointmentDialog dialog = new AddAppointmentDialog((Frame) owner);
        dialog.setVisible(true);

        if (dialog.isAppointmentAdded()) {
            loadAppointments();
        }
    }

    private void updateSelectedStatus(String newStatus) {
        AppointmentRow appointment = getSelectedAppointment();
        if (appointment == null) {
            return;
        }

        if (appointment.status().equals(newStatus)) {
            return;
        }

        appointmentDao.updateStatus(appointment.id(), newStatus);
        loadAppointments();
    }

    private void deleteSelectedAppointment() {
        AppointmentRow appointment = getSelectedAppointment();
        if (appointment == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete this appointment for " + appointment.clientName() + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            appointmentDao.delete(appointment.id());
            loadAppointments();
        }
    }

    private AppointmentRow getSelectedAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select an appointment first.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentRows.get(selectedRow);
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        currentRows = appointmentDao.findAll();

        for (AppointmentRow appointment : currentRows) {
            tableModel.addRow(new Object[]{
                    appointment.clientName(),
                    appointment.staffName(),
                    appointment.serviceName(),
                    appointment.date().format(DATE_FORMAT),
                    appointment.startTime().format(TIME_FORMAT),
                    appointment.endTime().format(TIME_FORMAT),
                    appointment.status()
            });
        }

        centerLayout.show(centerPanel, currentRows.isEmpty() ? "EMPTY" : "TABLE");
    }
}