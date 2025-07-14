package forms;

import globalValues.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddItemDialog extends JDialog {

    // UI Constants
    private static final Color COLOR_BG_MAIN = new Color(248, 249, 250);
    private static final Color COLOR_PRIMARY = new Color(13, 110, 253);
    private static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);

    private JTextField tfItemCode;
    private JTextField tfDescription;
    private JTextField tfQty;
    private JTextField tfUnitPrice;

    public AddItemDialog(JFrame parent) {
        super(parent, "Add New Item", true);
        setSize(450, 350);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout(15, 15));
        getRootPane().setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Add New Item", SwingConstants.CENTER);
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
        tfItemCode = new JTextField(20);
        tfItemCode.setFont(FONT_MAIN);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(tfItemCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        tfDescription = new JTextField(20);
        tfDescription.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(tfDescription, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Initial Quantity:"), gbc);
        tfQty = new JTextField(20);
        tfQty.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(tfQty, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Unit Price:"), gbc);
        tfUnitPrice = new JTextField(20);
        tfUnitPrice.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(tfUnitPrice, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnAdd = createButton("Add Item", COLOR_PRIMARY, e -> addItem());
        JButton btnCancel = new JButton("Cancel");
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnAdd);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnAdd);
    }

    private void addItem() {
        String code = tfItemCode.getText().trim();
        String desc = tfDescription.getText().trim();
        String qtyText = tfQty.getText().trim();
        String priceText = tfUnitPrice.getText().trim();

        if (code.isEmpty() || desc.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            BigDecimal price = new BigDecimal(priceText);

            if (qty < 0 || price.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Quantity and Price must be positive.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO item (item_code, description, qty, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = DBConnection.con.prepareStatement(sql)) {
                ps.setString(1, code);
                ps.setString(2, desc);
                ps.setInt(3, qty);
                ps.setBigDecimal(4, price);
                int result = ps.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add item.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Quantity and Price.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
