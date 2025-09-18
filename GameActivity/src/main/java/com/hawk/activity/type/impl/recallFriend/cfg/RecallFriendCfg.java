package com.hawk.activity.type.impl.recallFriend.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.KVResource;

@KVResource(file = "activity/recall_friend/recall_friend_const_cfg.xml")
public class RecallFriendCfg extends HawkConfigBase {
	private final int lostTime;
	private final int vip;
	private final int level;
	private final int battlePoint;
	private final int buildlevel;
	private final int serverDelay;
	private final int recallNumLimit;
	/**
	 * 默认可以触发几个人的召回任务.
	 */
	private final int triggerRecallNumLimit;
	/**
	 *  召回盟友时间判定条件，单位：秒
	 */
	private final int allianceRecallTime;
	/**
	 * 召回盟友刷新时间
	 */
	private final long allianceRefreshTime;

	/**
	 * 盟友接收次数上限，每日刷新
	 */
	private final int receiveLimit;
	/**
	 * 召回列表显示人数
	 */
	private final int showNumber;
	/**
	 * 被召回玩家大本等级限制
	 */
	private final int baseLimit;

	private static RecallFriendCfg instance;
	public static RecallFriendCfg getInstance() {
		return instance;
	}
	
	public RecallFriendCfg() {
		this.lostTime = 0;
		this.vip = 0;
		this.level = 0;
		this.battlePoint = 0;
		this.buildlevel = 0;
		this.serverDelay = 0;
		this.recallNumLimit = 0;
		this.triggerRecallNumLimit = 3;
		this.allianceRecallTime = 0;
		this.allianceRefreshTime = 0;
		this.receiveLimit = 0;
		this.showNumber = 0;
		this.baseLimit = 0;
		
		instance = this;
	}
	
	public long getServerDealy() {
		return serverDelay  * 1000l;
	}
	
	public int getLostTime() {
		return lostTime;
	}
	public int getVip() {
		return vip;
	}
	public int getLevel() {
		return level;
	}
	public int getBattlePoint() {
		return battlePoint;
	}
	public int getBuildlevel() {
		return buildlevel;
	}

	public int getRecallNumLimit() {
		return recallNumLimit;
	}
	
	public int getTriggerRecallNumLimit() {
		return triggerRecallNumLimit;
	}

	public int getAllianceRecallTime() {
		return allianceRecallTime;
	}

	public long getAllianceRefreshTime() {
		return allianceRefreshTime * 1000L;
	}

	public int getReceiveLimit() {
		return receiveLimit;
	}

	public int getShowNumber() {
		return showNumber;
	}

	public int getBaseLimit() {
		return baseLimit;
	}
}
