package forms;

import globalValues.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteItemDialog extends JDialog {

    // UI Constants
    private static final Color COLOR_BG_MAIN = new Color(248, 249, 250);
    private static final Color COLOR_DANGER = new Color(220, 53, 69);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);

    private JTextField txtItemId;

    public DeleteItemDialog(Frame parent) {
        super(parent, "Delete Item", true);
        setSize(400, 220);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout(15, 15));
        getRootPane().setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Delete Item by Code", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Item Code:"), gbc);
        txtItemId = new JTextField(20);
        txtItemId.setFont(FONT_MAIN);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtItemId, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnDelete = createButton("Delete Item", COLOR_DANGER, e -> deleteItem());
        JButton btnCancel = new JButton("Cancel");
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnDelete);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnDelete);
    }

    private void deleteItem() {
        String itemCode = txtItemId.getText().trim();
        if (itemCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Item Code.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete item '" + itemCode + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM item WHERE item_code = ?";
                PreparedStatement ps = DBConnection.con.prepareStatement(sql);
                ps.setString(1, itemCode);
                int rowsDeleted = ps.executeUpdate();
                ps.close();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Item deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Item Code not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createButton(String text, Color bg, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setBackground(bg);
        button.setForeground(COLOR_TEXT_LIGHT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        return button;
    }
}

