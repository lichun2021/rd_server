package com.hawk.game.config;


import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 攻防模拟战.
 * 
 * @author jm
 *
 */
@HawkConfigManager.KVResource(file = "xml/simulate_war_const.xml")
public class SimulateWarConstCfg extends HawkConfigBase {

	/**
	 * 实例
	 */
	private static SimulateWarConstCfg instance = null;

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static SimulateWarConstCfg getInstance() {
		return instance;
	}

	/**
	 * 总开关
	 */
	private final boolean systemClose;

	/**
	 * 开服时长不足以参加本次活动.
	 */
	private final int serverDelay;
	
	/**
	 * 参战最低大本等级限制
	 */
	private final int cityLvlLimit;

	/**
	 * 每条路的限制人数.
	 */
	private final int memberLimit;
	/**
	 * 最小的出战队伍.
	 */
	private final int minTeamNum;

	/**
	 * 匹配竞争锁有效期(单位:秒)
	 */
	private final int matchLockExpire;
	/**
	 * 匹配准备时间(单位:秒)
	 */
	private final int matchPrepareTime;
	/**
	 * 单次读取工会信息的数量
	 */
	private final int loadGuildSize;
	/**
	 * 单次读取玩家的数量.
	 */
	private final int loadPlayerSize;
	/**
	 * 周期时间.
	 */
	private final int periodTime;
	/**
	 * 最大可以助威次数。
	 */
	private final int maxEncourageTimes;
	/**
	 * 个人最大的出征队伍数.
	 */
	private final int personMaxTeam;
	/**
	 *  match range
	 */
	private final int matching;
	
	public SimulateWarConstCfg() {
		this.systemClose = false;
		this.serverDelay = 0;		
		this.cityLvlLimit = 0;
		this.memberLimit = 0;
		this.matchLockExpire = 10;
		this.matchPrepareTime = 180;		
		this.loadGuildSize = 200;
		this.loadPlayerSize = 30;
		this.minTeamNum = 45;
		this.periodTime = 10;
		this.maxEncourageTimes = 80;
		this.personMaxTeam = 3;
		this.matching = 5;
		
		instance = this;		
	}

	public boolean isSystemClose() {
		return systemClose;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getCityLvlLimit() {
		return cityLvlLimit;
	}

	public int getMemberLimit() {
		return memberLimit;
	}

	public int getMatchLockExpire() {
		return matchLockExpire;
	}

	public int getMatchPrepareTime() {
		return matchPrepareTime;
	}
	
	@Override
	public boolean assemble() {				
		return true;
	}

	public int getLoadGuildSize() {
		return loadGuildSize;
	}

	public int getLoadPlayerSize() {
		return loadPlayerSize;
	}

	public int getMinTeamNum() {
		return minTeamNum;
	}
	
	/**
	 * 周期时间.
	 * @return
	 */
	public int getPeriodTime() {
		return periodTime * 1000;
	}

	public int getMaxEncourageTimes() {
		return maxEncourageTimes;
	}

	public int getPersonMaxTeam() {
		return personMaxTeam;
	}

	public int getMatching() {
		return matching;
	}
}
