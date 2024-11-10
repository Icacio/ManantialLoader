package com.example.manantial.loader;

import static com.example.manantial.loader.MainLoader.getCon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.example.manantial.modelo.Entity;
import com.example.manantial.modelo.Tabla;

public class Loader {

	public Loader (String string) {
		var file = readFile(string);
		var db = readDB(getCon());
		System.out.print("saving...");
		db.suma(file).save();
		System.out.print("saved.");
	}
	
	private Tabla readFile(String string) {
		var file = new File(string);
		var absolutePath = file.getAbsolutePath();
		try (var reader = new BufferedReader(new FileReader(absolutePath))) {
			String line = reader.readLine();//skips the first line
			var com = Pattern.compile("([0-9]+),([^,]+),([0-9]+),([0-9]+)[,(.*)]*");
			var codeList = new ArrayList<Long>();
			var nameList = new ArrayList<String>();
			var pricList = new ArrayList<Integer>();
			var qntyList = new ArrayList<Integer>();
			while ((line = reader.readLine()) != null) {
				var matcher = com.matcher(line);
				if (matcher.find()) {
					System.out.println(line);
					codeList.add(Long.parseLong(matcher.group(1)));
					nameList.add(matcher.group(2));
					pricList.add(Integer.parseInt(matcher.group(3)));
					qntyList.add(Integer.parseInt(matcher.group(4)));
				}
			}
			var length = codeList.size();
			
			var code = new long[length];
			for (int i = 0; i < length;i++) {
				code[i] = codeList.get(i);
			}
			var name = (String[]) nameList.toArray(new String[0]);
			
			var pric = new int[length];
			for (int i = 0; i < length;i++) {
				pric[i] = pricList.get(i);
			}
			
			var qnty = new int[length];
			for (int i = 0; i < length;i++) {
				qnty[i] = qntyList.get(i);				
			}
			return new Tabla(null,code,name,pric,qnty);
		} catch (IOException e) {
			MainLoader.abort(e);
			return null;
		}
	}
	
	private Tabla readDB(Connection con) {
		int i = 0;
		Statement st = null;
		try {
			st = con.createStatement();
		} catch (SQLException e) {
			MainLoader.abort(e);
		}
		try (var rs = st.executeQuery("SELECT COUNT(*) AS rowcount FROM Inventario")) {
			while(rs.next()) {
				i = rs.getInt("rowcount");
			}
		} catch (SQLException e) {
			if (e.getSQLState().equals("42X05")) {//table doesn't exist
				return new Tabla(null);
			} else MainLoader.abort(e);
		}
		try (var rs = st.executeQuery("SELECT * FROM Inventario")) {
			var codigo = new long[i];
			var nombre = new String[i];
			var precio = new int[i];
			var cantid = new int[i];
			i = 0; 
			while(rs.next()) {
				codigo[i] = rs.getLong("codigo");
				nombre[i] = rs.getString("nombre");
				precio[i] = rs.getInt("precio");
				cantid[i++] = rs.getInt("cantidad");
			}
			rs.close();
			return new Entity(null,codigo,nombre,precio,cantid) {

				@Override
				protected Connection getCon() throws SQLException {
					return con;
				}

				@Override
				protected void abort(SQLException e) {
					MainLoader.abort(e);
				}
				
			};
		} catch (SQLException e) {
			return MainLoader.abort(e);
		}
	}
}
