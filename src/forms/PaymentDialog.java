package forms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class PaymentDialog extends JDialog {

    // --- UI Design Constants ---
    private static final Color COLOR_BG_MAIN = new Color(248, 249, 250);
    private static final Color COLOR_SUCCESS = new Color(25, 135, 84);
    private static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_TOTAL = new Font("Segoe UI", Font.BOLD, 24);

    private final BigDecimal totalAmount;
    private JTextField txtAmountReceived;
    private JLabel lblChangeValue;
    private boolean succeeded = false;
    private String change = "$0.00";

    public PaymentDialog(Frame parent, BigDecimal totalAmount) {
        super(parent, "Process Payment", true);
        this.totalAmount = totalAmount;
        setSize(450, 400);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout(15, 15));
        getRootPane().setBorder(new EmptyBorder(20, 25, 20, 25));

        // --- Title ---
        JLabel lblTitle = new JLabel("Payment", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(COLOR_TEXT_DARK);
        add(lblTitle, BorderLayout.NORTH);

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        DecimalFormat df = new DecimalFormat("$#,##0.00");

        // Total Due
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createBoldLabel("Total Due:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel lblTotalDueValue = new JLabel(df.format(totalAmount));
        lblTotalDueValue.setFont(FONT_TOTAL);

        formPanel.add(lblTotalDueValue, gbc);

        // Amount Received
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createBoldLabel("Amount Received:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        txtAmountReceived = new JTextField(10);
        txtAmountReceived.setFont(FONT_MAIN);
        formPanel.add(txtAmountReceived, gbc);

        // Change
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createBoldLabel("Change:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        lblChangeValue = new JLabel(df.format(0));
        lblChangeValue.setFont(FONT_BOLD);
        lblChangeValue.setForeground(COLOR_SUCCESS);
        formPanel.add(lblChangeValue, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- Button Panel ---
        JButton btnPay = new JButton("Confirm Payment");
        btnPay.setFont(FONT_BOLD);
        btnPay.setBackground(COLOR_SUCCESS);
        btnPay.setForeground(COLOR_TEXT_LIGHT);
        btnPay.setPreferredSize(new Dimension(100, 50));
        add(btnPay, BorderLayout.SOUTH);

        // --- Event Listeners ---
        txtAmountReceived.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calculateChange();
            }
        });
        btnPay.addActionListener(e -> processPayment());

        // Allow pressing Enter to confirm payment
        getRootPane().setDefaultButton(btnPay);
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        return label;
    }

    private void calculateChange() {
        try {
            BigDecimal received = new BigDecimal(txtAmountReceived.getText());
            BigDecimal changeAmount = received.subtract(totalAmount);
            if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
                changeAmount = BigDecimal.ZERO;
            }
            change = new DecimalFormat("$#,##0.00").format(changeAmount);
            lblChangeValue.setText(change);
        } catch (NumberFormatException e) {
            lblChangeValue.setText("$0.00");
        }
    }

    private void processPayment() {
        try {
            BigDecimal received = new BigDecimal(txtAmountReceived.getText());
            if (received.compareTo(totalAmount) < 0) {
                JOptionPane.showMessageDialog(this, "The amount received is less than the total due.", "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
                return;
            }
            this.succeeded = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount received.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public String getChange() {
        return change;
    }
}
