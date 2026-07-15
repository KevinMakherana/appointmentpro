package com.appointmentpro.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The main application window: a sidebar for navigation and a
 * content area that swaps between the different sections.
 */
public class MainFrame extends JFrame {

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);

    public MainFrame() {
        setTitle("Jacaranda Salon & Wellness — Appointment Booking System");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(new AppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(new ClientsPanel(), "CLIENTS");
        contentPanel.add(new StaffPanel(), "STAFF");
        contentPanel.add(new ServicesPanel(), "SERVICES");
        contentLayout.show(contentPanel, "APPOINTMENTS");
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(33, 37, 41));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel storeName = new JLabel("<html><div style='text-align:center;'>Jacaranda<br>Salon &amp; Wellness</div></html>", SwingConstants.CENTER);
        storeName.setForeground(Color.WHITE);
        storeName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        storeName.setAlignmentX(Component.CENTER_ALIGNMENT);
        storeName.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 10));
        storeName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("Appointment Booking");
        subtitle.setForeground(new Color(173, 181, 189));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        JButton appointmentsButton = createNavButton("Appointments");
        appointmentsButton.addActionListener(e -> contentLayout.show(contentPanel, "APPOINTMENTS"));

        JButton clientsButton = createNavButton("Clients");
        clientsButton.addActionListener(e -> contentLayout.show(contentPanel, "CLIENTS"));

        JButton staffButton = createNavButton("Staff");
        staffButton.addActionListener(e -> contentLayout.show(contentPanel, "STAFF"));

        JButton servicesButton = createNavButton("Services");
        servicesButton.addActionListener(e -> contentLayout.show(contentPanel, "SERVICES"));

        sidebar.add(storeName);
        sidebar.add(subtitle);
        sidebar.add(appointmentsButton);
        sidebar.add(clientsButton);
        sidebar.add(staffButton);
        sidebar.add(servicesButton);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        button.setBackground(new Color(52, 58, 64));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return button;
    }
}