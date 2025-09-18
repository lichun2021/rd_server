package com.hawk.ms.thread.task;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import org.hawk.log.HawkLog;
import com.hawk.ms.MergeServerConfig;
import com.hawk.ms.common.HawkBinLog;
import com.hawk.ms.service.MergeServerService;

public class MainMergeRunnable implements Callable<Boolean> {
	List<String> mergeTableList;
	String masterDbName;
	String slaveDbName;
	Set<String> dirtyIdSet;
	Set<String> newDirtyIdSet;
	ThreadPoolExecutor executor;
	Map<String, Boolean> mergeTableState;
	boolean extraMerge;
	
	public MainMergeRunnable(List<String> mergeTableList, String masterDbName, String slaveDbName, Set<String> dirtyIdSet,
			Set<String> newDirtyIdSet, ThreadPoolExecutor executor, Map<String, Boolean> mergeTableState, boolean extraMerge) {
		this.mergeTableList = mergeTableList;
		this.masterDbName = masterDbName;
		this.slaveDbName = slaveDbName;
		this.dirtyIdSet = dirtyIdSet;
		this.newDirtyIdSet = newDirtyIdSet;
		this.executor = executor;
		this.mergeTableState = mergeTableState;
		this.extraMerge = extraMerge;
	}

	@Override
	public Boolean call() throws Exception {
		MergeServerConfig config = MergeServerConfig.getInstance();
		List<String> extraMergeTables = config.getExtraMergeTableList();
		for (String mergeTable : mergeTableList) {		
			if (!extraMerge && extraMergeTables.contains(mergeTable)) {
				continue;
			}
			
			 if (config.getNotMergeTableList().contains(mergeTable)) {
				 HawkLog.logPrintln("merge ignore table:{}", mergeTable);
				 continue;
			 }
			 
			 if (config.isArgContinue() && getMergeServerState(slaveDbName, mergeTable)) {
				 HawkLog.logPrintln("db:{}, table:{} aleardy merged", slaveDbName, mergeTable);
				 continue;
			 }
			 
			boolean result = MergeServerService.getInstance().mergeTable(masterDbName, slaveDbName, mergeTable, dirtyIdSet, newDirtyIdSet, executor);
			if (!result) {
				HawkLog.logPrintln("merge table fail table:{}", mergeTable);
				HawkBinLog.insertTableFail(slaveDbName, mergeTable);
				return false;
			} else {
				HawkBinLog.insertTableSuccess(slaveDbName, mergeTable);
			}
		}
		
		return true;
	}
	
	public boolean getMergeServerState(String dbName, String tableName) {
		Boolean value = mergeTableState.get(dbName + "_" + tableName);
		if (value == null) {
			return false;
		} else {
			return value.booleanValue();
		}
	}
	
}
