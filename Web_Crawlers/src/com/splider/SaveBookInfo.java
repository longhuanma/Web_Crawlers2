package com.splider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mysql.jdbc.Statement;

public class SaveBookInfo {
	public static final String url = "jdbc:mysql://localhost:3306/mlh?autoReconnect=true";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "123456";

	public Connection conn = null;
	public Statement smt = null;

	public Statement saveBookInfo() {
		try {
			Class.forName(name);// 指定连接类型
			conn = DriverManager.getConnection(url, user, password);// 获取连接
			smt = (Statement) conn.createStatement();// 准备执行语句
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return smt;
	}

	
}
