package com.hawk.activity.type.impl.machineAwakeTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 机甲觉醒2(年兽)活动K-V配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/machine_awake_two/machine_awake_two_cfg.xml")
public class MachineAwakeTwoActivityKVCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 */
	private final int serverDelay;

	/** 是否每日重置(零点跨天重置) */
	private final int isDailyReset;

	/** 个人排行数量 */
	private final int selfRankLimit;

	/** 联盟排行数量 */
	private final int guildRankLimit;

	/** 排行刷新周期(毫秒) */
	private final long rankPeriod;
	private final int sharePoint;
	public MachineAwakeTwoActivityKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
		selfRankLimit = 0;
		guildRankLimit = 0;
		rankPeriod = 60000;
		sharePoint = 2000;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isDailyReset() {
		return isDailyReset == 1;
	}

	public int getSelfRankLimit() {
		return selfRankLimit;
	}

	public int getGuildRankLimit() {
		return guildRankLimit;
	}

	public long getRankPeriod() {
		return rankPeriod;
	}

	public int getSharePoint() {
		return sharePoint;
	}

}