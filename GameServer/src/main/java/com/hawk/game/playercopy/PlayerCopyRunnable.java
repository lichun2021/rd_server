package com.hawk.game.playercopy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hawk.db.mysql.result.HawkMysqlResult;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerCopyRunnable {
	/**
	 * 日志打印服务对象
	 */
	static final Logger logger = LoggerFactory.getLogger("Copy");
	/**
	 * 从区的连接
	 */
	PlayerCopySqlSession sqlSession;
	/**
	 * 表名
	 */
	String tableName; 
	/**
	 * 玩家ID
	 */
	String playerId;
	/**
	 * 判断是否是player表
	 */
	boolean playerTable;
	/**
	 * 是否是拷贝玩家
	 */
	boolean playerCopy;
	
	public PlayerCopyRunnable(PlayerCopySqlSession sqlSession, String tableName, String playerId, boolean playerTable, boolean playerCopy) {
		this.sqlSession = sqlSession;
		this.tableName = tableName;
		this.playerId = playerId;
		this.playerTable = playerTable;
		this.playerCopy = playerCopy;
	}

	public HawkTuple2<String, String> execute() throws Exception {
		String checkSql = String.format("select * from %s limit 1", tableName);
		HawkMysqlResult checkResult = sqlSession.executeQuery(checkSql, 0);
		if (checkResult == null || checkResult.getRowCount() <= 0) {
			HawkLog.debugPrintln("player select runnable execute, table {} is empty", tableName);
			return null;
		}
		
		HawkMysqlResult.RowData checkRowData = checkResult.getRowData(0);
		String playerIdValue = (String) checkRowData.getDataByName("playerId");
		String sql = "", deleteSql = "";
		if (!HawkOSOperator.isEmptyString(playerIdValue)) {
			sql = String.format("select * from %s where playerId='%s'", tableName, playerId);
			deleteSql = String.format("delete from %s where playerId='%s'", tableName, playerId);
		} else if (playerTable) {
			sql = String.format("select * from %s where id='%s'", tableName, playerId);
			deleteSql = String.format("delete from %s where id='%s'", tableName, playerId);
		} else {
			HawkLog.debugPrintln("player select runnable execute, table {} not contains playerId attribute", tableName);
			return null;
		}
		
		if (playerCopy) {
			int count = sqlSession.executeUpdateAndGetCount(deleteSql, null, 0);
			if (playerTable && count > 0) {
				return new HawkTuple2<String, String>("", "");
			} else {
				return null;
			}
		}
		
		// 从原表中查指定玩家的数据
		HawkMysqlResult result = sqlSession.executeQuery(sql, 0);
		if (result == null) {
			return null;
		}
		
		if (result.getRowCount() <= 0){
			return null;
		}
		
		String prepareSql = null;	
		StringBuilder sb = new StringBuilder();
		String openid = "", puid = "";
		// 对从原表中查出来的数据进行遍历
		for (int i = 0; i < result.getRowCount(); i++) {
			HawkMysqlResult.RowData rowData = result.getRowData(i);
			if (playerTable) {
				openid = (String) rowData.getDataByName("openid");
				puid = (String) rowData.getDataByName("puid");
			}
			// 拼接sql语句（此处只是拼接字段名），sb参数其实起临时缓存作用
			prepareSql = SQLUtil.generatePrepareStatemSql(tableName, rowData, sb);
			sb.delete(0, sb.length());
			
			PreparedStatement pstmt = null;
			try {
				pstmt = sqlSession.getConnection().prepareStatement(prepareSql);
				// 此处拼接字段值
				SQLUtil.fillData(pstmt, rowData);
				String outsql = pstmt.toString();
				int index = outsql.indexOf("insert into ");
				outsql = outsql.substring(index);
				logger.info(outsql);
			} catch (SQLException sqlException) {
				HawkException.catchException(sqlException);
				return null;
			} finally {
				try {
					if (pstmt != null) {
						pstmt.close();
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}	
			}				
		}					
		
		if (playerTable) {
			return new HawkTuple2<String, String>(openid, puid);
		}
		
		return null;
	}
	
}
