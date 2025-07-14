package forms;

import globalValues.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class CustomerSelectionDialog extends JDialog {
    private JTable customerTable;
    private DefaultTableModel customerTableModel;
    private String selectedCustomerId, selectedCustomerName;
    private boolean hasSelected = false;

    CustomerSelectionDialog(Frame owner) {
        super(owner, "Select a Customer", true);
        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search Field
        JTextField txtSearch = new JTextField("Search by ID or Name...");
        txtSearch.addActionListener(e -> loadCustomers(txtSearch.getText().trim()));
        add(txtSearch, BorderLayout.NORTH);

        // Customer Table
        customerTableModel = new DefaultTableModel(new Object[]{"Customer ID", "Name", "Phone"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(30);
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSelect = new JButton("Select Customer");
        btnSelect.addActionListener(e -> onSelect());
        getRootPane().setDefaultButton(btnSelect);
        buttonPanel.add(btnSelect);
        add(buttonPanel, BorderLayout.SOUTH);

        loadCustomers(""); // Initial load
    }

    private void onSelect() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedCustomerId = customerTableModel.getValueAt(selectedRow, 0).toString();
            selectedCustomerName = customerTableModel.getValueAt(selectedRow, 1).toString();
            hasSelected = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer from the list.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadCustomers(String filter) {
        customerTableModel.setRowCount(0);
        try {
            String sql = "SELECT customer_id, name, phone FROM customer";
            if (!filter.isEmpty() && !filter.equals("Search by ID or Name...")) {
                sql += " WHERE customer_id LIKE ? OR name LIKE ?";
            }
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            if (!filter.isEmpty() && !filter.equals("Search by ID or Name...")) {
                String searchTerm = "%" + filter + "%";
                ps.setString(1, searchTerm);
                ps.setString(2, searchTerm);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                customerTableModel.addRow(new Object[]{rs.getString("customer_id"), rs.getString("name"), rs.getString("phone")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean hasSelectedCustomer() { return hasSelected; }
    public String getSelectedCustomerId() { return selectedCustomerId; }
    public String getSelectedCustomerName() { return selectedCustomerName; }
}