package forms;

import forms.LoginDialog;
import globalValues.DBConnection;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Application {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        DBConnection.connectDB();

        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.setVisible(true);
        });
    }
}