package com.hawk.ms.thread.task;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.ms.db.MergeServerSqlSession;

public class ResetDirtPlayerTask implements Callable<Boolean> {
	
	private MergeServerSqlSession sqlSession;
	/**
	 * 垃圾的玩家数据.
	 */
	private List<String> dirtyPlayerIdList;
	
	public ResetDirtPlayerTask(MergeServerSqlSession sqlSession, List<String> dirtyPlayerIdList) {
		this.sqlSession = sqlSession;
		this.dirtyPlayerIdList = dirtyPlayerIdList;
	}
	
	@Override
	public Boolean call() throws Exception {
		if (dirtyPlayerIdList.isEmpty()) {
			return true;
		}
		StringBuilder sb = new StringBuilder("update player set icon = -100 where id in (");
		boolean isFirst = false;
		for (String playerId : dirtyPlayerIdList) {
			if (isFirst) {
				sb.append(",");
			}
			sb.append("'");
			sb.append(playerId);
			sb.append("'");
			isFirst = true;
		}
		sb.append(")");
		PreparedStatement pstmt = null;
		try {
			pstmt = sqlSession.getConnection().prepareStatement(sb.toString());			
			pstmt.executeUpdate();
			
			HawkLog.logPrintln("mark garbage player id :{}", dirtyPlayerIdList);
		} catch (SQLException sqlException) {
			HawkException.catchException(sqlException);			
			HawkLog.errPrintln("execute resetPlayerIdError idList:{}", dirtyPlayerIdList);								
			
			return false;
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}	
		}
		
		return true;
	}

}
