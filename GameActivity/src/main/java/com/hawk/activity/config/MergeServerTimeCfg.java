package com.hawk.activity.config;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/merge_server_time.xml")
public class MergeServerTimeCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int mergeType;
	/**
	 * 合并的区服列表.
	 */
	private final String mergeServers;
	/**
	 * 合服时间.
	 */
	private final String mergeTime;
	/**
	 * 主服
	 */
	private final String masterServer;
	/**
	 * 从服
	 */
	private final String slaveServers;
	/**
	 * 合服时间
	 * {@link MergeServerTimeCfg#mergeTime}
	 */
	private long mergeTimeValue;
	/**
	 * 合并区服列表
	 * {@link MergeServerTimeCfg#mergeServers}
	 */
	private List<String> mergeServerList;
	/**
	 * 从服
	 */
	private List<String> slaveServerIdList;
	
	private static Map<String, List<String>> mainSlaveServerMap = new HashMap<>();
	
	public MergeServerTimeCfg() {
		id = 0;
		mergeType = 0;
		mergeServers = "";
		mergeTime = "";
		masterServer = "";
		slaveServers = "";
	}
	
	@Override
	public boolean assemble() {
		this.mergeServerList = SerializeHelper.stringToList(String.class, mergeServers, SerializeHelper.ATTRIBUTE_SPLIT);
		if (mergeServerList.isEmpty() || !mergeServerList.get(0).equals(masterServer)) {
			HawkLog.errPrintln("MergeServerTimeCfg assemble failed, id: {}, masterServer: {}, mergeServerList: {}", id, masterServer, mergeServerList);
			return false;
		}
		this.mergeTimeValue = HawkTime.parseTime(mergeTime);
		this.slaveServerIdList = SerializeHelper.stringToList(String.class, slaveServers, SerializeHelper.ATTRIBUTE_SPLIT);
		if (slaveServerIdList.isEmpty() || slaveServerIdList.contains(masterServer)) {
			HawkLog.errPrintln("MergeServerTimeCfg assemble failed, id: {}, masterServer: {}, slaveServerIdList: {}", id, masterServer, slaveServerIdList);
			return false;
		}
		mainSlaveServerMap.put(masterServer, slaveServerIdList);
		return true;
	}

	public int getId() {
		return id;
	}

	public int getMergeType() {
		return mergeType;
	}
	
	public long getMergeTimeValue() {
		return mergeTimeValue;
	}

	public List<String> getMergeServerList() {
		return mergeServerList;
	}

	public String getMasterServer() {
		return masterServer;
	}

	public List<String> getSlaveServerIdList() {
		return slaveServerIdList;
	}

	@Override
	public boolean checkValid() {
		if (this.getMergeTimeValue() < HawkTime.getMillisecond()) {
			return true;
		}
		//起服的时候这个dataGeter还没有注入,而且起服校验会走另外一套.
		if (ActivityManager.getInstance() == null) {
			return true;
		}
		if (ActivityManager.getInstance().getDataGeter() == null) {
			return true;
		}
		boolean result = ActivityManager.getInstance().getDataGeter().checkMergeServerTimeWithCrossTime(this);
		if (!result) {
			throw new InvalidParameterException("合服配置和跨服配置有问题");
		}
		return true;
	}
	
	public static List<String> getSlaveServerIds(String mainServer) {
		return mainSlaveServerMap.getOrDefault(mainServer, Collections.emptyList());
	}
}
