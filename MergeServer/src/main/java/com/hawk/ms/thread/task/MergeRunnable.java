package com.hawk.ms.thread.task;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.hawk.db.mysql.result.HawkMysqlResult;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import com.hawk.ms.MergeServerConfig;
import com.hawk.ms.db.MergeServerSqlSession;
import com.hawk.ms.service.MergeServerService;
import com.hawk.ms.thread.ThreadStringBuilder;
import com.hawk.ms.util.SQLUtil;

public class MergeRunnable implements Callable<Boolean> {
	/**
	 * 主区的连接
	 */
	MergeServerSqlSession masterSqlSession;
	/**
	 * 从区的连接
	 */
	MergeServerSqlSession slaveSqlSession;
	/**
	 * 表名
	 */
	String tableName; 
	/**
	 * 下标
	 */
	private int begin;
	/**
	 * 计数
	 */
	private int count;
	/**
	 * 
	 */
	private Set<String> playerIdSet;
	/**
	 * 记录脏数据。
	 */
	AtomicInteger dirtyDataCounter;
	/**
	 * 删除的玩家.
	 */
	Set<String> dirtyPlayerSet;
	
	/**
	 * 记录脏数据。
	 */
	AtomicInteger newDirtyDataCounter;
	/**
	 * 删除的玩家.
	 */
	Set<String> newDirtyPlayerSet;
	
	public MergeRunnable(MergeServerSqlSession masterSqlSession, MergeServerSqlSession slaveSqlSession, String tableName, 
			int begin, int count, Set<String> playerIdSet, AtomicInteger dirtyDataCounter, Set<String> dirtyPlayerSet,
			AtomicInteger newDirtyDataCounter, Set<String> newDirtyPlayerSet) {
		this.masterSqlSession = masterSqlSession;
		this.slaveSqlSession = slaveSqlSession;
		this.tableName = tableName;
		this.begin = begin;
		this.count = count;
		this.playerIdSet = playerIdSet;
		this.dirtyDataCounter = dirtyDataCounter;
		this.dirtyPlayerSet = dirtyPlayerSet;
		this.newDirtyDataCounter = newDirtyDataCounter;
		this.newDirtyPlayerSet = newDirtyPlayerSet;
	}

	@Override
	public Boolean call() throws Exception {
		HawkLog.logPrintln("merge data task start table:{}, begin:{}, count:{}", tableName, begin, count);
		String sql = String.format("select * from %s limit %s, %s", tableName, begin, count);
		HawkMysqlResult result = slaveSqlSession.executeQuery(sql, 0);
		if (result == null) {
			return false;
		}
		//外面自己拦住0就别请求了.
		if (result.getRowCount() <= 0){
			return false;
		}
		
		HawkLog.logPrintln("merge data task load data finish table:{} tableName", tableName);
		String prepareSql = null;						
		StringBuilder sb = ThreadStringBuilder.getStringBuilder();
		boolean dirtyColunNull = false;
		MergeServerConfig config = MergeServerConfig.getInstance();
		String checkDirtyColumnName = MergeServerService.getInstance().getCheckDirtyTableColumnMap().get(tableName);
		Set<String> garbageNotDeleteTableSet = config.getGarbageNotDeleteTableSet();
		Map<String, List<String>> tableDirtyColumn = config.getGarbageTableColumnMap();
		
		Map<String, String> newCheckDirtyTablesMap = config.getNewCheckDirtyTablesMap();
		boolean isNewCheckDirtyTable = newCheckDirtyTablesMap.containsKey(tableName);
		String newCheckDirtyTableColumn = newCheckDirtyTablesMap.get(tableName);
		
		//没有就默认以playerId
		List<String> dirtyColumnList = tableDirtyColumn.getOrDefault(tableName, Arrays.asList("playerId"));		
		List<Integer> buildingSaveTypeList = config.getSaveTypeList();			
		boolean isGarbageNotDeleteTable = garbageNotDeleteTableSet.contains(tableName);
		//是否是活动相关的.
		boolean isActiivty = SQLUtil.isActiivtyDataTable(tableName);
		//是否是建筑相关.
		boolean isBuilding = SQLUtil.isBuildingTable(tableName);		
		String defaultDirtyColumnName = "playerId";		
		int termId = 0;
		if (config.isDeleteActivityHistory() && isActiivty) {
			termId = slaveSqlSession.queryInvalidTermId(tableName);
		}		
		for (int i = 0; i < result.getRowCount(); i++) {
			HawkMysqlResult.RowData rowData = result.getRowData(i);
			
			// 这里对需要统计脏数据量级的表进行判断
			if (isNewCheckDirtyTable) {
				Object object = rowData.getDataByName(newCheckDirtyTableColumn);
				if (object != null && newDirtyPlayerSet.contains(object.toString())) {
					newDirtyDataCounter.incrementAndGet();
				}
			}
			
			if (!HawkOSOperator.isEmptyString(checkDirtyColumnName)) {
				Object object = rowData.getDataByName(checkDirtyColumnName);
				if (object == null ) {
					if (!dirtyColunNull) {
						dirtyColunNull = true;
						HawkLog.errPrintln("talbeName:{}, checkDirtyData columnName {} data is null", tableName, checkDirtyColumnName);
					}						
				} else {
					//玩家数据不在player里面
					if (!playerIdSet.contains(object.toString())) {
						dirtyDataCounter.incrementAndGet();
						continue;
					} 											
				}
			}
			if (prepareSql == null) {
				prepareSql = SQLUtil.generatePrepareStatemSql(tableName, rowData, sb);
				sb.delete(0, sb.length());
			}			
			//建筑表特殊处理.
			if (!dirtyPlayerSet.isEmpty() && !isGarbageNotDeleteTable) {
				if (isBuilding) {
					Integer type = (Integer)rowData.getDataByName("type");
					String playerId = (String) rowData.getDataByName(defaultDirtyColumnName);
					//如果是垃圾并且不是大本则数据全部丢弃.
					if (dirtyPlayerSet.contains(playerId) && !buildingSaveTypeList.contains(type)) {
						continue;
					}
				} else if (isActiivty) {
					//活动开头的那些表也需要特殊处理.
					if (config.isDeleteActivityHistory() && filterActiivtyDirty(rowData, dirtyPlayerSet, termId)) {
						continue;
					}													
				} else {
					boolean needDiscard = false;					
					for (String dirtyColumn : dirtyColumnList) {
						//因为都是玩家ID过滤， 那么被过滤的字段必然也是和玩家ID字段一致。
						String dirtyColumnValue = (String)rowData.getDataByName(dirtyColumn);
						if (dirtyPlayerSet.contains(dirtyColumnValue)) {
							needDiscard = true;
							break;
						}						
					}
					
					if (needDiscard) {
						continue;
					}
				}
			} 
						
			PreparedStatement pstmt = null;
			try {
				pstmt = masterSqlSession.getConnection().prepareStatement(prepareSql);
				SQLUtil.fillData(pstmt, rowData);
				pstmt.executeUpdate();
			} catch (SQLException sqlException) {
				HawkException.catchException(sqlException);
				String insertSql = pstmt == null ? "create pstmt error" : pstmt.toString();
				HawkLog.errPrintln("execute insert error sql:{} errorMsg:{}", insertSql, sqlException.getMessage());								
				
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
		}					
		
		HawkLog.logPrintln("merge data task insert finish table:{}", tableName);
		
		return true;
	}
	

	/**
	 * 
	 * @param rowData
	 * @param activityPlayerMaxTermDataMap
	 * @param dirtyPlayerSet
	 * @return true 该数据不要
	 */
	public boolean filterActiivtyDirty(HawkMysqlResult.RowData rowData, Set<String> dirtyPlayerSet, int invalidTermId) {
		String defaultDirtyColumnName = "playerId";
		Object  activityPlayerIdObject = rowData.getDataByName(defaultDirtyColumnName);
		if (activityPlayerIdObject != null) {
			String playerId = (String)activityPlayerIdObject;
			//垃圾玩家,数据不合并。
			if (dirtyPlayerSet.contains(playerId)) {
				return true;
			} else {
				//非垃圾玩家把玩家的期数和玩家ID,存入等所有的
				Integer curDataTermId = (Integer)rowData.getDataByName("termId");
				
				return invalidTermId > curDataTermId ; 
			}
		}
		
		return false;
	}
}
