package com.cloudwalk.tool.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import com.cloudwalk.tool.util.DBHelper;
import com.cloudwalk.tool.util.FileUtil;

public class ImageTool {
	private static final String table = "testcollectionvideo";
	private DBHelper dbHelper;
	String root;

	public ImageTool(String url, String driver, String user, String password, String root) {
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
		deleteImage(conn0);
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
				if (!resource.endsWith("_0.txt")) {
					return;
				}
				resource = resource.replaceAll("_0.txt", "");
				System.out.println("start to insert or update " + resource + "  , process = " + getIndex() + "/"
						+ (chidren.length / 3));
				addImage(resource, conn);
				updateImageSrcBase64(resource, conn);
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
				builder.append("create table testcollectionvideo (");
				builder.append("src1 BLOB ,");
				builder.append("base641 CLOB ,");
				builder.append("result varchar2(128),");
				builder.append("info1 varchar2(256),");
				builder.append("info2 varchar2(256),");
				builder.append("base642 CLOB ,");
				builder.append("src2 BLOB ,");
				builder.append("name varchar2(128))");
				PreparedStatement prepareStatement2 = conn0.prepareStatement(builder.toString());
				prepareStatement2.execute();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	

	private void deleteImage(Connection conn) {
		try {
			PreparedStatement prepareStatement = conn.prepareStatement("truncate table " + table);
			prepareStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	public void updateImageSrcBase64(String resource, Connection conn) {
		File file = new File(root + File.separator + resource + "_1.png");
		String base64 = FileUtil.FileToBase64(file);
		File file2 = new File(root + File.separator + resource + "_2.png");
		String base642 = FileUtil.FileToBase64(file2);
		try (PreparedStatement prepareStatement = conn
				.prepareStatement("select src1,base641,src2,base642 from " + table + " where name = ? for update");) {
			prepareStatement.setString(1, resource);

			ResultSet rs = prepareStatement.executeQuery();
			if (rs.next()) {
				oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getBlob("src1");

				OutputStream out = blob.getBinaryOutputStream();
				FileInputStream in = new FileInputStream(file);

				byte[] bs = new byte[2048];
				while (in.read(bs) != -1) {
					out.write(bs);
				}

				oracle.sql.CLOB clob = (oracle.sql.CLOB) rs.getClob("base641");

				Writer writer = clob.getCharacterOutputStream();
				writer.write(base64);

				out.flush();
				writer.flush();

				oracle.sql.BLOB blob2 = (oracle.sql.BLOB) rs.getBlob("src2");

				OutputStream out2 = blob2.getBinaryOutputStream();
				FileInputStream in2 = new FileInputStream(file2);

				byte[] bs2 = new byte[2048];
				while (in2.read(bs2) != -1) {
					out2.write(bs2);
				}

				oracle.sql.CLOB clob2 = (oracle.sql.CLOB) rs.getClob("base642");

				Writer writer2 = clob2.getCharacterOutputStream();
				writer2.write(base642);

				out2.flush();
				writer2.flush();
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
				if (out2 != null) {
					out2.close();
				}
				if (in2 != null) {
					in2.close();
				}
				if (writer2 != null) {
					writer2.close();
				}
			}

		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

	}

	public void addImage(String resource, Connection conn) {
		File file = new File(root + File.separator + resource + "_0.txt");

		try {
			conn.setAutoCommit(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try (PreparedStatement prepareStatement = conn.prepareStatement("insert into " + table
				+ " (name, result, info1, base641, src1, info2, base642, src2) values (?,?,?,empty_clob(),empty_blob(),?,empty_clob(),empty_blob())");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), "utf-8"));) {
			String info1 = reader.readLine();
			reader.readLine();
			String info2 = reader.readLine();
			reader.readLine();
			String result = reader.readLine();

			prepareStatement.setString(1, resource);
			prepareStatement.setString(2, result);
			prepareStatement.setString(3, info1);
			prepareStatement.setString(4, info2);
			prepareStatement.executeUpdate();
			conn.commit();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

}
