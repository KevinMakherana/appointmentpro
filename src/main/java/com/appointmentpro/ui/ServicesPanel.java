package com.appointmentpro.ui;

import com.appointmentpro.dao.DeleteBlockedException;
import com.appointmentpro.dao.ServiceDao;
import com.appointmentpro.model.ServiceRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Displays all services with buttons to add and delete.
 */
public class ServicesPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {"Service", "Duration", "Price"};

    private final ServiceDao serviceDao = new ServiceDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<ServiceRow> currentRows;

    public ServicesPanel() {
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

        loadServices();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Services");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton addButton = new JButton("Add Service");
        addButton.addActionListener(e -> openAddServiceDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);
        return header;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton deleteButton = new JButton("Delete Service");
        deleteButton.addActionListener(e -> deleteSelectedService());

        footer.add(deleteButton);
        return footer;
    }

    private void openAddServiceDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        AddServiceDialog dialog = new AddServiceDialog((Frame) owner);
        dialog.setVisible(true);

        if (dialog.isServiceAdded()) {
            loadServices();
        }
    }

    private void deleteSelectedService() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a service first.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ServiceRow service = currentRows.get(selectedRow);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete \"" + service.name() + "\"?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceDao.delete(service.id());
            loadServices();
        } catch (DeleteBlockedException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Can't delete service",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadServices() {
        tableModel.setRowCount(0);
        currentRows = serviceDao.findAll();

        for (ServiceRow service : currentRows) {
            tableModel.addRow(new Object[]{
                    service.name(),
                    service.durationMinutes() + " min",
                    String.format("R%.2f", service.price())
            });
        }
    }
}