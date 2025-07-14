package forms;

import globalValues.DBConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Vector;

public class MainPanel extends JFrame {

    // --- Professional POS UI Design Constants ---
    private static final Color COLOR_BG_MAIN = new Color(248, 249, 250);
    private static final Color COLOR_BG_HEADER = new Color(33, 37, 41);
    private static final Color COLOR_BG_PANEL = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(13, 110, 253);
    private static final Color COLOR_SECONDARY = new Color(108, 117, 125);
    private static final Color COLOR_SUCCESS = new Color(25, 135, 84);
    private static final Color COLOR_DANGER = new Color(220, 53, 69);
    private static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(222, 226, 230);
    private static final Color COLOR_TOOLBAR_HOVER = new Color(233, 236, 239);

    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_HEADER = new Font("Segoe UI Semibold", Font.PLAIN, 24);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);

    // --- Member Variables ---
    private JTextField txtSearch;
    private final String userID;
    private final String userRole;
    private JLabel lblProfileImage;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;
    private JTable saleTable;
    private DefaultTableModel saleTableModel;
    private JLabel lblSubtotalValue;
    private JLabel lblTaxValue;
    private JLabel lblTotalValue;


    public MainPanel(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        setTitle("SU7.9 Professional POS — " + userID + " (" + userRole + ")");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        loadCustomers(); // Load initial customer list
        setVisible(true);
    }

    private void initUI() {
        // Set global rendering hints for smoother text and graphics
        UIManager.put("swing.aatext", true);
        UIManager.put("awt.useSystemAAFontSettings", "on");

        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(0.7); // 70% for left panel, 30% for right
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(COLOR_BG_HEADER);
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profilePanel.setOpaque(false);
        lblProfileImage = new JLabel();
        lblProfileImage.setPreferredSize(new Dimension(50, 50));
        loadUserProfilePicture();
        profilePanel.add(lblProfileImage);

        JLabel lblWelcome = new JLabel("Welcome, " + userID);
        lblWelcome.setFont(FONT_HEADER);
        lblWelcome.setForeground(COLOR_TEXT_LIGHT);
        profilePanel.add(lblWelcome);
        headerPanel.add(profilePanel, BorderLayout.WEST);

        JButton btnLogout = createHeaderButton("Logout", "https://img.icons8.com/fluency-systems-regular/24/ffffff/exit.png", this::onLogout);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(10, 15));
        leftPanel.setBorder(new EmptyBorder(15, 20, 20, 10));
        leftPanel.setBackground(COLOR_BG_MAIN);

        JPanel actionsPanel = new JPanel(new BorderLayout(0, 10));
        actionsPanel.setOpaque(false);
        actionsPanel.add(createSearchPanel(), BorderLayout.NORTH);
        actionsPanel.add(createToolbarPanel(), BorderLayout.CENTER);

        leftPanel.add(actionsPanel, BorderLayout.NORTH);
        leftPanel.add(createCustomerTablePanel(), BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 15));
        rightPanel.setBorder(new EmptyBorder(15, 10, 20, 20));
        rightPanel.setBackground(COLOR_BG_MAIN);

        JLabel lblTitle = new JLabel("Current Sale");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        lblTitle.setBorder(new EmptyBorder(0, 5, 5, 0));
        rightPanel.add(lblTitle, BorderLayout.NORTH);

        rightPanel.add(createSaleTablePanel(), BorderLayout.CENTER);
        rightPanel.add(createSaleActionsPanel(), BorderLayout.SOUTH);

        return rightPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        txtSearch = new JTextField("Search by Customer ID or Name...");
        txtSearch.setFont(FONT_MAIN);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, COLOR_BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        txtSearch.addActionListener(e -> searchCustomers());
        // Placeholder text functionality
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Search by Customer ID or Name...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(COLOR_TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText("Search by Customer ID or Name...");
                }
            }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        JButton btnSearch = createPrimaryButton("Search", "https://img.icons8.com/material-rounded/20/ffffff/search.png", e -> searchCustomers());
        searchPanel.add(btnSearch, BorderLayout.EAST);

        return searchPanel;
    }

    private JToolBar createToolbarPanel() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorder(new EmptyBorder(10, 0, 0, 0));
        toolbar.setOpaque(false);

        toolbar.add(createToolbarButton("New Customer", "https://img.icons8.com/fluency-systems-regular/24/212529/add-user-male.png", this::openNewCustomerDialog));
        toolbar.add(createToolbarButton("View All", "https://img.icons8.com/fluency-systems-regular/24/212529/user-group-man-man.png", this::onViewCustomers));
        toolbar.addSeparator(new Dimension(10,0));
        toolbar.add(createToolbarButton("Add to Sale", "https://img.icons8.com/fluency-systems-regular/24/212529/shopping-cart.png", this::onOrderItem));
        toolbar.add(createToolbarButton("View Orders", "https://img.icons8.com/fluency-systems-regular/24/212529/order-history.png", this::onCustomerOrderDetail));

        if (userRole.equalsIgnoreCase("Admin")) {
            toolbar.addSeparator(new Dimension(10,0));
            JButton adminMenuButton = createAdminMenuButton();
            toolbar.add(adminMenuButton);
        }

        return toolbar;
    }

    private JButton createAdminMenuButton() {
        JButton adminButton = createToolbarButton("Admin Tools", "https://img.icons8.com/fluency-systems-regular/24/212529/admin-settings-male.png", null);

        JPopupMenu adminPopupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add New Item", getIcon("https://img.icons8.com/fluency-systems-regular/20/212529/plus--v1.png"));
        addItem.addActionListener(this::onAddItem);
        adminPopupMenu.add(addItem);

        JMenuItem updateItem = new JMenuItem("Update Existing Item", getIcon("https://img.icons8.com/fluency-systems-regular/20/212529/edit--v1.png"));
        updateItem.addActionListener(this::onUpdateItem);
        adminPopupMenu.add(updateItem);

        JMenuItem deleteItem = new JMenuItem("Delete Item", getIcon("https://img.icons8.com/fluency-systems-regular/20/212529/delete-sign.png"));
        deleteItem.addActionListener(this::onDeleteItem);
        adminPopupMenu.add(deleteItem);

        adminPopupMenu.addSeparator();

        JMenuItem changePass = new JMenuItem("Change Password", getIcon("https://img.icons8.com/fluency-systems-regular/20/212529/lock--v1.png"));


        adminButton.addActionListener(e -> adminPopupMenu.show(adminButton, 0, adminButton.getHeight()));

        return adminButton;
    }

    private JScrollPane createCustomerTablePanel() {
        customerTableModel = new DefaultTableModel(new Object[]{"Customer ID", "Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        customerTable = new JTable(customerTableModel);
        styleTable(customerTable);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        return scrollPane;
    }

    private JScrollPane createSaleTablePanel() {
        // Add "Item Code" as a hidden first column for logic
        saleTableModel = new DefaultTableModel(new Object[]{"Item Code", "Item", "Qty", "Price", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        saleTable = new JTable(saleTableModel);
        styleTable(saleTable);

        // Hide the Item Code column from the user
        saleTable.getColumnModel().getColumn(0).setMinWidth(0);
        saleTable.getColumnModel().getColumn(0).setMaxWidth(0);
        saleTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(saleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        return scrollPane;
    }

    private JPanel createSaleActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        totalsPanel.setOpaque(false);

        lblSubtotalValue = new JLabel("$0.00", SwingConstants.RIGHT);
        lblSubtotalValue.setFont(FONT_MAIN);
        lblTaxValue = new JLabel("$0.00", SwingConstants.RIGHT);
        lblTaxValue.setFont(FONT_MAIN);
        lblTotalValue = createBoldLabel("$0.00", SwingConstants.RIGHT);
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 18));

        totalsPanel.add(createBoldLabel("Subtotal:", SwingConstants.LEFT));
        totalsPanel.add(lblSubtotalValue);
        totalsPanel.add(createBoldLabel("Tax (10%):", SwingConstants.LEFT));
        totalsPanel.add(lblTaxValue);
        totalsPanel.add(createBoldLabel("Total:", SwingConstants.LEFT));
        totalsPanel.add(lblTotalValue);

        panel.add(totalsPanel);

        JButton btnCheckout = createSuccessButton("Checkout (F12)", "https://img.icons8.com/fluency-systems-regular/24/ffffff/banknotes.png", e -> onCheckout());
        panel.add(btnCheckout);

        JPanel minorActionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        minorActionsPanel.setOpaque(false);
        minorActionsPanel.add(createSecondaryButton("Hold", "https://img.icons8.com/fluency-systems-regular/20/212529/pause--v1.png", e -> onHold()));
        minorActionsPanel.add(createDangerButton("Clear", "https://img.icons8.com/fluency-systems-regular/20/ffffff/delete-sign.png", e -> onClearSale()));
        panel.add(minorActionsPanel);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_MAIN);
        table.setRowHeight(40);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_PRIMARY);
        table.setSelectionForeground(COLOR_TEXT_LIGHT);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 243, 245));
        header.setForeground(COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        TableCellRenderer renderer = (tbl, value, isSelected, hasFocus, row, column) -> {
            JComponent c = (JComponent) new JTable().getDefaultRenderer(Object.class).getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? new Color(248, 249, 250) : Color.WHITE);
            }
            return c;
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    // --- Button Factory Methods ---

    private JButton createToolbarButton(String text, String iconUrl, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFont(FONT_SMALL);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (actionListener != null) {
            button.addActionListener(actionListener);
        }
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setOpaque(false);
        button.setBackground(COLOR_BG_MAIN);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.setForeground(COLOR_TEXT_DARK);
        button.setIcon(getIcon(iconUrl));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setOpaque(true); button.setBackground(COLOR_TOOLBAR_HOVER); }
            public void mouseExited(MouseEvent e) { button.setOpaque(false); button.setBackground(COLOR_BG_MAIN); }
        });
        return button;
    }

    private JButton createPrimaryButton(String text, String iconUrl, ActionListener actionListener) {
        return createColoredButton(text, iconUrl, COLOR_PRIMARY, COLOR_TEXT_LIGHT, actionListener);
    }

    private JButton createSuccessButton(String text, String iconUrl, ActionListener actionListener) {
        return createColoredButton(text, iconUrl, COLOR_SUCCESS, COLOR_TEXT_LIGHT, actionListener);
    }

    private JButton createDangerButton(String text, String iconUrl, ActionListener actionListener) {
        return createColoredButton(text, iconUrl, COLOR_DANGER, COLOR_TEXT_LIGHT, actionListener);
    }

    private JButton createSecondaryButton(String text, String iconUrl, ActionListener actionListener) {
        JButton button = createColoredButton(text, iconUrl, new Color(248,249,250), COLOR_TEXT_DARK, actionListener);
        button.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        return button;
    }

    private JButton createHeaderButton(String text, String iconUrl, ActionListener actionListener) {
        JButton button = createColoredButton(text, iconUrl, COLOR_BG_HEADER, COLOR_TEXT_LIGHT, actionListener);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        return button;
    }

    private JButton createColoredButton(String text, String iconUrl, Color bg, Color fg, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(actionListener);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setIcon(getIcon(iconUrl));
        return button;
    }

    private JLabel createBoldLabel(String text, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(FONT_BOLD);
        return label;
    }

    private ImageIcon getIcon(String urlString) {
        try {
            URL url = new URL(urlString);
            return new ImageIcon(url);
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + urlString);
            return new ImageIcon();
        }
    }

    private void loadUserProfilePicture() {
        try {
            String sql = "SELECT profile_picture FROM user_profile_picture WHERE user_id = ?";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            Image profileImg = (rs.next() && rs.getBytes("profile_picture") != null)
                    ? Toolkit.getDefaultToolkit().createImage(rs.getBytes("profile_picture"))
                    : ImageIO.read(new URL("https://img.icons8.com/ios-filled/100/ffffff/user-male-circle.png"));

            lblProfileImage.setIcon(new ImageIcon(createCircularImage(profileImg, 50)));
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
        }
    }

    private static Image createCircularImage(Image image, int diameter) {
        BufferedImage bufferedImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));
        g2.drawImage(image, 0, 0, diameter, diameter, null);
        g2.dispose();
        return bufferedImage;
    }

    // --- Data Handling and Logic ---

    public void addItemToCurrentSale(String itemCode, String description, int qty, BigDecimal unitPrice) {
        for (int i = 0; i < saleTableModel.getRowCount(); i++) {
            if (saleTableModel.getValueAt(i, 0).equals(itemCode)) {
                int currentQty = (int) saleTableModel.getValueAt(i, 2);
                int newQty = currentQty + qty;
                BigDecimal newTotal = unitPrice.multiply(new BigDecimal(newQty));
                saleTableModel.setValueAt(newQty, i, 2);
                saleTableModel.setValueAt(newTotal, i, 4);
                updateSaleTotals();
                return;
            }
        }
        BigDecimal total = unitPrice.multiply(new BigDecimal(qty));
        saleTableModel.addRow(new Object[]{itemCode, description, qty, unitPrice, total});
        updateSaleTotals();
    }


    private void updateSaleTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < saleTableModel.getRowCount(); i++) {
            Object totalValue = saleTableModel.getValueAt(i, 4);
            subtotal = subtotal.add((BigDecimal) totalValue);
        }

        BigDecimal taxRate = new BigDecimal("0.10"); // 10% tax
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal total = subtotal.add(tax);

        DecimalFormat df = new DecimalFormat("$#,##0.00");
        lblSubtotalValue.setText(df.format(subtotal));
        lblTaxValue.setText(df.format(tax));
        lblTotalValue.setText(df.format(total));
    }

    private void searchCustomers() {
        String term = txtSearch.getText().trim();
        if (term.isEmpty() || term.equals("Search by Customer ID or Name...")) {
            JOptionPane.showMessageDialog(this, "Please enter a term to search.", "Search Term Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadCustomers(term);
        if (customerTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No customers found matching \"" + term + "\".", "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadCustomers() { loadCustomers(""); }

    private void loadCustomers(String filter) {
        try {
            customerTableModel.setRowCount(0);
            String sql = "SELECT customer_id, name FROM customer";
            if (!filter.isEmpty()) {
                sql += " WHERE customer_id LIKE ? OR name LIKE ?";
            }
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            if (!filter.isEmpty()) {
                ps.setString(1, "%" + filter + "%");
                ps.setString(2, "%" + filter + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                customerTableModel.addRow(new Object[]{rs.getString("customer_id"), rs.getString("name")});
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load customers: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openNewCustomerDialog(java.awt.event.ActionEvent e) {
        new CustomerDialog(this).setVisible(true);
        loadCustomers();
    }

    private void onAddItem(java.awt.event.ActionEvent e) { new AddItemDialog(this).setVisible(true); }
    private void onUpdateItem(java.awt.event.ActionEvent e) { new UpdateItemDialog(this).setVisible(true); }
    private void onDeleteItem(java.awt.event.ActionEvent e) { new DeleteItemDialog(this).setVisible(true); }
    private void onViewCustomers(java.awt.event.ActionEvent e) { loadCustomers(); }


    private void onOrderItem(java.awt.event.ActionEvent e) {
        new OrderItemsDialog(this, userID).setVisible(true);
    }

    private void onCustomerOrderDetail(java.awt.event.ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table first.", "No Customer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String customerId = (String) customerTableModel.getValueAt(selectedRow, 0);
        new CustomerOrderDetailDialog(this, customerId).setVisible(true);
    }

    private void onLogout(java.awt.event.ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new Login().setVisible(true);
        }
    }

    private void onHold() { JOptionPane.showMessageDialog(this, "Sale has been put on hold."); }

    private void onClearSale() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the current sale?", "Clear Sale", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            saleTableModel.setRowCount(0);
            updateSaleTotals(); // Reset totals to zero
            customerTable.clearSelection();
            JOptionPane.showMessageDialog(this, "Sale cleared.");
        }
    }

    private void onCheckout() {
        // --- VALIDATION ---
        if (saleTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "There are no items in the current sale.", "Empty Sale", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedCustomerRow = customerTable.getSelectedRow();
        if (selectedCustomerRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer for this sale.", "No Customer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String customerId = (String) customerTableModel.getValueAt(selectedCustomerRow, 0);

        // --- PAYMENT ---
        BigDecimal totalAmount = new BigDecimal(lblTotalValue.getText().replace("$", "").replace(",", ""));
        PaymentDialog paymentDialog = new PaymentDialog(this, totalAmount);
        paymentDialog.setVisible(true);

        if (!paymentDialog.isSucceeded()) {
            return; // User cancelled or payment failed
        }

        // --- SAVE TO DATABASE ---
        if (saveSaleToDatabase(customerId)) {
            JOptionPane.showMessageDialog(this, "Checkout complete! Change due: " + paymentDialog.getChange(), "Success", JOptionPane.INFORMATION_MESSAGE);
            // Clear sale for next transaction
            saleTableModel.setRowCount(0);
            updateSaleTotals();
            customerTable.clearSelection();
        } else {
            JOptionPane.showMessageDialog(this, "There was an error saving the sale to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean saveSaleToDatabase(String customerId) {
        String orderId = "ORD" + String.valueOf(System.currentTimeMillis()).substring(7);
        try {
            DBConnection.con.setAutoCommit(false);

            // 1. Insert into `order` table
            String insertOrderSQL = "INSERT INTO `order` (order_id, date, user_id) VALUES (?, CURRENT_DATE, ?)";
            try (PreparedStatement psOrder = DBConnection.con.prepareStatement(insertOrderSQL)) {
                psOrder.setString(1, orderId);
                psOrder.setString(2, userID);
                psOrder.executeUpdate();
            }

            // 2. Insert into `customer_order` table
            String insertCustOrderSQL = "INSERT INTO customer_order (customer_id, order_id) VALUES (?, ?)";
            try (PreparedStatement psCustOrder = DBConnection.con.prepareStatement(insertCustOrderSQL)) {
                psCustOrder.setString(1, customerId);
                psCustOrder.setString(2, orderId);
                psCustOrder.executeUpdate();
            }

            // 3. Insert into `order_item` table (batch update)
            String insertOrderItemSQL = "INSERT INTO order_item (item_code, order_id, qty, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psOrderItem = DBConnection.con.prepareStatement(insertOrderItemSQL)) {
                for (int i = 0; i < saleTableModel.getRowCount(); i++) {
                    psOrderItem.setString(1, saleTableModel.getValueAt(i, 0).toString()); // item_code
                    psOrderItem.setInt(2, (int) saleTableModel.getValueAt(i, 2)); // qty
                    psOrderItem.setBigDecimal(3, (BigDecimal) saleTableModel.getValueAt(i, 3)); // unit_price
                    psOrderItem.setString(4, orderId); // order_id
                    psOrderItem.addBatch();
                }
                psOrderItem.executeBatch();
            }

            DBConnection.con.commit();
            return true;

        } catch (SQLException ex) {
            try {
                DBConnection.con.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            ex.printStackTrace();
            return false;
        } finally {
            try {
                DBConnection.con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}