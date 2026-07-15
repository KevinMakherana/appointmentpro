package com.appointmentpro.ui;

import com.appointmentpro.dao.ClientDao;
import com.appointmentpro.model.ClientRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Displays all clients in a table, with buttons to add and delete.
 */
public class ClientsPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {"Name", "Phone", "Email"};

    private final ClientDao clientDao = new ClientDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerPanel = new JPanel(centerLayout);
    private List<ClientRow> currentRows;

    public ClientsPanel() {
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

        JLabel emptyLabel = new JLabel("No clients yet. Click \"Add Client\" to create one.", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyLabel.setForeground(Color.GRAY);

        centerPanel.add(new JScrollPane(table), "TABLE");
        centerPanel.add(emptyLabel, "EMPTY");
        add(centerPanel, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);

        loadClients();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Clients");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton addButton = new JButton("Add Client");
        addButton.addActionListener(e -> openAddClientDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);
        return header;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton deleteButton = new JButton("Delete Client");
        deleteButton.addActionListener(e -> deleteSelectedClient());

        footer.add(deleteButton);
        return footer;
    }

    private void openAddClientDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        AddClientDialog dialog = new AddClientDialog((Frame) owner);
        dialog.setVisible(true);

        if (dialog.isClientAdded()) {
            loadClients();
        }
    }

    private void deleteSelectedClient() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a client first.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClientRow client = currentRows.get(selectedRow);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete " + client.fullName() + "? This will also permanently delete their appointment history.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            clientDao.delete(client.id());
            loadClients();
        }
    }

    private void loadClients() {
        tableModel.setRowCount(0);
        currentRows = clientDao.findAll();

        for (ClientRow client : currentRows) {
            tableModel.addRow(new Object[]{
                    client.fullName(),
                    client.phone(),
                    client.email() == null ? "—" : client.email()
            });
        }

        centerLayout.show(centerPanel, currentRows.isEmpty() ? "EMPTY" : "TABLE");
    }
}
