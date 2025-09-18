package com.hawk.game.playercopy;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hawk.db.mysql.result.HawkMysqlResult;

public class SQLUtil {
	
	public static void fillData(PreparedStatement pstmt, HawkMysqlResult.RowData rowData) throws SQLException {
		int i = 1;
		for (HawkMysqlResult.ColumnData columnData : rowData.colItems) {
			pstmt.setObject(i++, columnData.getValue());
		}
	}
	
	public static String generatePrepareStatemSql(String tableName, HawkMysqlResult.RowData rowData, StringBuilder sb) {
		boolean flag = false;		
		sb.append("insert into " + tableName);
		sb.append("(");			
		StringBuilder valueBuilder = new StringBuilder();
		valueBuilder.append("(");
		for (HawkMysqlResult.ColumnData columnData : rowData.colItems) {
			if (flag) {
				sb.append(",");
				valueBuilder.append(",");
			}
			sb.append(columnData.getName());
			valueBuilder.append("?");
			flag = true;
		}
		sb.append(")");
		valueBuilder.append(");");
		sb.append(" values");
		sb.append(valueBuilder);							
		
		return sb.toString();
	}
	
}
