package com.example.manantial.loader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.example.manantial.modelo.Tabla;
import com.example.manantial.vista.language.Language;

public class MainLoader {
	public static final String working_dir = System.getProperty("os.name").toLowerCase().contains("win")?System.getenv("APPDATA")+"\\Manantial":"\\";
	public static final String defaultpath = working_dir+"\\file.csv";
	static Scanner scan = new Scanner(System.in);
	
	public static void main (String[] a) {
		String path = defaultpath;
		if (a.length>0) {
			if (a[0].equals("thinker")) {
				new Cli();
			}
			else path = a[0];
		} else
		new Loader(path);
	}

	public static Connection getCon() {
		Connection con = null;
		var answer = true;
		do {
			System.out.print("Password? ");
			var pass = scan.nextLine();
			try {
				con = DriverManager.getConnection("jdbc:derby:"+working_dir+"\\Manantial;user=root;password="+pass);
				answer = false;
				con.setSchema("APP");
			} catch (Exception e) {
				if (e instanceof SQLException) {
					var sqlE = (SQLException) e;
					if (sqlE.getSQLState().equals("XJ004")) {//database doesn't exist
						try {
							con = DriverManager.getConnection("jdbc:derby:"+working_dir+"\\Manantial"+";create=true");
							//con.setSchema("APP");
							createTable(con.createStatement(),pass);
							return con;
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					} else if (sqlE.getSQLState().equals("08004")) {//Authentication error
						System.out.println("Password not accepted");
					} else {
						e.printStackTrace();
						System.exit(-1);
					}
				} else abort(e);
			}
		} while (answer);
		return con;
	}
	
	private static void createTable(Statement st,String pass) throws SQLException {
		st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.connection.requireAuthentication', 'true')");
		st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.authentication.provider', 'BUILTIN')");
		st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user.root', '"+pass+"')");
		st.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.database.propertiesOnly', 'true')");
		st.execute("CREATE TABLE Inventario ("
			+ "codigo bigint NOT NULL,"
			+ "nombre varchar(255) NOT NULL,"
			+ "precio int NOT NULL,"
			+ "cantidad int NOT NULL,"
			+ "PRIMARY KEY (codigo))");
	}

	public static String requestPassword(boolean b) {
		System.out.print("Password? ");
		var pass = scan.nextLine();
		if (b) {//if creating password
			var repeat = true;
			while (repeat) {
				System.out.print("Repeat: ");
				var confirm = scan.nextLine();
				if (confirm.equals(pass))
					repeat = false;
				else {
					System.out.println("Passwords don't match");
					pass = scan.nextLine();
				}
			}
		}
		return pass;
	}
	
	public static Tabla abort(SQLException e) {
		System.out.println(e.getSQLState());
		abort((Exception)e);
		return null;
	}
	
	public static void abort(Exception e) {
		System.out.println(e.getMessage());
		e.printStackTrace();
		System.out.print(Language.libraryError);
		System.exit(-1);
	}
}
