package com.hawk.activity.type.impl.sendFlower.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/** 机甲觉醒活动K-V配置
 * 
 * @author Jesse */
@HawkConfigManager.KVResource(file = "activity/send_flower/send_flower_cfg.xml")
public class SendFlowerActivityKVCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 */
	private final int serverDelay;

	/** 是否每日重置(零点跨天重置) */
	private final int isDailyReset;

	/** 个人排行数量 */
	private final int selfRankLimit;

	/** 联盟排行数量 */
	private final int guildRankLimit;

	private final int lapiaoCd;// = 3600

	/** 排行刷新周期(毫秒) */
	private final long rankPeriod;
	
	private final int sendItem;

	public SendFlowerActivityKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
		selfRankLimit = 0;
		guildRankLimit = 0;
		rankPeriod = 60000;
		lapiaoCd = 3600;
		sendItem = 0;
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

	public int getLapiaoCd() {
		return lapiaoCd;
	}

	public int getSendItem() {
		return sendItem;
	}

}