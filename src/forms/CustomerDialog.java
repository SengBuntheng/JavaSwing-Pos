package forms;

import globalValues.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerDialog extends JDialog {

    // UI Constants
    private static final Color COLOR_BG_MAIN = new Color(248, 249, 250);
    private static final Color COLOR_PRIMARY = new Color(13, 110, 253);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);

    private JTextField txtCustomerId, txtName, txtAddress, txtPhone;

    public CustomerDialog(Frame parent) {
        super(parent, "Add New Customer", true);
        setSize(450, 400);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout(15, 15));
        getRootPane().setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Create New Customer", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Customer ID:"), gbc);
        txtCustomerId = new JTextField(20);
        txtCustomerId.setFont(FONT_MAIN);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtCustomerId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        txtName = new JTextField(20);
        txtName.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Address:"), gbc);
        txtAddress = new JTextField(20);
        txtAddress.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        txtPhone = new JTextField(20);
        txtPhone.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(txtPhone, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnSave = createButton("Save Customer", COLOR_PRIMARY, e -> saveCustomer());
        JButton btnCancel = new JButton("Cancel");
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSave);
    }

    private void saveCustomer() {
        String id = txtCustomerId.getText().trim();
        String name = txtName.getText().trim();
        String address = txtAddress.getText().trim();
        String phone = txtPhone.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer ID and Name are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String sql = "INSERT INTO customer (customer_id, name, address, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, address.isEmpty() ? null : address);
            ps.setString(4, phone.isEmpty() ? null : phone);

            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Customer created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create customer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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