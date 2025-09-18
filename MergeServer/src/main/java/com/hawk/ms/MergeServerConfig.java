package com.hawk.ms;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.app.HawkAppCfg;
import org.hawk.config.HawkConfigManager.KVResource;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.ms.common.Constants;
import com.hawk.ms.util.StringUtil;

@KVResource(files ={"cfg/app.cfg", "cfg/mergeDb.cfg"})
public class MergeServerConfig extends HawkAppCfg {
	/**
	 * 单次操作mysql的size
	 */
	private final int batchSize;
	/**
	 * 线程数量
	 */
	private final int threadNum;
	/**
	 * 主区执行的sql
	 */
	private final String masterSql;
	/**
	 * 从区执行的sql
	 */
	private final String slaveSql;
	
	/**
	 * 主从区都要执行的Sql. 
	 */
	private final String allExecuteSql;
	
	/**
	 * 合完之后执行的sql 
	 */
	private final String afterMergeSql;
	/**
	 * 活动表的前缀.
	 */
	private final String activityPrefix;	
	/**
	 * 需要保留的活动表.
	 */
	private final String saveActivityTable;
	/**
	 * 数据库的名字
	 */
	private final String dbNames;
	/**
	 * 主机
	 */
	private final String dbHosts;
	/**
	 * 端口
	 */
	private final String dbPorts;
	/**
	 * 数据库的用户名
	 */
	private final String dbUsernames;
	/**
	 * 密码
	 */
	private final String dbPasswords;
	/**
	 * 不合并的表
	 */
	private final String notMergeTables;
	
	/**
	 * 数据库名
	 * {@link MergeServerConfig#dbNames}
	 */
	private List<String> dbNameList;
	/**
	 * {@link MergeServerConfig#dbHosts}
	 */
	private List<String> dbHostList;
	/**
	 * {@link MergeServerConfig#dbPorts}
	 */
	private List<String> dbPortsList;
	/**
	 * 用户名
	 */
	private List<String> dbUsernameList;
	/**
	 * {@link MergeServerConfig#dbPasswords}
	 */
	private List<String> dbPasswordList;
	
	/**
	 * {@link MergeServerConfig#saveActivityTable}
	 */
	private List<String> saveActivityTableList;
	/**
	 * 非合并从区表.
	 */
	private List<String> notMergeTableList;
	/**
	 * sql是否批量插入
	 */
	private final boolean sqlBatch;
	/**
	 * 命令行传过来的是否继续合并
	 */
	private boolean argContinue;
	/**
	 * 检测脏数据.
	 */
	private final String checkDirtyTables;
	
	/**
	 * 检测脏数据
	 * {tableNma, columnName} 
	 */
	private Map<String, String> checkDirtyTablesMap;
	
	/**
	 * 检测脏数据.
	 */
	private final String newCheckDirtyTables;
	
	/**
	 * 检测脏数据
	 * {tableNma, columnName} 
	 */
	private Map<String, String> newCheckDirtyTablesMap;
	
	/**
	 * 匹配脏表的数据. 只要有一个column 属于垃圾playerId则也当垃圾数据.
	 * tableName_column1_column2;
	 */
	private final String garbageTableColumn;
	/**
	 * 虽然是垃圾数据，但是不清理.
	 */
	private final String garbageNotDeleteTable;
	
	/**
	 * {@link #garbageTableColumn}
	 */
	private Map<String, List<String>> garbageTableColumnMap;
	/**
	 * {@link #garbageNotDeleteTable}
	 */
	private Set<String> garbageNotDeleteTableSet;
	/**
	 * 注册时间.
	 */
	private final String registerTime;
	
	private final String lastLogoutTime;
	/**
	 * long类型的注册时间。
	 */
	private long registerTimeLong;
	
	private long lastLogoutTimeLong;
	/**
	 * 最后一次登录时间.
	 */
	private final int lastLoginDay;
	/**
	 * 小于此配置ID的玩家.
	 */
	private final int filterBuildCfgId;
	/**
	 * 垃圾玩家数据,需要保留的建筑表.
	 */
	private final String saveType;
	/**
	 * {@link #saveType}
	 */
	private List<Integer> saveTypeList;
	/**
	 * 单次标记玩家的个数
	 */
	private final int garbageMarkSize;
	/**
	 * serverId
	 */
	private final String serverId;
	
	/**
	 * 活动的历史数据是否需要合并. 
	 */
	private final boolean deleteActivityHistory;
	/**
	 * 额外处理合并的表
	 */
	private final String extraMergeTable;
	private List<String> extraMergeTableList;
	
	static MergeServerConfig instance = null;
	
	public static MergeServerConfig getInstance() {
		return instance;
	}
	public MergeServerConfig() {
		batchSize = 5000;
		threadNum = 8;
		masterSql = "";
		slaveSql = "";
		allExecuteSql = "";
		afterMergeSql = "";
		activityPrefix = "";
		saveActivityTable = "";
		dbNames = "";
		dbHosts = "";
		dbUsernames = "";
		dbPasswords = "";
		dbPorts = "";
		sqlBatch = false;
		notMergeTables = "";
		checkDirtyTables = "";
		newCheckDirtyTables = "item,playerId;building,playerId;mission,playerId;custom_data,playerId;queue,playerId;talent,playerId;army,playerId";
		garbageTableColumn = "";
		garbageNotDeleteTable = "";
		registerTime = "";
		lastLogoutTime = "2018_11_05_00_00_00";
		lastLoginDay = 0;
		filterBuildCfgId = 201010;
		this.saveType = "2010";
		this.garbageMarkSize = 10;
		serverId = "";
		this.deleteActivityHistory = false;
		extraMergeTable = "item,mission,building,custom_data,queue";
		instance = this;
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public String getMasterSql() {
		return masterSql;
	}

	public String getSlaveSql() {
		return slaveSql;
	}

	public String getAllExecuteSql() {
		return allExecuteSql;
	}

	public String getAfterMergeSql() {
		return afterMergeSql;
	}

	public String getActivityPrefix() {
		return activityPrefix;
	}

	public String getSaveActivityTable() {
		return saveActivityTable;
	}
	public String getDbNames() {
		return dbNames;
	}
	public String getDbHosts() {
		return dbHosts;
	}
	public String getDbPorts() {
		return dbPorts;
	}
	public String getDbPasswords() {
		return dbPasswords;
	}
	public List<String> getDbNameList() {
		return dbNameList;
	}
	public List<String> getDbHostList() {
		return dbHostList;
	}
	public List<String> getDbPortsList() {
		return dbPortsList;
	}
	public List<String> getDbPasswordList() {
		return dbPasswordList;
	}
	
	public List<String> getDbUsernameList() {
		return dbUsernameList;
	}
	
	@Override
	public boolean assemble() {
		dbNameList = StringUtil.str2List(dbNames, Constants.AttributeCutter, String.class);
		dbHostList = StringUtil.str2List(dbHosts, Constants.AttributeCutter, String.class);
		dbPortsList = StringUtil.str2List(dbPorts, Constants.AttributeCutter, String.class);
		dbUsernameList = StringUtil.str2List(dbUsernames, Constants.AttributeCutter, String.class);
		dbPasswordList = StringUtil.str2List(dbPasswords, Constants.AttributeCutter, String.class);
		saveActivityTableList = StringUtil.str2List(saveActivityTable, Constants.AttributeCutter, String.class);
		notMergeTableList = StringUtil.str2List(notMergeTables, Constants.AttributeCutter, String.class);
		checkDirtyTablesMap = StringUtil.str2Map(checkDirtyTables, Constants.RowSpliter, Constants.ColumnSpliter, String.class, String.class);
		newCheckDirtyTablesMap = StringUtil.str2Map(newCheckDirtyTables, Constants.RowSpliter, Constants.ColumnSpliter, String.class, String.class);
		
		if (!HawkOSOperator.isEmptyString(extraMergeTable)) {
			extraMergeTableList = StringUtil.str2List(extraMergeTable, Constants.AttributeCutter, String.class);
		} else {
			extraMergeTableList = Collections.emptyList();
		}
		
		if (HawkOSOperator.isEmptyString(garbageTableColumn)) {
			garbageTableColumnMap = new HashMap<>();
		} else {
			//table_name,columnName1,columnName2
			Map<String, List<String>> localGarbageTableColumnMap = new HashMap<>();
			String[] tableItemList = garbageTableColumn.split(";");
			for (String tableItem : tableItemList) {
				String[] nameArray = tableItem.split(",");
				List<String> strList = new ArrayList<>();
				for (int i = 1; i < nameArray.length; i ++) {
					strList.add(nameArray[i]);
				}
				
				localGarbageTableColumnMap.put(nameArray[0], strList);
			}
			
			this.garbageTableColumnMap = localGarbageTableColumnMap;
		}
		
		if (HawkOSOperator.isEmptyString(garbageNotDeleteTable)) {
			garbageNotDeleteTableSet = new HashSet<>();
		} else {
			Set<String> localGarbageNotDeleteTableSet = new HashSet<>();
			String[] tableArray = garbageNotDeleteTable.split(",");
			localGarbageNotDeleteTableSet.addAll(Arrays.asList(tableArray));
			
			this.garbageNotDeleteTableSet = localGarbageNotDeleteTableSet;
		}
		
		if (HawkOSOperator.isEmptyString(registerTime)) {
			registerTimeLong = 0l;
		} else {
			registerTimeLong = HawkTime.parseTime(registerTime, "yyyy_MM_dd_HH_mm_ss");
		}
		
		if (HawkOSOperator.isEmptyString(lastLogoutTime)) {
			lastLogoutTimeLong = 0;
		} else {
			lastLogoutTimeLong = HawkTime.parseTime(lastLogoutTime, "yyyy_MM_dd_HH_mm_ss");
		}
		saveTypeList = StringUtil.str2List(saveType, "_", Integer.class);
		
		return true;
	}
	
	public List<String> getExtraMergeTableList() {
		return extraMergeTableList;
	}
	@Override
	public boolean checkValid() {
		if (dbNameList.size() <= 1) {
			throw new InvalidParameterException("dbNames must be large than one");
		}
		
		if (dbNameList.size() != new HashSet<>(dbNameList).size()) {
			throw new InvalidParameterException("dbNames has same db name");
		}
		
		checkSize("dbHost", dbHostList, dbNameList);
		checkSize("dbPort", dbPortsList, dbNameList);
		checkSize("dbUsername", dbUsernameList, dbNameList);
		checkSize("dbPassowrd", dbPasswordList, dbNameList);
		
		//测试完成之后把下面的数据弄成和dbName一一对应。
		if (dbHostList.size() != dbNameList.size()) {
			fillList(dbHostList, dbNameList);
		}
		if (dbPortsList.size() != dbNameList.size()) {
			fillList(dbPortsList, dbNameList);
		}
		
		if (dbUsernameList.size() != dbNameList.size()) {
			fillList(dbUsernameList, dbNameList);
		}
		
		if (dbPasswordList.size() != dbNameList.size()) {
			fillList(dbPasswordList, dbNameList);
		}
		
		return true;
	}
	
	private void fillList(List<String> paramList, List<String> dbNameList) {
		String sourceData = paramList.get(0);
		for (int i = 1; i < dbNameList.size(); i++) {
			paramList.add(sourceData);			
		}
	}
	
	private void checkSize(String msg, List<String> paramList, List<String> dbNameList) {
		if (paramList.size() != 1 && paramList.size() != dbNameList.size()) {
			throw new InvalidParameterException(msg +" size must be one or equal to dbNameList");
		}
	}
	public List<String> getSaveActivityTableList() {
		return saveActivityTableList;
	}
	public boolean isSqlBatch() {
		return sqlBatch;
	}
	public List<String> getNotMergeTableList() {
		return notMergeTableList;
	}
	public boolean isArgContinue() {
		return argContinue;
	}
	public void setArgContinue(boolean argContinue) {
		this.argContinue = argContinue;
	}
	public Map<String, String> getCheckDirtyTablesMap() {
		return checkDirtyTablesMap;
	}
	
	public Map<String, String> getNewCheckDirtyTablesMap() {
		return newCheckDirtyTablesMap;
	}
	
	public Map<String, List<String>> getGarbageTableColumnMap() {
		return garbageTableColumnMap;
	}
	public Set<String> getGarbageNotDeleteTableSet() {
		return garbageNotDeleteTableSet;
	}
	public long getRegisterTimeLong() {
		return registerTimeLong;
	}
	public int getLastLoginDay() {
		return lastLoginDay;
	}
	public int getFilterBuildCfgId() {
		return filterBuildCfgId;
	}
	public List<Integer> getSaveTypeList() {
		return saveTypeList;
	}
	public int getGarbageMarkSize() {
		return garbageMarkSize;
	}
	public String getServerId() {
		return serverId;
	}
	public boolean isDeleteActivityHistory() {
		return deleteActivityHistory;
	}
	public long getLastLogoutTimeLong() {
		return lastLogoutTimeLong;
	}
	
}
