package com.cloudwalk.tool.idcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import com.cloudwalk.tool.util.DBHelper;
import com.cloudwalk.tool.util.FileUtil;

public class IDCardBackTool {
	private static final String table = "testcollectionidcardback";
	private DBHelper dbHelper;
	String root;

	public IDCardBackTool(String url, String driver, String user, String password, String root) {
		this.dbHelper = new DBHelper(url, driver, user, password);
		this.root = root;
	}

	private int index;

	private synchronized int getIndex() {
		return index++;
	}

	public void uploadAll() {
		Connection conn0 = dbHelper.getConn();
		createTableIfNotExists(conn0);
		delete(conn0);
		if (conn0 != null) {
			try {
				conn0.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File rootFile = new File(root);
		String[] chidren = rootFile.list();
		if (chidren != null) {
			System.out.println("data count = " + chidren.length);
			this.index = 1;
			Connection conn = dbHelper.getConn();
			Arrays.asList(chidren).stream().forEach(resource -> {
				System.out.println(
						"start to insert or update " + resource + "  , process = " + getIndex() + "/" + chidren.length);
				add(resource, conn);
				update(resource, conn);
			});
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	private void createTableIfNotExists(Connection conn0) {

		try {
			PreparedStatement prepareStatement = conn0
					.prepareStatement("select count(*) counts from user_tables where table_name=upper(?) ");
			prepareStatement.setString(1, table);

			ResultSet rs = prepareStatement.executeQuery();
			if (rs.next()) {
				int counts = rs.getInt("counts");
				if (counts != 0) {
					return;
				}
			}
			
			StringBuilder builder = new StringBuilder();
			builder.append("create table testcollectionidcardback (");
			builder.append("src BLOB ,");
			builder.append("base64 CLOB ,");
			builder.append("name varchar2(128))");
			PreparedStatement prepareStatement2 = conn0.prepareStatement(builder.toString());
			prepareStatement2.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void delete(Connection conn) {
		try {
			PreparedStatement prepareStatement = conn.prepareStatement("truncate table " + table);
			prepareStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	public void update(String resource, Connection conn) {
		File file = new File(root + File.separator + resource);
		String base64 = FileUtil.FileToBase64(file);
		String name = file.getName();
		try (PreparedStatement prepareStatement = conn
				.prepareStatement("select src,base64 from " + table + " where name = ? for update");) {
			prepareStatement.setString(1, name);

			ResultSet rs = prepareStatement.executeQuery();
			if (rs.next()) {
				oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getBlob("src");

				OutputStream out = blob.getBinaryOutputStream();
				FileInputStream in = new FileInputStream(file);

				byte[] bs = new byte[2048];
				while (in.read(bs) != -1) {
					out.write(bs);
				}

				oracle.sql.CLOB clob = (oracle.sql.CLOB) rs.getClob("base64");

				Writer writer = clob.getCharacterOutputStream();
				writer.write(base64);

				out.flush();
				writer.flush();
				conn.commit();

				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (writer != null) {
					writer.close();
				}
			}

		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

	}

	public void add(String resource, Connection conn) {
		File file = new File(root + File.separator + resource);
		String name = file.getName();

		try {
			conn.setAutoCommit(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try (PreparedStatement prepareStatement = conn.prepareStatement(
				"insert into " + table + " (name, base64, src) values (?,empty_clob(),empty_blob())")) {

			prepareStatement.setString(1, name);
			prepareStatement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
