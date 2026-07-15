package com.appointmentpro.app;

import com.appointmentpro.db.DatabaseManager;
import com.appointmentpro.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;

/**
 * Entry point for the AppointmentPro application.
 */
public class AppointmentPro {

    public static void main(String[] args) {
        DatabaseManager.getInstance().initializeDatabase();

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}