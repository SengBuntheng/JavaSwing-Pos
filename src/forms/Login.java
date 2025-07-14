package forms;

import globalValues.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Login extends JDialog {

	private JComboBox<String> cbUser;
	private JPasswordField pwdPassword;
	private Map<String, String> mapUser = new HashMap<>();
	private Map<String, String> mapUserRole = new HashMap<>();

	public static void main(String[] args) {
		DBConnection.connectDB();
		// Run the dialog on the Event Dispatch Thread
		SwingUtilities.invokeLater(() -> {
			try {
				Login dialog = new Login();
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Login() {
		setTitle("POS Login");
		setSize(450, 550);
		setMinimumSize(new Dimension(400, 500));
		setLocationRelativeTo(null);
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
				Color color1 = new Color(44, 62, 80);
				Color color2 = new Color(52, 152, 219);
				GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
				g2d.setPaint(gp);
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		setContentPane(mainPanel);
		mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

		// Center panel for login form elements
		JPanel centerPanel = new JPanel(new GridBagLayout());
		centerPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title
		JLabel lblTitle = new JLabel("SU7.9 POS", SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
		lblTitle.setForeground(Color.WHITE);
		gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weighty = 0.5;
		centerPanel.add(lblTitle, gbc);

		// User Label
		gbc.gridy++; gbc.gridwidth = 1; gbc.weighty = 0;
		JLabel lblUser = new JLabel("Username:");
		styleLabel(lblUser);
		centerPanel.add(lblUser, gbc);

		// User ComboBox
		cbUser = new JComboBox<>();
		styleComboBox(cbUser);
		gbc.gridx = 1;
		centerPanel.add(cbUser, gbc);

		// Password Label
		gbc.gridx = 0; gbc.gridy++;
		JLabel lblPassword = new JLabel("Password:");
		styleLabel(lblPassword);
		centerPanel.add(lblPassword, gbc);

		// Password Field
		pwdPassword = new JPasswordField();
		styleTextField(pwdPassword);
		gbc.gridx = 1;
		centerPanel.add(pwdPassword, gbc);

		// Buttons Panel
		gbc.gridx = 1; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		btnPanel.setOpaque(false);
		JButton btnLogin = new JButton("Login");
		styleButton(btnLogin, new Color(25, 135, 84));
		btnPanel.add(btnLogin);
		centerPanel.add(btnPanel, gbc);

		mainPanel.add(centerPanel, BorderLayout.CENTER);

		// Load users when the window opens
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				loadUsersFromDB();
			}
		});

		// Login action
		btnLogin.addActionListener(e -> performLogin());
		getRootPane().setDefaultButton(btnLogin);
	}

	private void styleLabel(JLabel label) {
		label.setForeground(Color.WHITE);
		label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
	}

	private void styleComboBox(JComboBox<?> comboBox) {
		comboBox.setPreferredSize(new Dimension(220, 35));
		comboBox.setBackground(Color.WHITE);
		comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
	}

	private void styleTextField(JTextField field) {
		field.setPreferredSize(new Dimension(220, 35));
		field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200)),
				new EmptyBorder(5, 8, 5, 8)
		));
	}

	private void styleButton(JButton button, Color color) {
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		button.setFont(new Font("Segoe UI", Font.BOLD, 15));
		button.setPreferredSize(new Dimension(120, 40));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void loadUsersFromDB() {
		mapUser.clear();
		mapUserRole.clear();
		cbUser.removeAllItems();
		try {
			String sql = "SELECT u.user_id, u.password, r.role " +
					"FROM user u LEFT JOIN user_role r ON u.user_role_id = r.id";
			PreparedStatement stmt = DBConnection.con.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String id = rs.getString("user_id");
				mapUser.put(id, rs.getString("password"));
				mapUserRole.put(id, rs.getString("role"));
				cbUser.addItem(id);
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Failed to load users from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void performLogin() {
		String userId = (String) cbUser.getSelectedItem();
		String password = new String(pwdPassword.getPassword());

		if (userId == null) {
			JOptionPane.showMessageDialog(this, "Please select a user.", "Login Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String storedPass = mapUser.get(userId);
		if (storedPass != null && storedPass.equals(password)) {
			String role = mapUserRole.getOrDefault(userId, "User");
			dispose();
			// Run the main panel on the EDT
			SwingUtilities.invokeLater(() -> new MainPanel(userId , role).setVisible(true));
		} else {
			JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
}

