package com.example.manantial.loader;

import static com.example.manantial.loader.MainLoader.getCon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Cli {
	public Cli () {
		boolean answer = true;
		var scan = new Scanner(System.in);
		var con = getCon();
		while (answer) {
			System.out.print("> ");
			var line = scan.nextLine();
			if (line.equals("exit")) answer = false;
			else {
				try (var st = con.createStatement()) {
					ResultSet rs;
					if(st.execute(line)) {
						rs = st.getResultSet();
						var md = rs.getMetaData();
						for (int i = 1; i <= md.getColumnCount();i++)
							System.out.print(md.getColumnLabel(i)+(md.getColumnLabel(i).equals("CODIGO")?"\t\t":(md.getColumnName(i).equals("NOMBRE")?"\t\t\t":"\t")));
						System.out.println();
						while (rs.next()) {
							for (int i = 1; i <= md.getColumnCount();i++)
								System.out.print(rs.getString(i)+"\t");
							System.out.println();
						}//SELECT * FROM Inventario
						System.out.println();
					}
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		scan.close();
	}
}
