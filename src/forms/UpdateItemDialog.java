package forms;

import globalValues.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateItemDialog extends JDialog {

    // UI Constants
    private static final Color COLOR_BG_MAIN = new Color(248, 249, 250);
    private static final Color COLOR_PRIMARY = new Color(13, 110, 253);
    private static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);

    private JTextField txtItemId, txtDescription, txtPrice;
    private JButton btnUpdate;

    public UpdateItemDialog(Frame parent) {
        super(parent, "Update Item", true);
        setSize(500, 350);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout(15, 15));
        getRootPane().setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Update Item Details", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Item ID with Load button
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Item Code:"), gbc);
        txtItemId = new JTextField(15);
        txtItemId.setFont(FONT_MAIN);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtItemId, gbc);
        JButton btnLoad = createButton("Load", COLOR_PRIMARY, e -> loadItem());
        gbc.gridx = 2; gbc.weightx = 0;
        formPanel.add(btnLoad, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        txtDescription = new JTextField(20);
        txtDescription.setFont(FONT_MAIN);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtDescription, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Unit Price:"), gbc);
        txtPrice = new JTextField(20);
        txtPrice.setFont(FONT_MAIN);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtPrice, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        btnUpdate = createButton("Update Item", COLOR_PRIMARY, e -> updateItem());
        JButton btnCancel = new JButton("Cancel");
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnUpdate);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initially disable fields until an item is loaded
        txtDescription.setEnabled(false);
        txtPrice.setEnabled(false);
        btnUpdate.setEnabled(false);

        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLoad);
    }

    private void loadItem() {
        String itemCode = txtItemId.getText().trim();
        if (itemCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Item Code to load.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String sql = "SELECT description, unit_price FROM item WHERE item_code = ?";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtDescription.setText(rs.getString("description"));
                txtPrice.setText(rs.getBigDecimal("unit_price").toPlainString());
                txtDescription.setEnabled(true);
                txtPrice.setEnabled(true);
                btnUpdate.setEnabled(true);
                getRootPane().setDefaultButton(btnUpdate);
            } else {
                JOptionPane.showMessageDialog(this, "Item Code not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
                txtDescription.setText("");
                txtPrice.setText("");
                txtDescription.setEnabled(false);
                txtPrice.setEnabled(false);
                btnUpdate.setEnabled(false);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItem() {
        String itemCode = txtItemId.getText().trim();
        String description = txtDescription.getText().trim();
        String priceText = txtPrice.getText().trim();

        if (description.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description and Price cannot be empty.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal price = new BigDecimal(priceText);
            String sql = "UPDATE item SET description = ?, unit_price = ? WHERE item_code = ?";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, description);
            ps.setBigDecimal(2, price);
            ps.setString(3, itemCode);

            int rowsUpdated = ps.executeUpdate();
            ps.close();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Item updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed. Item not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for price.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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