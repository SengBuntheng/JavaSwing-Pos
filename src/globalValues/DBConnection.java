package globalValues;
import java.sql.*;

public class DBConnection {
	private static String connectionString = "jdbc:mysql://localhost:3306/pos1?serverTimezone=Asia/Novosibirsk&useSSL=false&allowPublicKeyRetrieval=true";
	private static String connectionUser = "root";
	private static String connectionPassword = "";
	public static Connection con = null;
	public String userID = "";
	public static int selectedIDIndex = -1;


	private DBConnection(){
		throw new RuntimeException();
	}

	public static void connectDB(){
		// TODO Auto-generated method stub
		try {
			con = DriverManager.getConnection(connectionString, connectionUser, connectionPassword);
		}catch(Exception e){
			System.out.println(e);
		}
	}

	private static void setupTestData() throws Exception {
		con.createStatement().execute("CREATE TABLE user_role (id INT PRIMARY KEY, role VARCHAR(50))");
		con.createStatement().execute("CREATE TABLE user (user_id VARCHAR(50) PRIMARY KEY, password VARCHAR(50), user_role_id INT)");
		con.createStatement().execute("INSERT INTO user_role VALUES (1, 'Admin'), (2, 'Cashier')");
		// Passwords should be hashed in a real system!
		con.createStatement().execute("INSERT INTO user VALUES ('admin', 'admin123', 1)");
		con.createStatement().execute("INSERT INTO user VALUES ('cashier1', 'pass', 2)");
	}

}

	
