package com.hawk.ms.db;

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
import org.hawk.db.mysql.result.HawkMysqlResult;
import org.hawk.os.HawkException;
import org.hawk.reflect.HawkReflectManager;

import com.hawk.ms.util.SQLUtil;

public class MergeServerSqlSession extends HawkMysqlSession {
	/**
	 * 记录一下行数
	 * @param sql
	 * @param params
	 * @param timeout
	 * @return
	 * @throws SQLException
	 */
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
	
	public  List<String> selectTableList(String dbName){
		
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
				tableNameList.add(rs.getString("TABLE_NAME"));				
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
	
	/**
	 * 自己判断是不是活动表
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 */
	public int queryInvalidTermId(String tableName) throws SQLException {
		HawkMysqlResult result = this.executeQuery("select max(termId) from " + tableName, 0);
		if (result == null) {
			return 0;
		}
		
		HawkMysqlResult.RowData rowData = result.getRowData(0);
		Object obj = rowData.colItems.get(0).getValue();
		int termId = 0;
		if (obj instanceof Integer) {
			termId = (Integer)obj;
		} else {
			termId = ((Long)obj).intValue();
		}		 
		
		return termId;
	}
	/**
	 * 主要是为了计算活动那里的数据。
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 */
	public int queryTableCountFilterDirtyData(String tableName) throws SQLException {
		boolean isActiivty = SQLUtil.isActiivtyDataTable(tableName);
		HawkMysqlResult result = null;
		if (isActiivty) {
			int termId = this.queryInvalidTermId(tableName);
			String sql = String.format("select count(1) from %s where termId >= %s", tableName, termId);
			result = this.executeQuery(sql, 0); 
		} else {
			result = this.executeQuery("select count(1) from " + tableName, 0);
		}
		
		if (result == null) {
			return - 1;
		}
		
		HawkMysqlResult.RowData rowData = result.getRowData(0);
		
		return ((Long)(rowData.colItems.get(0).getValue())).intValue();
	}
	/**
	 * 偷懒就放这里了
	 * @throws SQLException 
	 * 
	 */	
	public int queryTableCount(String tableName) throws SQLException {
		HawkMysqlResult result = this.executeQuery("select count(1) from " + tableName, 0);
		if (result == null) {
			return - 1;
		}
		
		HawkMysqlResult.RowData rowData = result.getRowData(0);
		
		return ((Long)(rowData.colItems.get(0).getValue())).intValue();
	}
}
