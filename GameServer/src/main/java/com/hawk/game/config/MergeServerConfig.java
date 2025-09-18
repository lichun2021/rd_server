package com.hawk.game.config;

import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "cfg/merge/merge_config.cfg")
public class MergeServerConfig extends HawkConfigBase {
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
	 * 检测脏数据.
	 */
	private final String newCheckDirtyTables;
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
	 * 主服中不需要清理垃圾数据的表
	 */
	private final String masterNotDeleteTable;
	/**
	 * 注册时间.
	 */
	private final String registerTime;
	
	private final String lastLogoutTime;
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
	
	private final int jdbcSocketTimeout;
	
	private final String duplicateEntryTable;
	
	/**
	 * {@link MergeServerConfig#saveActivityTable}
	 */
	private List<String> saveActivityTableList;
	
	
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
		masterNotDeleteTable = "activity_card";
		registerTime = "";
		lastLogoutTime = "2018_11_05_00_00_00";
		lastLoginDay = 0;
		filterBuildCfgId = 201010;
		saveType = "2010";
		garbageMarkSize = 10;
		serverId = "";
		deleteActivityHistory = false;
		extraMergeTable = "item,mission,building,custom_data,queue";
		jdbcSocketTimeout = 0;
		duplicateEntryTable = "";
	}
	
	@Override
	public boolean assemble() {
		saveActivityTableList = SerializeHelper.stringToList(String.class, saveActivityTable, ",");
		instance = this;
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return true;
	}

	public boolean isArgContinue() {
		return argContinue;
	}

	public void setArgContinue(boolean argContinue) {
		this.argContinue = argContinue;
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

	public String getDbUsernames() {
		return dbUsernames;
	}

	public String getDbPasswords() {
		return dbPasswords;
	}

	public String getNotMergeTables() {
		return notMergeTables;
	}

	public boolean isSqlBatch() {
		return sqlBatch;
	}

	public String getCheckDirtyTables() {
		return checkDirtyTables;
	}

	public String getNewCheckDirtyTables() {
		return newCheckDirtyTables;
	}

	public String getGarbageTableColumn() {
		return garbageTableColumn;
	}

	public String getGarbageNotDeleteTable() {
		return garbageNotDeleteTable;
	}

	public String getMasterNotDeleteTable() {
		return masterNotDeleteTable;
	}

	public String getRegisterTime() {
		return registerTime;
	}

	public String getLastLogoutTime() {
		return lastLogoutTime;
	}

	public int getLastLoginDay() {
		return lastLoginDay;
	}

	public int getFilterBuildCfgId() {
		return filterBuildCfgId;
	}

	public String getSaveType() {
		return saveType;
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

	public String getExtraMergeTable() {
		return extraMergeTable;
	}

	public List<String> getSaveActivityTableList() {
		return saveActivityTableList;
	}

	public int getJdbcSocketTimeout() {
		return jdbcSocketTimeout;
	}

	public String getDuplicateEntryTable() {
		return duplicateEntryTable;
	}
}
