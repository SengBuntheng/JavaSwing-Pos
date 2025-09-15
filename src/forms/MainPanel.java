package forms;

import globalValues.DBConnection;
import globalValues.Products;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MainPanel extends JFrame {

    private final String userID, userRole;
    private final Map<String, Products> productsMap = new HashMap<>();
    private final Map<String, CartItem> cartItemsMap = new HashMap<>();

    private JLabel lblSubTotalValue, lblTaxValue, lblTotalValue, lblSelectedCustomer;
    private JPanel productGridPanel, cartItemsPanel, categoryPanel;
    private JTextField txtDiscount;
    private JButton btnPay;
    private String selectedCustomerId = null;

    private static class CartItem {
        Products product;
        int quantity;
        JLabel quantityLabel, priceLabel;
        JPanel panel;
        CartItem(Products p, int qty) { this.product = p; this.quantity = qty; }
    }

    public MainPanel(String userID, String userRole , String username) {
        this.userID = userID;
        this.userRole = userRole;

        setTitle("Main Application Dashboard" + userID);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        UITheme.applyProfessionalLook();
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createProductDisplayPanel(), createCheckoutPanel());
        splitPane.setDividerLocation(0.65);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
        loadCategories();
        loadProducts("All");
        setVisible(true);
    }

    // --- UI Panel Creation ---

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(UITheme.PRIMARY_GREEN);
        headerPanel.setBorder(new EmptyBorder(8, 25, 8, 25));
        JLabel lblTitle = new JLabel("POS System");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        JButton btnSelectCustomer = new JButton("Select Customer");
        UITheme.stylePrimaryButton(btnSelectCustomer);
        btnSelectCustomer.setBackground(UITheme.ACCENT_GREEN);
        btnSelectCustomer.setForeground(UITheme.PRIMARY_GREEN);
        btnSelectCustomer.addActionListener(e -> selectCustomer());
        rightPanel.add(btnSelectCustomer);

        if(userRole.equalsIgnoreCase("Admin")) rightPanel.add(createAdminMenu());

        JLabel lblProfileImage = createCircularImageLabel("https://img.icons8.com/ios-filled/100/ffffff/user-male-circle.png");
        loadUserProfilePicture(lblProfileImage);
        rightPanel.add(lblProfileImage);

        JLabel lblUsername = new JLabel(userID);
        lblUsername.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        lblUsername.setForeground(Color.WHITE);
        rightPanel.add(lblUsername);

        headerPanel.add(rightPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createProductDisplayPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(15, 20));
        leftPanel.setBorder(new EmptyBorder(15, 20, 20, 10));
        leftPanel.setBackground(UITheme.BACKGROUND);
        leftPanel.add(createTopBar(), BorderLayout.NORTH);

        productGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        productGridPanel.setBackground(UITheme.BACKGROUND);
        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(createCategoryPanel(), BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setOpaque(false);
        JButton btnAddItem = new JButton("+ Add New Item");
        UITheme.stylePrimaryButton(btnAddItem);
        btnAddItem.addActionListener(e -> new AddItemDialog(this).setVisible(true));
        if (!userRole.equalsIgnoreCase("Admin")) btnAddItem.setVisible(false);
        topPanel.add(btnAddItem, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(UITheme.PANEL_BG);
        searchPanel.setBorder(UITheme.BORDER_NORMAL);
        JTextField txtSearch = new JTextField("Search items here...");
        txtSearch.setBorder(new EmptyBorder(10, 12, 10, 12));
        txtSearch.setForeground(UITheme.TEXT_LIGHT);
        txtSearch.addActionListener(e -> loadProducts(txtSearch.getText()));
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        JButton btnSearch = new JButton(createIcon("https://img.icons8.com/material-rounded/20/ffffff/search.png"));
        UITheme.stylePrimaryButton(btnSearch);
        btnSearch.setBorder(null);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel createCategoryPanel() {
        categoryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        categoryPanel.setBackground(UITheme.PANEL_BG);
        categoryPanel.setBorder(UITheme.BORDER_NORMAL);
        return categoryPanel;
    }

    private JPanel createCheckoutPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 15));
        rightPanel.setBorder(new EmptyBorder(15, 10, 20, 20));
        rightPanel.setBackground(UITheme.PANEL_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(new JLabel("Checkout"), BorderLayout.WEST);
        lblSelectedCustomer = new JLabel("(Guest)", SwingConstants.RIGHT);
        header.add(lblSelectedCustomer, BorderLayout.EAST);
        rightPanel.add(header, BorderLayout.NORTH);

        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(UITheme.PANEL_BG);
        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.PANEL_BG);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(createSaleActionsPanel(), BorderLayout.SOUTH);
        return rightPanel;
    }

    private JPanel createSaleActionsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(createTotalsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createActionButtonsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        btnPay = new JButton("Pay ($0.00)");
        UITheme.stylePrimaryButton(btnPay);
        btnPay.setFont(UITheme.FONT_TITLE);
        btnPay.addActionListener(e -> onCheckout());
        mainPanel.add(btnPay);
        return mainPanel;
    }

    private JPanel createTotalsPanel() {
        JPanel totalsPanel = new JPanel(new GridBagLayout());
        totalsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        totalsPanel.add(new JLabel("Discount (%)"), gbc);
        txtDiscount = new JTextField("20");
        txtDiscount.setHorizontalAlignment(JTextField.RIGHT);
        txtDiscount.addActionListener(e -> updateCheckoutTotals());
        gbc.anchor = GridBagConstraints.EAST;
        totalsPanel.add(txtDiscount, gbc);
        gbc.gridy++; gbc.anchor = GridBagConstraints.WEST;
        totalsPanel.add(new JLabel("Sub Total"), gbc);
        lblSubTotalValue = new JLabel("$0.00", SwingConstants.RIGHT);
        gbc.anchor = GridBagConstraints.EAST;
        totalsPanel.add(lblSubTotalValue, gbc);
        gbc.gridy++; gbc.anchor = GridBagConstraints.WEST;
        totalsPanel.add(new JLabel("Tax 1.5%"), gbc);
        lblTaxValue = new JLabel("$0.00", SwingConstants.RIGHT);
        gbc.anchor = GridBagConstraints.EAST;
        totalsPanel.add(lblTaxValue, gbc);
        gbc.gridy++; gbc.gridwidth = 2; gbc.insets = new Insets(10,0,10,0);
        totalsPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(4,4,4,4);
        gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel totalLabel = new JLabel("Total");
        totalLabel.setFont(UITheme.FONT_TITLE);
        totalsPanel.add(totalLabel, gbc);
        lblTotalValue = new JLabel("$0.00", SwingConstants.RIGHT);
        lblTotalValue.setFont(UITheme.FONT_TITLE);
        gbc.anchor = GridBagConstraints.EAST;
        totalsPanel.add(lblTotalValue, gbc);
        return totalsPanel;
    }

    private JPanel createActionButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonsPanel.setOpaque(false);
        JButton btnCancel = new JButton("Cancel Order");
        UITheme.styleSecondaryButton(btnCancel, UITheme.CANCEL_COLOR);
        btnCancel.addActionListener(e -> onClearSale());
        buttonsPanel.add(btnCancel);
        JButton btnHold = new JButton("Hold Order");
        UITheme.styleSecondaryButton(btnHold, UITheme.HOLD_COLOR);
        buttonsPanel.add(btnHold);
        return buttonsPanel;
    }


    // --- UI Component Creation ---
    private JComponent createImagePlaceholder() {
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(new Color(230, 230, 230));
        JLabel placeholderText = new JLabel("No Image");
        placeholderText.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        placeholderText.setForeground(Color.GRAY);
        placeholder.add(placeholderText);
        return placeholder;
    }

    private JPanel createProductCard(Products product) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(170, 180));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(UITheme.BORDER_NORMAL);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { addItemToCart(product); }
            public void mouseEntered(MouseEvent e) { card.setBorder(UITheme.BORDER_HOVER); }
            public void mouseExited(MouseEvent e) { card.setBorder(UITheme.BORDER_NORMAL); }
        });
        card.add(createImagePlaceholder(), BorderLayout.CENTER);
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        JLabel lblName = new JLabel(product.getName());
        lblName.setFont(UITheme.FONT_MAIN);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblPrice = new JLabel(new DecimalFormat("$#,##0.00").format(product.getPrice()));
        lblPrice.setFont(UITheme.FONT_PRICE);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(lblName);
        infoPanel.add(lblPrice);
        card.add(infoPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCartItemPanel(CartItem cartItem) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(UITheme.PANEL_BG);
        p.setBorder(new EmptyBorder(10, 5, 10, 5));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel lblName = new JLabel(cartItem.product.getName());
        p.add(lblName, BorderLayout.WEST);
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        qtyPanel.setOpaque(false);
        JButton btnMinus = new JButton("-");
        cartItem.quantityLabel = new JLabel(String.valueOf(cartItem.quantity));
        cartItem.quantityLabel.setFont(UITheme.FONT_BOLD);
        JButton btnPlus = new JButton("+");
        btnPlus.addActionListener(e -> updateCartItemQuantity(cartItem.product.getProductID(), 1));
        btnMinus.addActionListener(e -> updateCartItemQuantity(cartItem.product.getProductID(), -1));
        qtyPanel.add(btnMinus);
        qtyPanel.add(cartItem.quantityLabel);
        qtyPanel.add(btnPlus);
        p.add(qtyPanel, BorderLayout.CENTER);
        BigDecimal itemTotal = cartItem.product.getPrice().multiply(new BigDecimal(cartItem.quantity));
        cartItem.priceLabel = new JLabel(new DecimalFormat("$##0.00").format(itemTotal));
        cartItem.priceLabel.setFont(UITheme.FONT_BOLD);
        p.add(cartItem.priceLabel, BorderLayout.EAST);
        cartItem.panel = p;
        return p;
    }

    // --- Data Loading and Logic ---
    private void loadCategories() {
        String[] categories = {"All", "Coffee", "Beverages", "Snacks", "Desserts"};
        for (String cat : categories) {
            JButton btn = new JButton(cat);
            boolean isActive = cat.equals("All");
            btn.setForeground(isActive ? UITheme.PRIMARY_GREEN : UITheme.TEXT_LIGHT);
            btn.setBackground(UITheme.PANEL_BG);
            btn.setFont(UITheme.FONT_BOLD);
            btn.setBorder(new EmptyBorder(12, 25, 12, 25));
            btn.addActionListener(e -> {
                for (Component c : categoryPanel.getComponents()) {
                    c.setForeground(UITheme.TEXT_LIGHT);
                }
                btn.setForeground(UITheme.PRIMARY_GREEN);
                loadProducts(cat);
            });
            categoryPanel.add(btn);
        }
    }

    private void loadProducts(String filter) {
        productGridPanel.removeAll();
        productsMap.clear();
        try {
            String sql = "SELECT item_code, description, unit_price FROM item";
            if (!"All".equalsIgnoreCase(filter) && !filter.isEmpty() && !filter.equals("Search items here...")) {
                sql += " WHERE description LIKE ?";
            }
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            if (!"All".equalsIgnoreCase(filter) && !filter.isEmpty() && !filter.equals("Search items here...")) {
                ps.setString(1, "%" + filter + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Products p = new Products(rs.getString("item_code"), rs.getString("description"), "", null, null, rs.getBigDecimal("unit_price"), 0, false);
                productsMap.put(p.getProductID(), p);
                productGridPanel.add(createProductCard(p));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage());
        }
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    public void addItemToCart(Products product) {
        if (cartItemsMap.containsKey(product.getProductID())) {
            updateCartItemQuantity(product.getProductID(), 1);
        } else {
            CartItem newItem = new CartItem(product, 1);
            cartItemsMap.put(product.getProductID(), newItem);
            cartItemsPanel.add(createCartItemPanel(newItem));
        }
        refreshCartUI();
    }

    private void updateCartItemQuantity(String itemCode, int change) {
        CartItem cartItem = cartItemsMap.get(itemCode);
        if (cartItem == null) return;
        cartItem.quantity += change;
        if (cartItem.quantity <= 0) {
            cartItemsMap.remove(itemCode);
            cartItemsPanel.remove(cartItem.panel);
        } else {
            cartItem.quantityLabel.setText(String.valueOf(cartItem.quantity));
            BigDecimal newTotal = cartItem.product.getPrice().multiply(new BigDecimal(cartItem.quantity));
            cartItem.priceLabel.setText(new DecimalFormat("$##0.00").format(newTotal));
        }
        refreshCartUI();
    }

    private void refreshCartUI() {
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
        updateCheckoutTotals();
    }

    private void updateCheckoutTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItemsMap.values()) {
            subtotal = subtotal.add(item.product.getPrice().multiply(new BigDecimal(item.quantity)));
        }
        BigDecimal discountPercent;
        try {
            double discount = Double.parseDouble(txtDiscount.getText().trim());
            discount = Math.max(0, Math.min(100, discount));
            discountPercent = BigDecimal.valueOf(discount / 100.0);
        } catch (NumberFormatException e) {
            discountPercent = BigDecimal.ZERO;
        }
        BigDecimal subtotalAfterDiscount = subtotal.subtract(subtotal.multiply(discountPercent));
        BigDecimal tax = subtotalAfterDiscount.multiply(new BigDecimal("0.015"));
        BigDecimal total = subtotalAfterDiscount.add(tax).setScale(2, RoundingMode.HALF_UP);
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        lblSubTotalValue.setText(df.format(subtotal));
        lblTaxValue.setText(df.format(tax));
        lblTotalValue.setText(df.format(total));
        btnPay.setText("Pay (" + df.format(total) + ")");
    }

    private void onClearSale() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this order?", "Cancel Order", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            cartItemsMap.clear();
            cartItemsPanel.removeAll();
            selectedCustomerId = null;
            lblSelectedCustomer.setText("(Guest)");
            refreshCartUI();
        }
    }

    private void onCheckout() {
        if (cartItemsMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The cart is empty.", "Checkout Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal totalAmount = new BigDecimal(lblTotalValue.getText().replaceAll("[^\\d.]", ""));
        PaymentDialog paymentDialog = new PaymentDialog(this, totalAmount);
        paymentDialog.setVisible(true);
        if (paymentDialog.isSucceeded()) {
            saveSaleToDatabase();
        }
    }

    private void saveSaleToDatabase() {
        // 1. Generate a unique Order ID that fits the database schema (e.g., VARCHAR(10)).
        // Using a substring of the current time is a simple way to create a unique ID.
        String orderId = "O" + String.valueOf(System.currentTimeMillis()).substring(4);

        try {
            // 2. Begin a transaction. This ensures that all database operations
            // either succeed together or fail together, preventing partial data saves.
            DBConnection.con.setAutoCommit(false);

            // 3. Insert the main order record into the `order` table.
            String orderSQL = "INSERT INTO `order` (order_id, date, user_id) VALUES (?, CURRENT_DATE, ?)";
            try (PreparedStatement ps = DBConnection.con.prepareStatement(orderSQL)) {
                ps.setString(1, orderId);
                ps.setString(2, this.userID);
                ps.executeUpdate();
            }

            // 4. (Optional) If a customer was selected, link them to the order.
            if (this.selectedCustomerId != null) {
                String customerOrderSQL = "INSERT INTO customer_order (customer_id, order_id) VALUES (?, ?)";
                try (PreparedStatement ps = DBConnection.con.prepareStatement(customerOrderSQL)) {
                    ps.setString(1, this.selectedCustomerId);
                    ps.setString(2, orderId);
                    ps.executeUpdate();
                }
            }

            // 5. Insert all cart items into the `order_item` table using a batch for efficiency.
            String itemSQL = "INSERT INTO order_item (item_code, order_id, qty, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = DBConnection.con.prepareStatement(itemSQL)) {
                for (CartItem item : cartItemsMap.values()) {
                    // Ensure parameter order matches the SQL statement's column order.
                    ps.setString(1, item.product.getProductID()); // ? #1 is item_code
                    ps.setString(2, orderId);                     // ? #2 is order_id
                    ps.setInt(3, item.quantity);                  // ? #3 is qty
                    ps.setBigDecimal(4, item.product.getPrice()); // ? #4 is unit_price
                    ps.addBatch(); // Add the prepared statement to the batch.
                }
                ps.executeBatch(); // Execute all statements in the batch at once.
            }

            // 6. If all operations were successful, commit the transaction to save the changes.
            DBConnection.con.commit();

            JOptionPane.showMessageDialog(this, "Checkout complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
            onClearSaleConfirmed(); // Clear the UI for the next sale.

        } catch (SQLException ex) {
            // 7. If any error occurred, roll back the entire transaction.
            try {
                DBConnection.con.rollback();
            } catch (SQLException e) {
                e.printStackTrace(); // Log rollback failure
            }
            JOptionPane.showMessageDialog(this, "Error saving sale: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Print full error to console for debugging.

        } finally {
            // 8. Always restore the default auto-commit behavior in a finally block.
            try {
                DBConnection.con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void onClearSaleConfirmed() {
        cartItemsMap.clear();
        cartItemsPanel.removeAll();
        selectedCustomerId = null;
        lblSelectedCustomer.setText("(Guest)");
        refreshCartUI();
    }


    // --- Helper & Utility Methods ---
    private void selectCustomer() {
        CustomerSelectionDialog dialog = new CustomerSelectionDialog(this);
        dialog.setVisible(true);
        if (dialog.hasSelectedCustomer()) {
            selectedCustomerId = dialog.getSelectedCustomerId();
            lblSelectedCustomer.setText(dialog.getSelectedCustomerName());
        }
    }

    private JButton createAdminMenu() {
        JButton adminButton = createHeaderIcon("https://img.icons8.com/fluency-systems-regular/28/ffffff/admin-settings-male.png");
        JPopupMenu adminMenu = new JPopupMenu();
        JMenuItem updateItem = new JMenuItem("Update Item");
        updateItem.addActionListener(e -> new UpdateItemDialog(this).setVisible(true));
        adminMenu.add(updateItem);
        JMenuItem deleteItem = new JMenuItem("Delete Item");
        deleteItem.addActionListener(e -> new DeleteItemDialog(this).setVisible(true));
        adminMenu.add(deleteItem);
        adminButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                adminMenu.show(e.getComponent(), 0, e.getComponent().getHeight());
            }
        });
        return adminButton;
    }

    private void styleButton(JButton button, Color bg, Color fg, Font font) { button.setBackground(bg); button.setForeground(fg); button.setFont(font); button.setFocusPainted(false); }
    private JButton createHeaderIcon(String url) { JButton b = new JButton(createIcon(url)); b.setBorder(null); b.setContentAreaFilled(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
    private GridBagConstraints createGbc() { GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(4, 4, 4, 4); return gbc; }
    private ImageIcon createIcon(String urlString) { try { return new ImageIcon(new ImageIcon(new URL(urlString)).getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH)); } catch (Exception e) { return new ImageIcon(new BufferedImage(28, 28, BufferedImage.TYPE_INT_ARGB)); } }

    private JLabel createCircularImageLabel(String defaultIconUrl) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(45, 45));
        try {
            Image img = new ImageIcon(new URL(defaultIconUrl)).getImage();
            label.setIcon(new ImageIcon(createCircularImage(img, 45)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return label;
    }

    private void loadUserProfilePicture(JLabel label) {
        try {
            String sql = "SELECT profile_picture FROM user_profile_picture WHERE user_id = ?";
            PreparedStatement ps = DBConnection.con.prepareStatement(sql);
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getBytes("profile_picture") != null) {
                Image img = Toolkit.getDefaultToolkit().createImage(rs.getBytes("profile_picture"));
                label.setIcon(new ImageIcon(createCircularImage(img, 45)));
            }
        } catch (Exception e) {
            System.err.println("Could not load user profile picture: " + e.getMessage());
        }
    }

    private Image createCircularImage(Image image, int diameter) {
        BufferedImage bImg = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));
        g2.drawImage(image, 0, 0, diameter, diameter, null);
        g2.dispose();
        return bImg;
    }
}