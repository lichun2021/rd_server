package com.hawk.ms.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.mysql.result.HawkMysqlResult;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.util.HawkNumberConvert;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.ms.MergeServerConfig;
import com.hawk.ms.cfg.RegisterPuidCtrl;
import com.hawk.ms.common.HawkBinLog;
import com.hawk.ms.db.MergeServerSqlSession;
import com.hawk.ms.db.SqlFileUtil;
import com.hawk.ms.thread.MergeServerThreadFactory;
import com.hawk.ms.thread.task.MainMergeRunnable;
import com.hawk.ms.thread.task.MergeRunnable;
import com.hawk.ms.thread.task.ResetDirtPlayerTask;


public class MergeServerService {
	
	private Map<String, MergeServerSqlSession> sessionMap = new HashMap<>();
	private ThreadPoolExecutor executor = null;
	private ThreadPoolExecutor extraExecutor = null;
	private ThreadPoolExecutor mainExecutor = null;
	private Map<String, Boolean> mergeTableState = null;
	private Map<String, Set<String>> dbPlayerSet = new HashMap<>();
	private Map<String, String> checkDirtyTableColumnMap = new HashMap<>();
	private Map<String, Set<String>> dirtyPlayerSet = new HashMap<>();
	/**
	 * 白名单中的玩家,
	 */
	private Set<String> puidSet = new HashSet<>();
	
	private MergeServerService() {
		
	}
	
	/**
	 * 初始化连接的一些信息
	 * @param config
	 */
	public boolean init(MergeServerConfig config) {
		//初始化db
		List<String> dbNameList = config.getDbNameList();
		List<String> dbHostList = config.getDbHostList();
		List<String> dbPortList = config.getDbPortsList();
		List<String> dbUsernameList = config.getDbUsernameList();
		List<String> dbPasswordList = config.getDbPasswordList();
		
		for (int i = 0; i < dbNameList.size(); i++) {
			MergeServerSqlSession session = new MergeServerSqlSession();
			String dbUrl = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true", dbHostList.get(i), dbPortList.get(i), dbNameList.get(i));
			boolean rlt = session.init(dbUrl, dbUsernameList.get(i), dbPasswordList.get(i));
			sessionMap.put(dbNameList.get(i), session);
			if (!rlt) {
				HawkLog.errPrintln("init db connection fail url:{}, username:{}, password:{}", dbUrl, dbUsernameList.get(i), dbPasswordList.get(i));
				return false;
			}
			
			rlt = session.getConnection().isValid();
			if(!rlt) {
				HawkLog.errPrintln("init db connection fail url:{}, username:{}, password:{}", dbUrl, dbUsernameList.get(i), dbPasswordList.get(i));
				return false;
			}
		}
		
		//基本从前面继续执行.
		mergeTableState = new HashMap<>();
		if (config.isArgContinue()) {
			try {
				List<String> strList = HawkBinLog.convert2StringList(HawkBinLog.BIN_LOG_PATH);
				strList.stream().forEach(str->{
					String[] strs = str.split(":");
					boolean result = Boolean.valueOf(strs[3]);
					if (result) {
						this.addMergeServerState(strs[1], strs[2], result);
					}					
				});
			} catch (IOException e) {
				HawkException.catchException(e);
				
				return false;
			}
			
		}
		//初始化任务线程池.
		executor = new ThreadPoolExecutor(config.getThreadNum(), config.getThreadNum(), 0l, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new MergeServerThreadFactory("MergeServer"));
		extraExecutor = new ThreadPoolExecutor(config.getThreadNum(), config.getThreadNum(), 0l, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new MergeServerThreadFactory("MergeServerExtra"));
		mainExecutor = new ThreadPoolExecutor(2, 2, 0l, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new MergeServerThreadFactory("mainExecutor"));
		this.checkDirtyTableColumnMap = this.getCheckDirtyDataTablesAndColumn(dbNameList.get(0));
		
		initPuid();
		
		//合服工具用一个特殊的serverId
		HawkUUIDGenerator.prepareUUIDGenerator(MergeServerConfig.getInstance().getServerId());
		
		return true;
	}
	
	public void initPuid() {
		ConfigIterator<RegisterPuidCtrl> iter = HawkConfigManager.getInstance().getConfigIterator(RegisterPuidCtrl.class);
		Set<String> puidSet = new HashSet<>();
		while(iter.hasNext()) {			
			puidSet.add(iter.next().getPuid());
		}
		
		this.puidSet = puidSet;
	}
	
	
	public boolean run() {
		//执行从区需要执行的sql
		boolean executeResult = executeSlaveSql();
		if (!executeResult) {
			return false;
		}
		
		//执行主区特有的sql
		executeResult = executeMasterSql();
		if (!executeResult) {
			return false;
		}
		
		//执行主区从区都要执行的sql
		executeResult = executeAllSql();
		if (!executeResult) {
			return false;
		}
		
		//合并数据
		executeResult = mergeData();
		if (!executeResult) {
			return false;
		}
		
		//改名字之类的功能
		executeResult = executeAfterMergeSql();
		if (!executeResult) {
			return false;
		}
		
		executeResult = updateDuplicationGuildNameTag();
		if(!executeResult) {
			return false;
		}
		
		executeResult = mergeNationalBuild();
		return executeResult;
	}	
	
	/**
	 * 获取区服ID从ID中
	 * @param id
	 * @return
	 */
	public String getServerIdById(String id) {
		if (HawkOSOperator.isEmptyString(id)) {
			throw new RuntimeException("playerId can not be null or empty");
		}
		String[] idPartsArray = id.split("-");
		if (idPartsArray.length != 3) {
			throw new RuntimeException(String.format("playerId:%s is incorrect", id));
		}
		
		//serverId 不能有字符.
		int serverId = HawkNumberConvert.convertInt(idPartsArray[0]);
		
		return serverId + "";
	} 
	
	/**
	 * 合并国家建筑数据
	 * @return
	 */
	private boolean mergeNationalBuild() {
		HawkLog.logPrintln("start merge national build");
		MergeServerConfig config = MergeServerConfig.getInstance();
		List<String> dbNameList = config.getDbNameList();					
		String masterDbName = dbNameList.get(0), slaveDbName = dbNameList.get(1);
		MergeServerSqlSession masterSqlSession = sessionMap.get(masterDbName), slaveSqlSession = sessionMap.get(slaveDbName);
		
		try {
			HawkMysqlResult masterResult = masterSqlSession.executeQuery("select buildingId, level, buildingStatus, buildVal, totalVal, buildTime, createTime, updateTime, invalid from nation_construction", 0);
			HawkMysqlResult slaveResult = slaveSqlSession.executeQuery("select buildingId, level, buildingStatus, buildVal, totalVal, buildTime, createTime, updateTime, invalid from nation_construction", 0);
			
			Map<Integer, Map<String, Object>> masterValMap = new HashMap<>();
			for (int i = 0; i < masterResult.getRowCount(); i++) {
				HawkMysqlResult.RowData rowData = masterResult.getRowData(i);
				int buildingId = (int)rowData.getDataByName("buildingId");
				int level = (int)rowData.getDataByName("level");
				int buildingStatus = (int)rowData.getDataByName("buildingStatus");
				int buildVal = (int)rowData.getDataByName("buildVal");
				int totalVal = (int)rowData.getDataByName("totalVal");
				long buildTime = (long)rowData.getDataByName("buildTime");
				long createTime = (long)rowData.getDataByName("createTime");
				long updateTime = (long)rowData.getDataByName("updateTime");
				boolean invalid = (boolean)rowData.getDataByName("invalid");
				Map<String, Object> masterVal = new HashMap<>();
				masterVal.put("buildingId", buildingId);
				masterVal.put("level", level);
				masterVal.put("buildingStatus", buildingStatus);
				masterVal.put("buildVal", buildVal);
				masterVal.put("totalVal", totalVal);
				masterVal.put("buildTime", buildTime);
				masterVal.put("createTime", createTime);
				masterVal.put("updateTime", updateTime);
				masterVal.put("invalid", invalid);
				masterValMap.put(buildingId, masterVal);
			}
			
			Map<Integer, Map<String, Object>> slaveValMap = new HashMap<>();
			for (int i = 0; i < slaveResult.getRowCount(); i++) {
				HawkMysqlResult.RowData rowData = slaveResult.getRowData(i);
				int buildingId = (int)rowData.getDataByName("buildingId");
				int level = (int)rowData.getDataByName("level");
				int buildingStatus = (int)rowData.getDataByName("buildingStatus");
				int buildVal = (int)rowData.getDataByName("buildVal");
				int totalVal = (int)rowData.getDataByName("totalVal");
				long buildTime = (long)rowData.getDataByName("buildTime");
				long createTime = (long)rowData.getDataByName("createTime");
				long updateTime = (long)rowData.getDataByName("updateTime");
				boolean invalid = (boolean)rowData.getDataByName("invalid");
				Map<String, Object> slaveVal = new HashMap<>();
				slaveVal.put("buildingId", buildingId);
				slaveVal.put("level", level);
				slaveVal.put("buildingStatus", buildingStatus);
				slaveVal.put("buildVal", buildVal);
				slaveVal.put("totalVal", totalVal);
				slaveVal.put("buildTime", buildTime);
				slaveVal.put("createTime", createTime);
				slaveVal.put("updateTime", updateTime);
				slaveVal.put("invalid", invalid);
				slaveValMap.put(buildingId, slaveVal);
			}
			
			Map<Integer, Map<String, Object>> finalValMap = new HashMap<>();
			for (Entry<Integer, Map<String, Object>> entry : masterValMap.entrySet()) {
				int key = entry.getKey();
				Map<String,Object> map = entry.getValue();
				if (!slaveValMap.containsKey(key)) {
					finalValMap.put(key, map);
					continue;
				}
				
				int level = (int) map.get("level");
				int buildVal = (int) map.get("buildVal");
				
				int slaveLevel = (int) slaveValMap.get(key).get("level");
				int slaveBuildVal = (int) slaveValMap.get(key).get("buildVal");
				if (level > slaveLevel) {
					finalValMap.put(key, map);
				} else if (level < slaveLevel) {
					finalValMap.put(key, slaveValMap.get(key));
				} else if (buildVal < slaveBuildVal) {
					finalValMap.put(key, slaveValMap.get(key));
				} else {
					finalValMap.put(key, map);
				}
			}
			
			for (Entry<Integer, Map<String, Object>> entry : slaveValMap.entrySet()) {
				int key = entry.getKey();
				if (finalValMap.containsKey(key)) {
					continue;
				}
				
				Map<String,Object> map = entry.getValue();
				finalValMap.put(key, map);
			}
			
			int count = masterSqlSession.executeUpdateAndGetCount("delete from nation_construction", null, 0);
			HawkLog.logPrintln("merge national build, delete old count: {}, insert info: {}", count, finalValMap);
			for (Entry<Integer, Map<String, Object>> entry : finalValMap.entrySet()) {
				Map<String, Object> map = entry.getValue();
				StringBuilder sb1 = new StringBuilder("insert into nation_construction(");
				StringBuilder sb2 = new StringBuilder(" values(");
				for (Entry<String, Object> entry1 : map.entrySet()) {
					sb1.append(entry1.getKey()).append(",");
					sb2.append(entry1.getValue()).append(",");
				}
				
				sb2.deleteCharAt(sb2.length() - 1).append(");");
				String sql = sb1.deleteCharAt(sb1.length() - 1).append(")").append(sb2.toString()).toString();
				masterSqlSession.executeInsert(sql, null, 0);
			}
						
		} catch (SQLException e) {
			HawkException.catchException(e);
			return false;
		}
		
		HawkLog.logPrintln("end merge national build");
		
		return true;
	}

	private boolean updateDuplicationGuildNameTag() {
		HawkLog.logPrintln("start editDuplicationGuildNameTag");
		MergeServerConfig config = MergeServerConfig.getInstance();
		List<String> dbNameList = config.getDbNameList();					
		String dbName = dbNameList.get(0);
		MergeServerSqlSession sqlSession = sessionMap.get(dbName);
		
		try {
			HawkMysqlResult result = sqlSession.executeQuery("select id, name, tag from guild_info", 0);
			Set<String> nameSet = new HashSet<>();
			Set<String> tagSet = new HashSet<>();
			
			//重名需要修改的map,
			Map<String, Map<String, String>> editMap = new HashMap<>();
			
			for (int i = 0; i < result.getRowCount(); i++) {
				HawkMysqlResult.RowData rowData = result.getRowData(i);
				String id = (String)rowData.getDataByName("id");
				String name = (String)rowData.getDataByName("name");
				String tag = (String)rowData.getDataByName("tag");
				//没有加入成功,则说明重名了.
				Map<String, String> updateKeyValueMap = new HashMap<>();
				String serverId = this.getServerIdById(id);
				if(!nameSet.add(name)) {
					updateKeyValueMap.put("name",  name + "-" + serverId);
					editMap.put(id, updateKeyValueMap);
				}
				
				if (!tagSet.add(tag)) {
					updateKeyValueMap.put("tag",  tag + "-" + serverId);
					editMap.put(id, updateKeyValueMap);
				}
				
			}
			
			HawkLog.logPrintln("update dumpliation guild info:{}", editMap);
			for (Entry<String, Map<String, String>> entry : editMap.entrySet()) {
				StringBuilder sb = new StringBuilder("update guild_info set ");
				boolean isFirst = true;
				for (Entry<String, String> keyValue : entry.getValue().entrySet()) {
					if (!isFirst) {
						sb.append(",");
					}
					sb.append(" ");
					sb.append(keyValue.getKey());
					sb.append("=");
					sb.append("'");
					sb.append(keyValue.getValue());
					sb.append("' ");
					isFirst = false;
				}
				
				sb.append(" where id='");
				sb.append(entry.getKey());
				sb.append("'");
				
				sqlSession.executeUpdate(sb.toString(), 0);
			}
						
		} catch (SQLException e) {
			HawkException.catchException(e);
			
			return false;
		}
		
		HawkLog.logPrintln("end editDuplicationGuildNameTag");
		
		return true;
	}

	private boolean executeAfterMergeSql() {
		//执行主区才执行的sql
		HawkLog.logPrintln("execte after merge sql file start");
		MergeServerConfig config = MergeServerConfig.getInstance();
		List<String> dbNameList = config.getDbNameList();					
		String dbName = dbNameList.get(0);
		MergeServerSqlSession sqlSession = sessionMap.get(dbName);
		try {
			Result<String> result = SqlFileUtil.executeSqlFile(sqlSession, config.getAfterMergeSql());
			HawkLog.logPrintln("execte after merge sql dbName:{}, result:{}", dbName, result.getRetObj());
		} catch (IOException | SQLException e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("execte after merge sql file fail dbName:{}", dbName);
			
			return false;
		}
		
		HawkLog.logPrintln("execte after merge sql file end");
		
		return true;
	}

	private boolean mergeData() {
		//主区
		MergeServerConfig config = MergeServerConfig.getInstance(); 
		String masterDbName =  config.getDbNameList().get(0);
		List<String> slaveList = config.getDbNameList().subList(1, config.getDbNameList().size());
		List<String> tableList = getMergeTableNameList(masterDbName);
		if (tableList == null || tableList.isEmpty()) {
			HawkLog.errPrintln("tableList is null or empty masterDbName:{}", masterDbName);			
			return false;
		}
		
		//查找主区的垃圾数据
		MergeServerSqlSession masterSqlSession = sessionMap.get(masterDbName);
		Set<String> masterDirtyPlayerId = this.findDirtyPlayerIdSet(masterSqlSession, null);
		if (masterDirtyPlayerId == null) {
			HawkLog.errPrintln("find master dirty player error");
			return false;
		} else {
			dirtyPlayerSet.put(masterDbName, masterDirtyPlayerId);
		}
		
		
		for (String slaveDbName : slaveList) {
			HawkLog.logPrintln("merge data start masterDbName:{}, slaveDbName:{}, tableList:{}", masterDbName, slaveDbName, tableList);
			boolean result = mergeDb(masterDbName, slaveDbName, tableList);
			if (!result) {
				HawkLog.logPrintln("merge data fail masterDbName:{}, slaveDbName:{}", masterDbName, slaveDbName);				
				return false;
			} else {
				HawkLog.logPrintln("merget data end masterDbName:{}, slaveDbName:{}", masterDbName, slaveDbName);
			}					
		}
		
		//重置数据失败的还是允许作为成功的, 这不影响合服逻辑
		markDirtyPlayer();		
		
		return true;
	}

	private void markDirtyPlayer() {
		HawkLog.logPrintln("start reset dirty player");
		MergeServerConfig serverConfig = MergeServerConfig.getInstance();
		List<Future<Boolean>> futureList = new ArrayList<>();		
		List<String> dbNameList = serverConfig.getDbNameList();					
		String dbName = dbNameList.get(0);
		MergeServerSqlSession sqlSession = sessionMap.get(dbName);
		for (Entry<String, Set<String>> entry : dirtyPlayerSet.entrySet()) {
			HawkLog.logPrintln("garbage playerId dbName:{}, size:{}", entry.getKey(), entry.getValue().size());
			List<String> garbagePlayerList = new ArrayList<>();			
			for (String playerId : entry.getValue()) {
				garbagePlayerList.add(playerId);
				if (garbagePlayerList.size() >= serverConfig.getGarbageMarkSize()) {
					//复制一份
					Future<Boolean> future = executor.submit(new ResetDirtPlayerTask(sqlSession, new ArrayList<>(garbagePlayerList)));
					futureList.add(future);
					garbagePlayerList.clear();
				}							
			}
			
			if (!garbagePlayerList.isEmpty()) {
				Future<Boolean> future = executor.submit(new ResetDirtPlayerTask(sqlSession, new ArrayList<>(garbagePlayerList)));
				futureList.add(future);
				garbagePlayerList.clear();
			}					
		} 
		
		for (Future<Boolean> future : futureList) {
			while(!future.isDone()) {															
				HawkOSOperator.osSleep(5);										
			}			
		}
		
		HawkLog.logPrintln("end mark dirty player");
	}

	/**
	 * 删除不合并的互动表
	 * @param str
	 * @return
	 */
	private List<String> getDeleteActivityTable(String masterDbName) {
		MergeServerConfig config = MergeServerConfig.getInstance();
		MergeServerSqlSession sqlSession = sessionMap.get(masterDbName);
		List<String> tableList = sqlSession.selectTableList(masterDbName);
		if (tableList == null) {
			return null;
		}
		List<String> deleteList = new ArrayList<>();
		for (String table : tableList) {
			if (table.indexOf(config.getActivityPrefix()) >= 0 && !config.getSaveActivityTableList().contains(table)) {
				deleteList.add(table);
			}			
		}
		
		return deleteList;
	}
	
	/**
	 * 找到需要检测脏数据表
	 * @param dbName
	 * @return
	 */
	public Map<String, String> getCheckDirtyDataTablesAndColumn(String dbName) {
		MergeServerConfig config = MergeServerConfig.getInstance();
		MergeServerSqlSession sqlSession = sessionMap.get(dbName);
		List<String> tableList = sqlSession.selectTableList(dbName);
		if (tableList == null) {
			return null;
		}
		
		Map<String, String> tableColumnMap = config.getCheckDirtyTablesMap();
		Map<String, String> checkMap = new HashMap<>();
		for (String table : tableList) {
			if (table.indexOf(config.getActivityPrefix()) >= 0 && config.getSaveActivityTableList().contains(table)) {
				checkMap.put(table, "playerId");
			}			
			
			if (tableColumnMap.containsKey(table)) {
				checkMap.put(table, tableColumnMap.get(table));
			}
		}
		
		return checkMap;
	}
	
	/**
	 * 需要合并的表
	 * @param masterDbName
	 * @return
	 */
	private List<String> getMergeTableNameList(String masterDbName) {
		//这里先把需要操作的表查出来
		MergeServerConfig config = MergeServerConfig.getInstance();
		MergeServerSqlSession sqlSession = sessionMap.get(masterDbName);
		List<String> tableList = sqlSession.selectTableList(masterDbName);
		List<String> mergeTableList = new ArrayList<>();
		for (String table : tableList) {
			if (table.indexOf(config.getActivityPrefix()) >= 0 && !config.getSaveActivityTableList().contains(table)) {
				continue;
			}
			mergeTableList.add(table);
		}
		
		return mergeTableList;
	}
	
	/**
	 * 
	 * @param masterDbName
	 * @param slaveDbName
	 * @param mergeTableList
	 * @return
	 */
	private  boolean mergeDb(String masterDbName, String slaveDbName, List<String> mergeTableList) {
		MergeServerConfig config = MergeServerConfig.getInstance();
		MergeServerSqlSession slaveSqlSession = sessionMap.get(slaveDbName);
		Set<String> dirtyIdSet = null;
		Set<String> newDirtyIdSet = new HashSet<>();
		try {
			Set<String> idSet = this.selectAllPlayerId(slaveSqlSession);
			dbPlayerSet.put(slaveDbName, idSet);
			dirtyIdSet = this.findDirtyPlayerIdSet(slaveSqlSession, newDirtyIdSet);
			if (dirtyIdSet == null) {
				return false;
			}
			dirtyPlayerSet.put(slaveDbName, dirtyIdSet);
		} catch (Exception e) {
			HawkException.catchException(e);
			
			return false;
		}
		
		List<String> extraMergeTablesCopy = new ArrayList<>(config.getExtraMergeTableList());
		long startTime = HawkTime.getMillisecond();
		Future<Boolean> future1 =  mainExecutor.submit(new MainMergeRunnable(mergeTableList, masterDbName, slaveDbName, 
				dirtyIdSet, newDirtyIdSet, executor, mergeTableState, false));
		Future<Boolean> future2 =  mainExecutor.submit(new MainMergeRunnable(extraMergeTablesCopy, masterDbName, slaveDbName, 
				dirtyIdSet, newDirtyIdSet, extraExecutor, mergeTableState, true));
		boolean future1Wait = true, future2Wait = true;
		while (future1Wait || future2Wait) {
			try {
				if (future1Wait && future1.isDone()) {
					future1Wait = false;
					if (!future1.get().booleanValue()) {
						return false;
					}
					HawkLog.logPrintln("executor execute time: {}", HawkTime.getMillisecond() - startTime);
				}
				
				if (future2Wait && future2.isDone()) {
					future2Wait = false;
					if (!future2.get().booleanValue()) {
						return false;
					}
					HawkLog.logPrintln("extraExecutor execute time: {}", HawkTime.getMillisecond() - startTime);
				}
				
				HawkOSOperator.osSleep(5);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return true;
	}
	
	
	
	/**
	 * 表合并
	 * @param masterDbName
	 * @param slaveDbName
	 * @param mergeTable
	 * @return
	 */
	public boolean mergeTable(String masterDbName, String slaveDbName, String mergeTable, Set<String> dirtyPlayerSet, Set<String> newDirtyPlayerSet, ThreadPoolExecutor executor) {
		MergeServerSqlSession masterSqlSession = sessionMap.get(masterDbName);
		MergeServerSqlSession slaveSqlSession = sessionMap.get(slaveDbName);
		int batchSize = MergeServerConfig.getInstance().getBatchSize();
		long startTime = System.nanoTime();
		try {
			int slaveTableCount = slaveSqlSession.queryTableCount(mergeTable);
			if (slaveTableCount < 0) {
				HawkLog.errPrintln("table count less than 0 tableName:{}", mergeTable);
				return false;
			} else if (slaveTableCount == 0) {
				HawkLog.errPrintln("no need merge table count is zero tableName:{}", mergeTable);
				return true;
			}
			
			int beforeMasterTableCount = masterSqlSession.queryTableCount(mergeTable);
			if (beforeMasterTableCount < 0) {
				HawkLog.errPrintln("before merge master table count less than 0 may be some error occur");
				return false;
			}
			
			int count = 0;
			Set<String> playerIdSet = dbPlayerSet.get(slaveDbName);
			HawkLog.logPrintln("tableName:{} tableCount:{} start merge", mergeTable, slaveTableCount);
			List<Future<Boolean>> futureList = new ArrayList<>();
			int singleTaskSize = (slaveTableCount - 1) / executor.getCorePoolSize() + 1; 
			batchSize = singleTaskSize > batchSize ? batchSize : singleTaskSize;
			AtomicInteger countDirtyData = new AtomicInteger();
			AtomicInteger newCountDirtyData = new AtomicInteger();
			
			for (int begin = 0; begin < slaveTableCount; begin = begin + batchSize) {
				count = Math.min(batchSize, slaveTableCount - begin);				
				Future<Boolean> future =  executor.submit(new MergeRunnable(masterSqlSession, slaveSqlSession, mergeTable, 
						begin, count, playerIdSet, countDirtyData, dirtyPlayerSet, newCountDirtyData, newDirtyPlayerSet));
				futureList.add(future);
			}
			for (Future<Boolean> future : futureList) {
				while(!future.isDone()) {															
					HawkOSOperator.osSleep(5);										
				}
				Boolean result = future.get();
				if (!result.booleanValue()) {
					HawkLog.errPrintln("slave db insert into master db error table:{}", mergeTable);
					return false;
				}
			}
			
			int afterMasterTableCount = masterSqlSession.queryTableCount(mergeTable);
			/** edit by codej 2019-12-18 加入垃圾数据清理之后之前的一些判定依据就有问题了.
			if (afterMasterTableCount < 0) {
				HawkLog.errPrintln("after merge master table count less than 0 may be some error occur");
				return false;
			}
			
			if (afterMasterTableCount - beforeMasterTableCount != slaveTableCount - countDirtyData.get()) {
				HawkLog.errPrintln("afterMergeTableCount - beforeMergeTableCount != mergeTableCount, after:{},before:{}, slave:{}, dirtyData:{}",
						afterMasterTableCount, beforeMasterTableCount, slaveTableCount, countDirtyData.get());
				return false;
			} else {
				HawkLog.logPrintln("afterMergeTableCount - beforeMergeTableCount == mergeTableCount, after:{},before:{}, slave:{}, dirtyData:{}",
						afterMasterTableCount, beforeMasterTableCount, slaveTableCount, countDirtyData.get());
			}*/
			HawkLog.logPrintln("afterMergeTableCount - beforeMergeTableCount == mergeTableCount, "
					+ "tableName: {}, after:{}, before:{}, slave:{}, dirtyData:{}, newDirtyDataCount: {}, newDirtyPlayerCount: {}",
					mergeTable, afterMasterTableCount, beforeMasterTableCount, slaveTableCount, countDirtyData.get(), 
					newCountDirtyData.get(), newDirtyPlayerSet.size());
			
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("merge table  fail tableName:{}", mergeTable);
			return false;
		}
		
		long endTime = System.nanoTime();
		HawkLog.logPrintln("merge table:{} costTime:{}", mergeTable, (endTime - startTime) / 1_000_000_000);
		
		return true;
	}	

	private boolean executeAllSql() {
		//执行主区才执行的sql
		HawkLog.logPrintln("execte all sql file start");
		MergeServerConfig config = MergeServerConfig.getInstance();
		List<String> dbNameList = config.getDbNameList();
		List<String> deleteList = this.getDeleteActivityTable(dbNameList.get(0));
		if (deleteList.isEmpty()) {
			HawkLog.errPrintln("can not find delete table list dbName:{}", dbNameList.get(0));
			return false;
		}
		for (int i = 0; i < dbNameList.size(); i ++) {
			String dbName = dbNameList.get(i);
			MergeServerSqlSession sqlSession = sessionMap.get(dbName);
			try {
				Result<String> result = SqlFileUtil.executeSqlFile(sqlSession, config.getAllExecuteSql());
				HawkLog.logPrintln("execte all sql dbName:{}, result:{}", dbName, result.getRetObj());
			} catch (IOException | SQLException e) {
				HawkException.catchException(e);
				HawkLog.errPrintln("execte all sql file fail dbName:{}", dbName);
				
				return false;
			}					
		}
		
		HawkLog.logPrintln("execte all sql file end");
		
		return true;
	}

	private boolean executeMasterSql() {
		//执行主区才执行的sql
		HawkLog.logPrintln("execte master sql file start");
		MergeServerConfig config = MergeServerConfig.getInstance();
		List<String> dbNameList = config.getDbNameList();					
		String dbName = dbNameList.get(0);
		MergeServerSqlSession sqlSession = sessionMap.get(dbName);
		List<String> deleteList = this.getDeleteActivityTable(dbNameList.get(0));
		if (deleteList.isEmpty()) {
			HawkLog.errPrintln("can not find delete table list dbName:{}", dbNameList.get(0));
			return false;
		}
		
		try {
			Result<String> result = SqlFileUtil.executeSqlFile(sqlSession, config.getMasterSql());
			HawkLog.logPrintln("execte master sql dbName:{}, result:{}", dbName, result.getRetObj());
		} catch (IOException | SQLException e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("execte master sql file fail dbName:{}", dbName);
			
			return false;
		}
		
		for (String deleteTable : deleteList) {
			try {
				HawkLog.logPrintln("delete table tableName:{} start", deleteTable);
				int deleteCount = sqlSession.executeUpdateAndGetCount("delete from " + deleteTable, null, 0);
				HawkLog.logPrintln("delete table tableName:{}, deleteCount:{}", deleteTable, deleteCount);
			} catch (Exception e) {
				HawkException.catchException(e);
				HawkLog.errPrintln("dlete table fail tableName:{}", deleteTable);					
				return false;
			}
		}
		
		String sql = null;
		String inParam = null;
		try {
			List<String> playerIdServerList = this.selectAllPlayerIdServerPrefix(sqlSession);
			if (playerIdServerList.isEmpty()) {
				HawkLog.warnPrintln("select serverId is empty");
				return true;
			}
			
			inParam = strList2InParam(playerIdServerList);
		} catch(Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("select serverId by playerid fail");
			return false;
		}
		
		for (Entry<String, String> entry : checkDirtyTableColumnMap.entrySet()) {
			try {
				sql = "delete from " + entry.getKey() + " where substring(`"+entry.getValue()+"`,1,3)";				
				sql = sql + " not in " + inParam;				
				HawkLog.logPrintln("clean master dirty table:{} sql:{}, start", entry, sql);				
				int deleteCount = sqlSession.executeUpdateAndGetCount(sql, null, 0);
				HawkLog.logPrintln("clean master dirty table:{}, deleteCount:{}", entry.getKey(), deleteCount);
			} catch (Exception e) {
				HawkException.catchException(e);
				HawkLog.errPrintln("clean master dirty  fail tableName:{}", entry.getKey());					
				return false;
			}
		}
		
		HawkLog.logPrintln("execte master sql file end");
		
		return true;
	}

	public boolean executeSlaveSql() {
		//执行从区才执行的sql
		HawkLog.logPrintln("execte slave sql start");
		MergeServerConfig config = MergeServerConfig.getInstance();
		List<String> dbNameList = config.getDbNameList();			
		for (int i = 1; i < dbNameList.size(); i ++) {
			String dbName = dbNameList.get(i);
			MergeServerSqlSession sqlSession = sessionMap.get(dbName);
			try {
				Result<String> result = SqlFileUtil.executeSqlFile(sqlSession, config.getSlaveSql());
				HawkLog.logPrintln("execute slave sql dbName:{}, result :{}", dbName, result.getRetObj());
			} catch (IOException | SQLException e) {
				HawkException.catchException(e);
				HawkLog.errPrintln("execte sql file fail dbName:{}", dbName);
				
				return false;
			}
		}
		
		HawkLog.logPrintln("execte slave sql end");
		
		return true;
	}
	
	public static MergeServerService getInstance() {
		return MergeServerServiceHolder.instance;
	}
	
	static class MergeServerServiceHolder {
		static MergeServerService instance = new MergeServerService();
	}	
	
	public static String rowDataotoString(String tableName, HawkMysqlResult.RowData rowData, StringBuilder sb) {
		boolean flag = false;
		if (sb.length() <= 0) {
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
				if (columnData.getValue() instanceof String) {
					valueBuilder.append("'");
					valueBuilder.append(((String)columnData.getValue()).replaceAll("\\\\", "\\\\\\\\"));
					valueBuilder.append("'");
				} else {
					valueBuilder.append(columnData.getValue());
				}
				flag = true;
			}
			sb.append(")");
			valueBuilder.append(");");
			sb.append(" values");
			sb.append(valueBuilder);
		} else {
			sb.append(",");
			for (HawkMysqlResult.ColumnData columnData : rowData.colItems) {
				if (flag) {
					sb.append(",");
				}
				if (columnData.getValue() instanceof String) {
					sb.append("'");
					sb.append(((String)columnData.getValue()).replaceAll("\\\\", "\\\\\\\\"));
					sb.append("'");
				} else {
					sb.append(columnData.getValue());
				}
				flag = true;
			}
		}		
		
		return sb.toString();
	}
	
	public void addMergeServerState(String dbName, String tableName, boolean value) {
		mergeTableState.put(dbName + "_" + tableName, value);
	}
	
	public String strList2InParam(List<String> strList) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		boolean flag = false;
		for (String str : strList) {
			if (flag) {
				sb.append(",");
			}
			sb.append("'" + str + "'");
			flag = true;
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	public List<String> selectAllPlayerIdServerPrefix(MergeServerSqlSession session) throws SQLException {
		String sql = "select distinct(substring(id,1,3)) as serverId from player";
		HawkMysqlResult result = session.executeQuery(sql, 0);
		List<String> serverList = new ArrayList<>();
		if (result == null) {
			return new ArrayList<>(); 
		}
		
		for (int i = 0; i < result.getRowCount(); i++ ) {
			HawkMysqlResult.RowData rowData = result.getRowData(i);
			String serverId = (String)rowData.getDataByName("serverId");
			serverList.add(serverId);
		}
		
		return serverList;
	}   
	
	public Set<String> selectAllPlayerId(MergeServerSqlSession session) throws SQLException {
		String sql = "select id from player ";
		Set<String> playerIdSet = new HashSet<>(20000);
		HawkMysqlResult result = session.executeQuery(sql, 0);
		if (result == null) {
			return playerIdSet; 
		}
		
		for (int i = 0; i < result.getRowCount(); i++ ) {
			HawkMysqlResult.RowData rowData = result.getRowData(i);
			String id = (String)rowData.getDataByName("id");
			playerIdSet.add(id);
		}
		
		return playerIdSet; 
	}

	public Map<String, String> getCheckDirtyTableColumnMap() {
		return checkDirtyTableColumnMap;
	}
	
	/**
	 * 找到当前库的垃圾数据。
	 * @param session
	 * @return
	 */
	public Set<String> findDirtyPlayerIdSet(MergeServerSqlSession session, Set<String> newDirtyIdSet) {
		MergeServerConfig serverConfig = MergeServerConfig.getInstance();
		if (serverConfig.getRegisterTimeLong() <= 0) {
			return new HashSet<>();
		}
			
		long startTime = HawkTime.getMillisecond();
		Set<String> dirtyPlayerIdSet = new HashSet<>();		
		try {
			long filterLastLogoutTime = serverConfig.getLastLogoutTimeLong();
			// 找到x日期前创建的角色, 最早的登录时间. （在***时间之前注册，且在***时间之后再未登录过的玩家，如果是白名单账号除外）
			if (filterLastLogoutTime <= 0) {
				filterLastLogoutTime = HawkTime.getMillisecond() - serverConfig.getLastLoginDay() * HawkTime.DAY_MILLI_SECONDS; 
			}
			String playerSql = String.format("select id,openid from player where createTime < %s and logoutTime < %s", 
					serverConfig.getRegisterTimeLong(), filterLastLogoutTime);
			HawkMysqlResult  playerResult = session.executeQuery(playerSql, 0);			
			for (int i = 0; i < playerResult.getRowCount(); i++ ) {
				HawkMysqlResult.RowData rowData = playerResult.getRowData(i);
				String id = (String)rowData.getDataByName("id");
				String openid = (String)rowData.getDataByName("openid");
				//在白名单中的玩家,不算是垃圾数据。
				if (puidSet.contains(openid)) {
					continue;
				}
				dirtyPlayerIdSet.add(id);
			}
			
			if (newDirtyIdSet != null) {
				String sql = String.format("select id from player where logoutTime < %s", serverConfig.getLastLogoutTimeLong());
				HawkMysqlResult  result = session.executeQuery(sql, 0);			
				for (int i = 0; i < result.getRowCount(); i++) {
					HawkMysqlResult.RowData rowData = result.getRowData(i);
					String id = (String)rowData.getDataByName("id");
					newDirtyIdSet.add(id);
				}
			}
			
			
			//过滤大本小于等于x等级的玩家, 大于10本的玩家较少，所以这里取大于10本的玩家.
			String buildingSql = String.format("select playerId from building where type = 2010 and buildingCfgId > %s", serverConfig.getFilterBuildCfgId());
			HawkMysqlResult buildResult = session.executeQuery(buildingSql, 0);			
			for (int i = 0; i < buildResult.getRowCount(); i++ ) {
				HawkMysqlResult.RowData rowData = buildResult.getRowData(i);
				String id = (String)rowData.getDataByName("playerId");				
				dirtyPlayerIdSet.remove(id);
				if (newDirtyIdSet != null) {
					newDirtyIdSet.remove(id);
				}
			}
			
			//有充值的玩家.
			String rechargeSql = "select distinct(playerId) as playerId from recharge";
			HawkMysqlResult rechargeResult = session.executeQuery(rechargeSql, 0);			
			for (int i =0; i < rechargeResult.getRowCount(); i++ ) {
				HawkMysqlResult.RowData rowData = rechargeResult.getRowData(i);
				String id = (String)rowData.getDataByName("playerId");
				
				dirtyPlayerIdSet.remove(id);
			}
								
			return dirtyPlayerIdSet;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			HawkLog.logPrintln("find dirty player set costtime: {}", HawkTime.getMillisecond() - startTime);
		} 			
		
		return null;
	}
}
