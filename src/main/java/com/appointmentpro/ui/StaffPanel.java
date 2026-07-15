package com.appointmentpro.ui;

import com.appointmentpro.dao.DeleteBlockedException;
import com.appointmentpro.dao.StaffDao;
import com.appointmentpro.model.StaffRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Displays all staff members (active and inactive), with buttons
 * to add, deactivate/reactivate, and delete staff.
 */
public class StaffPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {"Name", "Role", "Phone", "Status"};

    private final StaffDao staffDao = new StaffDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<StaffRow> currentRows;

    public StaffPanel() {
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

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadStaff();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Staff");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton addButton = new JButton("Add Staff");
        addButton.addActionListener(e -> openAddStaffDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);
        return header;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton toggleButton = new JButton("Toggle Active Status");
        toggleButton.addActionListener(e -> toggleSelectedStaffStatus());

        JButton deleteButton = new JButton("Delete Staff");
        deleteButton.addActionListener(e -> deleteSelectedStaff());

        footer.add(toggleButton);
        footer.add(deleteButton);
        return footer;
    }

    private void openAddStaffDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        AddStaffDialog dialog = new AddStaffDialog((Frame) owner);
        dialog.setVisible(true);

        if (dialog.isStaffAdded()) {
            loadStaff();
        }
    }

    private void toggleSelectedStaffStatus() {
        StaffRow staff = getSelectedStaff();
        if (staff == null) {
            return;
        }

        staffDao.setActive(staff.id(), !staff.active());
        loadStaff();
    }

    private void deleteSelectedStaff() {
        StaffRow staff = getSelectedStaff();
        if (staff == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete " + staff.fullName() + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            staffDao.delete(staff.id());
            loadStaff();
        } catch (DeleteBlockedException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Can't delete staff member",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private StaffRow getSelectedStaff() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a staff member first.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentRows.get(selectedRow);
    }

    private void loadStaff() {
        tableModel.setRowCount(0);
        currentRows = staffDao.findAll();

        for (StaffRow staff : currentRows) {
            tableModel.addRow(new Object[]{
                    staff.fullName(),
                    staff.role(),
                    staff.phone() == null ? "—" : staff.phone(),
                    staff.active() ? "Active" : "Inactive"
            });
        }
    }
}