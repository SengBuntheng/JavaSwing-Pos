package forms;

import globalValues.DBConnection;
import globalValues.Products; // Import the Products class

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class OrderItemsDialog extends JDialog {

    // --- UI Design Constants ---
    private static final Color COLOR_BG_MAIN = new Color(248, 249, 250);
    private static final Color COLOR_PRIMARY = new Color(1, 126, 81); // Updated color
    private static final Color COLOR_SUCCESS = new Color(25, 135, 84);
    private static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);

    // --- Components ---
    private JComboBox<Products> cbItems; // Changed to use Products object
    private JTextField txtQty;
    private JLabel lblUnitPrice;
    private JLabel lblExtPrice;
    private final MainPanel mainPanel; // Reference to the main panel

    public OrderItemsDialog(MainPanel parent) { // Removed unused userId parameter
        super(parent, "Add Item to Sale", true);
        this.mainPanel = parent;

        setSize(500, 350);
        setLocationRelativeTo(parent);

        initUI();
        loadItems();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(15, 20, 15, 20));

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Item Selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Select Item:"), gbc);
        cbItems = new JComboBox<>();
        cbItems.setFont(FONT_MAIN);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(cbItems, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Quantity:"), gbc);
        txtQty = new JTextField("1", 5);
        txtQty.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(txtQty, gbc);

        // Unit Price
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Unit Price:"), gbc);
        lblUnitPrice = new JLabel("$0.00");
        lblUnitPrice.setFont(FONT_MAIN);
        gbc.gridx = 1;
        formPanel.add(lblUnitPrice, gbc);

        // Extended Price
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Extended Price:"), gbc);
        lblExtPrice = new JLabel("$0.00");
        lblExtPrice.setFont(FONT_BOLD);
        gbc.gridx = 1;
        formPanel.add(lblExtPrice, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- Buttons Panel ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        JButton btnAddItem = createButton("Add to Sale", COLOR_SUCCESS, this::addItemToSale);
        JButton btnDone = createButton("Done", COLOR_PRIMARY, e -> dispose());

        buttonsPanel.add(btnAddItem);
        buttonsPanel.add(btnDone);
        add(buttonsPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        cbItems.addActionListener(e -> updatePrices());
        txtQty.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updatePrices();
            }
        });
        getRootPane().setDefaultButton(btnAddItem);
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

    private void loadItems() {
        try (PreparedStatement ps = DBConnection.con.prepareStatement("SELECT item_code, description, unit_price FROM item");
             ResultSet rs = ps.executeQuery()) {
            cbItems.removeAllItems();
            while (rs.next()) {
                // Create a full Products object
                Products product = new Products(
                        rs.getString("item_code"),
                        rs.getString("description"),
                        "", null, null, // Placeholder values for unused fields
                        rs.getBigDecimal("unit_price"),
                        0, false
                );
                cbItems.addItem(product);
            }
            if (cbItems.getItemCount() > 0) {
                cbItems.setSelectedIndex(0);
                updatePrices();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load items from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePrices() {
        Products selected = (Products) cbItems.getSelectedItem();
        if (selected == null) {
            lblUnitPrice.setText("$0.00");
            lblExtPrice.setText("$0.00");
            return;
        }

        DecimalFormat df = new DecimalFormat("$#,##0.00");
        lblUnitPrice.setText(df.format(selected.getPrice()));

        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText());
            if (qty < 1) qty = 1;
        } catch (NumberFormatException e) {
            qty = 1;
        }

        BigDecimal extPrice = selected.getPrice().multiply(new BigDecimal(qty));
        lblExtPrice.setText(df.format(extPrice));
    }

    private void addItemToSale(java.awt.event.ActionEvent e) {
        Products selected = (Products) cbItems.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // **FIX:** Call the correct method in the new MainPanel
        // The new method may need to be made public if it isn't already.
        // We will add the item 'qty' times.
        for (int i = 0; i < qty; i++) {
            mainPanel.addItemToCart(selected);
        }

        // Close dialog after adding
        dispose();
    }
}