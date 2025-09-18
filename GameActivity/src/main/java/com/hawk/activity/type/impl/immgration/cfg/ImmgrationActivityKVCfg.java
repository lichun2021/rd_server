package com.hawk.activity.type.impl.immgration.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

@HawkConfigManager.KVResource(file = "activity/migrant/migrant_cfg.xml")
public class ImmgrationActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/**
	 * 道具id
	 */
	private final int item;

	/**
	 * 不能移民去的新服保护时间：单位：秒
	 */
	private final int newServerProtect;

	/**
	 * 注册多少天以前
	 */
	private final int registerDay;
	
	/**
	 * 流失多少天以上
	 */
	private final int lostDay;
	
	/**
	 * 可移民的前后天数
	 */
	private final int migrantDay;
	
	/**
	 * 哪些服开启，0全部开，多个服务器直接用,分割。
	 */
	private final String openServer;
	
	/**
	 * 这个时间之后有登录过
	 */
	private final String activeTimeStart;
	
	/**
	 * 需要的VIP等级
	 */
	private final int vipLevel;

	/**
	 * 最小消耗道具数量
	 */
	private final int itemNumMin;
	
	/**
	 * 开启服务器(灰度用)
	 */
	private List<String> openServerList;
	
	public ImmgrationActivityKVCfg() {
		serverDelay = 0;
		item = 0;
		newServerProtect = 7776000;
		registerDay = 730;
		lostDay = 180;
		migrantDay = 100;
		openServer = "";
		activeTimeStart = "";
		vipLevel = 6;
		itemNumMin = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getItem() {
		return item;
	}

	public long getNewServerProtect() {
		return newServerProtect * 1000L;
	}

	public int getRegisterDay() {
		return registerDay;
	}

	public int getLostDay() {
		return lostDay;
	}

	public int getMigrantDay() {
		return migrantDay;
	}

	public String getOpenServer() {
		return openServer;
	}

	public String getActiveTimeStart() {
		return activeTimeStart;
	}

	public int getVipLevel() {
		return vipLevel;
	}
	
	public int getItemNumMin() {
		return itemNumMin;
	}

	public List<String> getOpenServerList() {
		return openServerList;
	}
	
	@Override
	protected boolean assemble() {
		List<String> openServerList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(openServer) && !openServer.equals("0")) {
			String[] split = openServer.split(",");
			for (int i = 0; i < split.length; i++) {
				openServerList.add(split[i]);
			}
		}
		this.openServerList = openServerList;
		return super.assemble();
	}
}
