package forms;

import globalValues.DBConnection; // Assuming this class exists
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginDialog extends JDialog {

	// --- UI Theme Constants ---
	private static final Color PRIMARY_COLOR_DARK = new Color(34, 40, 49);
	private static final Color PRIMARY_COLOR_LIGHT = new Color(57, 62, 70);
	private static final Color ACCENT_COLOR = new Color(0, 173, 181);
	private static final Color TEXT_COLOR_LIGHT = new Color(238, 238, 238);
	private static final Color ERROR_COLOR = new Color(255, 107, 107);
	private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 16);
	private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 16);
	private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 38);

	// --- UI Components ---
	private JTextField txtUsername;
	private JPasswordField pwdPassword;
	private JButton btnLogin;
	private JLabel lblErrorMessage;

	public LoginDialog() {
		setTitle("POS System Login");
		setModal(true);
		setSize(480, 600);
		setMinimumSize(new Dimension(450, 550));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		initUI();
	}

	private void initUI() {
		// Main panel with a gradient background
		JPanel mainPanel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR_DARK, 0, getHeight(), PRIMARY_COLOR_LIGHT);
				g2d.setPaint(gp);
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
		setContentPane(mainPanel);

		// Center panel for login form
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		// --- Title ---
		JLabel lblTitle = new JLabel("Welcome");
		lblTitle.setFont(FONT_TITLE);
		lblTitle.setForeground(TEXT_COLOR_LIGHT);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 40, 0);
		formPanel.add(lblTitle, gbc);

		// Reset insets
		gbc.insets = new Insets(8, 5, 8, 5);

		// --- Username Field ---
		gbc.gridy++; gbc.gridwidth = 2;
		txtUsername = createStyledTextField("Username");
		formPanel.add(txtUsername, gbc);

		// --- Password Field ---
		gbc.gridy++;
		pwdPassword = createStyledPasswordField("Password");
		JToggleButton btnToggleVisibility = createVisibilityToggleButton();
		JPanel passwordContainer = createFieldWithButton(pwdPassword, btnToggleVisibility);
		formPanel.add(passwordContainer, gbc);

		// --- Error Message Label ---
		gbc.gridy++;
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setForeground(ERROR_COLOR);
		lblErrorMessage.setFont(FONT_REGULAR.deriveFont(14f));
		lblErrorMessage.setHorizontalAlignment(SwingConstants.CENTER);
		formPanel.add(lblErrorMessage, gbc);

		// --- Login Button ---
		gbc.gridy++;
		gbc.insets = new Insets(20, 5, 8, 5);
		btnLogin = createStyledButton("Login");
		formPanel.add(btnLogin, gbc);

		mainPanel.add(formPanel, BorderLayout.CENTER);

		// --- Actions ---
		btnLogin.addActionListener(e -> performLogin());
		getRootPane().setDefaultButton(btnLogin);
	}

	private void performLogin() {
		String username = txtUsername.getText();
		String password = new String(pwdPassword.getPassword());

		if (username.isEmpty() || password.isEmpty() || username.equals("Username")) {
			showError("Username and password cannot be empty.");
			return;
		}

		// Securely query only for the entered user
		try {
			String sql = "SELECT u.password, r.role FROM user u " +
					"LEFT JOIN user_role r ON u.user_role_id = r.id " +
					"WHERE u.user_id = ?";
			PreparedStatement stmt = DBConnection.con.prepareStatement(sql);
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String storedPassword = rs.getString("password");
				// In a real system, passwords should be hashed (e.g., with BCrypt)
				if (Objects.equals(password, storedPassword)) {
					String role = rs.getString("role");
					dispose(); // Close login dialog
					SwingUtilities.invokeLater(() -> new MainPanel(username, role, username).setVisible(true));
				} else {
					showError("Invalid username or password.");
				}
			} else {
				showError("Invalid username or password.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showError("Database connection error.");
		}
	}

	// --- Helper methods for creating styled components ---

	private JTextField createStyledTextField(String placeholder) {
		JTextField textField = new JTextField();
		setupTextField(textField, placeholder);
		return textField;
	}

	private JPasswordField createStyledPasswordField(String placeholder) {
		JPasswordField passwordField = new JPasswordField();
		setupTextField(passwordField, placeholder);
		return passwordField;
	}

	private void setupTextField(JTextField field, String placeholder) {
		field.setFont(FONT_REGULAR);
		field.setForeground(Color.GRAY);
		field.setText(placeholder);
		field.setBackground(PRIMARY_COLOR_LIGHT);
		field.setCaretColor(ACCENT_COLOR);
		field.setBorder(new RoundedBorder(10));
		field.setPreferredSize(new Dimension(250, 45));

		// Placeholder text functionality
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (field.getText().equals(placeholder)) {
					field.setText("");
					field.setForeground(TEXT_COLOR_LIGHT);
					if (field instanceof JPasswordField) {
						((JPasswordField) field).setEchoChar('•');
					}
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (field.getText().isEmpty()) {
					if (field instanceof JPasswordField) {
						((JPasswordField) field).setEchoChar((char) 0);
					}
					field.setText(placeholder);
					field.setForeground(Color.GRAY);
				}
			}
		});
		if (field instanceof JPasswordField) {
			((JPasswordField) field).setEchoChar((char) 0);
		}
	}

	private JToggleButton createVisibilityToggleButton() {
		// Note: You will need eye-open.png and eye-closed.png icons
		ImageIcon eyeIcon = createIcon("/icons/eye-open.png");
		ImageIcon eyeOffIcon = createIcon("/icons/eye-closed.png");

		JToggleButton toggleButton = new JToggleButton(eyeOffIcon);
		toggleButton.setSelectedIcon(eyeIcon);
		toggleButton.setFocusPainted(false);
		toggleButton.setBorderPainted(false);
		toggleButton.setContentAreaFilled(false);
		toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		toggleButton.setPreferredSize(new Dimension(30, 30));

		toggleButton.addActionListener(e -> {
			boolean isSelected = toggleButton.isSelected();
			pwdPassword.setEchoChar(isSelected ? (char) 0 : '•');
		});
		return toggleButton;
	}

	private JPanel createFieldWithButton(JComponent field, JToggleButton button) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.add(field, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, (field.getPreferredSize().height - button.getPreferredSize().height) / 2));
		buttonPanel.setOpaque(false);
		buttonPanel.add(button);
		panel.add(buttonPanel, BorderLayout.EAST);
		return panel;
	}

	private JButton createStyledButton(String text) {
		JButton button = new JButton(text);
		button.setFont(FONT_BOLD);
		button.setForeground(PRIMARY_COLOR_DARK);
		button.setBackground(ACCENT_COLOR);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setPreferredSize(new Dimension(250, 50));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) { button.setBackground(ACCENT_COLOR.brighter()); }
			@Override
			public void mouseExited(MouseEvent e) { button.setBackground(ACCENT_COLOR); }
		});
		return button;
	}

	private void showError(String message) {
		lblErrorMessage.setText(message);
		Timer timer = new Timer(4000, e -> lblErrorMessage.setText(" "));
		timer.setRepeats(false);
		timer.start();
	}

	private ImageIcon createIcon(String path) {
		try {
			return new ImageIcon(Objects.requireNonNull(getClass().getResource(path)));
		} catch (Exception e) {
			// Return a blank icon if the file is not found
			return new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
		}
	}

	private static class RoundedBorder implements Border {
		private final int radius;
		RoundedBorder(int radius) { this.radius = radius; }
		public Insets getBorderInsets(Component c) { return new Insets(this.radius, this.radius, this.radius, this.radius); }
		public boolean isBorderOpaque() { return true; }
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(c.getBackground().darker());
			g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
			g2.dispose();
		}
	}
}