package com.cloudwalk.tool.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {
	public String url;
	public String diver;
	public String user;
	public String password;

	public DBHelper(String url, String driver, String user, String password) {
		try {
			this.url = url;
			this.diver = driver;
			this.user = user;
			this.password = password;
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConn() {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}