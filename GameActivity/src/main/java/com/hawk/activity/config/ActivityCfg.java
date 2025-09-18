package com.hawk.activity.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.type.ActivityType;


/**
 * 活动基础配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/activity.xml")
public class ActivityCfg extends HawkConfigBase {
	@Id
	private final int activityId;
	
	/** 活动类型*/
	private final int activityType;
	
	/** 活动名称*/
	private final String activityName;
	
	/** 活动时间展示类型*/
	private final int timeType;
	
	/** 活动是否生效 0：关闭 1:开启*/
	private final int isOpen ;
	
	/** 活动入口类型*/
	private final int entranceType;
	
	/** 是否有登录公告*/
	private final int hasBillBoard;
	
	/** 渠道id限制*/
	private final String channelLimit;
	
	/** 大区id限制*/
	private final String areaLimit;
	
	/** 服务器id限制*/
	private final String serverLimit;
	
	/** 版本号限制*/
	private final String versionLimit;
	
	/** 跨服时是否开启数据获取*/
	private final int isCrossOpen;
	
	/**
	 * 配置版本号
	 */
	private final int cfgVersion;
	
	// 活动类型
	private ActivityType type;
	
	// 允许开放活动的渠道列表,empty表示全渠道开放
	private List<String> channelList;
	
	// 允许开放活动的大区(微信,手Q)列表,empty表示全大区开放
	private List<String> areaList;
	
	// 允许开放活动的服务器id列表
	private List<String> serverList;
	
	private List<Integer> versionLimitInfo;
	/**
	 * 
	 */
	private final boolean checkMergeServer;
	private final boolean checkSeparateServer;
	
	public ActivityCfg() {
		activityId = 0;
		activityName = "";
		activityType = 0;
		timeType = 0;
		isOpen = 0;
		entranceType = 0;
		hasBillBoard = 0;
		channelLimit = "";
		areaLimit = "";
		serverLimit = "";
		versionLimit = "";
		checkMergeServer = false;
		isCrossOpen = 0;
		cfgVersion = 0;
		checkSeparateServer = false;
	}

	public boolean isCheckSeparateServer() {
		return checkSeparateServer;
	}

	@Override
	protected boolean assemble() {
		type = ActivityType.getType(activityType);
		if (type == null) {
			HawkLog.errPrintln("activity type not found! typeCode: {}", activityType);
			return false;
		}
		
		channelList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(channelLimit)) {
			for(String channelId :  channelLimit.split(",")){
				channelList.add(channelId);
			}
		}
		
		areaList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(areaLimit)) {
			for(String area :  areaLimit.split(",")){
				areaList.add(area);
			}
		}
		
		serverList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(serverLimit)) {
			for(String serverId :  serverLimit.split(",")){
				serverList.add(serverId);
			}
		}
		versionLimitInfo = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(versionLimit)) {
			for(String info : versionLimit.split("\\.")){
				versionLimitInfo.add(Integer.valueOf(info));
			}
			if (versionLimitInfo.size() != 4) {
				logger.error("activity versionlimit format error, activityId : {}, activityName : {}, versionLimit : {}", activityId, activityName, versionLimit);
				return false;
			}
		}
		return true;
	}

	public int getActivityId() {
		return activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public boolean isInvalid() {
		return isOpen == 0;
	}
	
	public ActivityType getType() {
		return type;
	}
	
	public int getActivityType() {
		return activityType;
	}
	
	public int getTimeType() {
		return timeType;
	}

	public int getEntranceType() {
		return entranceType;
	}

	public boolean hasBillBoard() {
		return hasBillBoard == 1;
	}

	public List<String> getChannelList() {
		return channelList;
	}

	public List<String> getAreaList() {
		return areaList;
	}

	public List<String> getServerList() {
		return serverList;
	}

	public void setServerList(List<String> serverList) {
		this.serverList = serverList;
	}

	public List<Integer> getVersionLimitInfo() {
		return Collections.unmodifiableList(versionLimitInfo);
	}

	public boolean isCheckMergeServer() {
		return checkMergeServer;
	}
	
	public boolean isCrossOpen() {
		return isCrossOpen == 1;
	}

	public int getCfgVersion() {
		return cfgVersion;
	}
}
