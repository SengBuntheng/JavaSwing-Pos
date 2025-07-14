package forms;

import globalValues.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerOrderDetailDialog extends JDialog {

    private final String customerId;
    private JLabel lblCustomerName, lblTotalItems;
    private JTable tblOrderDetails;
    private DefaultTableModel tableModel;

    public CustomerOrderDetailDialog(JFrame parent, String customerId) {
        super(parent, "Order Details for Customer: " + customerId, true);
        this.customerId = customerId;

        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        loadCustomerName();
        loadOrderDetails();
        calculateTotalItems();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        lblCustomerName = new JLabel("Customer: ");
        lblCustomerName.setFont(new Font("SansSerif", Font.BOLD, 18));

        lblTotalItems = new JLabel("Total Items Bought: 0");
        lblTotalItems.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblTotalItems.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(lblCustomerName, BorderLayout.WEST);
        headerPanel.add(lblTotalItems, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table for order details
        String[] columns = {"Order ID", "Item Code", "Item Description", "Quantity", "Unit Price", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Make quantity editable for update, others no
                return col == 3;
            }
        };
        tblOrderDetails = new JTable(tableModel);
        tblOrderDetails.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblOrderDetails.setRowHeight(28);

        JScrollPane scrollPane = new JScrollPane(tblOrderDetails);
        scrollPane.setBorder(new EmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton btnUpdate = new JButton("Update Quantity");
        JButton btnDelete = new JButton("Delete Item");
        JButton btnPayment = new JButton("Make Payment");
        JButton btnClose = new JButton("Close");

        btnUpdate.addActionListener(e -> updateSelectedItem());
        btnDelete.addActionListener(e -> deleteSelectedItem());
        btnPayment.addActionListener(e -> makePayment());
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnPayment);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadCustomerName() {
        try {
            String sql = "SELECT name FROM customer WHERE customer_id = ?";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblCustomerName.setText("Customer: " + rs.getString("name") + " (ID: " + customerId + ")");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load customer name: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrderDetails() {
        try {
            String sql = """
                SELECT o.order_id, i.item_code, i.description AS item_description, oi.qty, oi.unit_price, o.date
                FROM customer_order co
                JOIN `order` o ON co.order_id = o.order_id
                JOIN order_item oi ON o.order_id = oi.order_id
                JOIN item i ON oi.item_code = i.item_code
                WHERE co.customer_id = ?
                ORDER BY o.date DESC
                """;

            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("order_id"),
                        rs.getString("item_code"),
                        rs.getString("item_description"),


                });
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load order details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateTotalItems() {
        int totalQuantity = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            totalQuantity += (int) tableModel.getValueAt(i, 3);
        }
        lblTotalItems.setText("Total Items Bought: " + totalQuantity);
    }

    private void updateSelectedItem() {
        int row = tblOrderDetails.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(row, 0);
        String itemCode = (String) tableModel.getValueAt(row, 1);
        Object quantityObj = tableModel.getValueAt(row, 3);

        int newQuantity;
        try {
            newQuantity = Integer.parseInt(quantityObj.toString());
            if (newQuantity <= 0) {
                throw new NumberFormatException("Quantity must be positive.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive integer for quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String sql = "UPDATE order_item SET qty = ? WHERE order_id = ? AND item_code = ?";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setInt(1, newQuantity);
            ps.setString(2, orderId);
            ps.setString(3, itemCode);

            int updated = ps.executeUpdate();
            ps.close();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Quantity updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                calculateTotalItems();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedItem() {
        int row = tblOrderDetails.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(row, 0);
        String itemCode = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item from the order?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String sql = "DELETE FROM order_item WHERE order_id = ? AND item_code = ?";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, orderId);
            ps.setString(2, itemCode);

            int deleted = ps.executeUpdate();
            ps.close();

            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, "Item deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadOrderDetails();
                calculateTotalItems();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makePayment() {
        double totalAmount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int quantity = (int) tableModel.getValueAt(i, 3);
            double price = (double) tableModel.getValueAt(i, 4);
            totalAmount += quantity * price;
        }


    }
}
