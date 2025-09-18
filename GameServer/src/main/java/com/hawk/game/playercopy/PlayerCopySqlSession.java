package com.hawk.game.playercopy;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hawk.db.mysql.HawkMysqlConnection;
import org.hawk.db.mysql.HawkMysqlSession;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.reflect.HawkReflectManager;

public class PlayerCopySqlSession extends HawkMysqlSession {
	
	public int executeUpdateAndGetCount(String sql, Object[] params, int timeout) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = this.getConnection().prepareStatement(sql);
			if (params != null) {
				int costtime = 1;
				Object[] arg7 = params;
				int arg8 = params.length;

				for (int arg9 = 0; arg9 < arg8; ++arg9) {
					Object o = arg7[arg9];
					stmt.setObject(costtime++, o);
				}
			}

			if (timeout > 0) {
				stmt.setQueryTimeout(timeout / 1000);
			}

			return stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	/**
	 * 获取数据库表名
	 * 
	 * @param dbName
	 * @return
	 */
	public List<String> selectTableList(String dbName){
		return selectTableList(dbName, null, null);
	}
	
	/**
	 * 获取数据库表名，同时获取一些特殊的表
	 * 
	 * @param dbName
	 * @param specialKeyNameTable 主键字段名为playerId的表
	 * @param specialKeyIndexTable 主键字段不是第一个字段的表
	 * @return
	 */
	public List<String> selectTableList(String dbName, List<String> specialKeyNameTable, List<String> specialKeyIndexTable){
		List<String> tableNameList = new ArrayList<String>();
		try {
			HawkMysqlConnection conn = getConnection();
			Field field = HawkReflectManager.getClassField(HawkMysqlConnection.class, "connection");
			if (field == null) {
				return tableNameList;
			}
			Connection connection = (Connection)field.get(conn);
			DatabaseMetaData dmd = connection.getMetaData();
			
			ResultSet rs = dmd.getTables(null, dbName, null, new String[]{"TABLE"});
			while(rs.next()){
				try {
					//column1: 50001_trunk, column2: null, column3: activity_airdrop_supply, column4: TABLE, column5: , column6: null
					if (rs.getString(1).indexOf(dbName) >= 0) {
						tableNameList.add(rs.getString("TABLE_NAME"));				
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			try {
				for (String tableName : tableNameList) {
					if (!tableName.startsWith("activity_")) {
						HawkLog.logPrintln("player copy selectTableList, game tableName: {}", tableName);
					}
					ResultSet prs = dmd.getPrimaryKeys(null, dbName, tableName);
					while(prs.next()){
						String columnName = (String) prs.getObject(4);
						int keyIndex = (int) prs.getObject(5);
						HawkLog.debugPrintln("find primaryKey, tableName: {}, columnName: {}, KEY_SEQ: {}", prs.getObject(3), columnName, keyIndex);
						if (keyIndex != 1) {
							HawkLog.debugPrintln("find primaryKey special table, tableName: {}", tableName);
							if (specialKeyIndexTable != null) {
								specialKeyIndexTable.add(tableName);
							}
						}
						
						if (columnName.equals("playerId")) {
							HawkLog.debugPrintln("find primaryKey special column, tableName: {}, columnName: {}", tableName, columnName);
							if (specialKeyNameTable != null) {
								specialKeyNameTable.add(tableName);
							}
						}
					}
				}
			} catch (Exception e) {
			}
			
			return tableNameList;
		} catch (SQLException e) {
			HawkException.catchException(e);
		} catch (IllegalArgumentException e) {
			HawkException.catchException(e);
		} catch (IllegalAccessException e) {
			HawkException.catchException(e);
		}
		
		return null;
	}
	
}
