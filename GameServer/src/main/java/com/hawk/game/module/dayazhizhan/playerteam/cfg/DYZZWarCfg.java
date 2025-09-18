package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.GsConfig;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 端午-联盟庆典配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "xml/dyzz_const.xml")
public class DYZZWarCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	
	private final int fortLevel;
	
	private final int teamMemberCount;
	
	private final String mapSize;
	
	private final int rewardlimittimes;
	
	/**
	 * 战场活跃状态检测周期
	 */
	private final int perioTime;
	
	/**
	 * 战场开启时间(单位:秒)
	 */
	private final int warOpenTime;
	
	/**
	 * 邀请CD
	 */
	private final int inviteCD;
	
	/**
	 * 奖励获取积分限制
	 */
	private final int negativeintegral;
	
	/**
	 * 历史战绩条数
	 */
	private final int recordtimes;
	
	/**
	 * 强制迁回时间.
	 */
	private final int forceMoveBackTime;
	
	/**
	 *回原服最小时间,
	 */
	private final int minBackServerWaitTime;
	/**
	 *回原服最大时间,
	 */
	private final int maxBackServerWaitTime;
	
	
	private final String limitServer;
	
	
	private List<String> limitServerList;
	
	public DYZZWarCfg() {
		serverDelay = 0;
		teamMemberCount = 0;
		fortLevel = 0;
		perioTime = 10000;
		warOpenTime = 0;
		forceMoveBackTime = 30000;
		mapSize = "";
		rewardlimittimes = 0;
		minBackServerWaitTime = 1000;
		maxBackServerWaitTime = 5000;
		limitServer = "";
		inviteCD = 0;
		recordtimes = 1;
		negativeintegral = 0;
	}

	
	@Override
	protected boolean assemble() {
		limitServerList = SerializeHelper.stringToList(String.class, limitServer, SerializeHelper.BETWEEN_ITEMS);
		return super.assemble();
	}
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getFortLevel() {
		return fortLevel;
	}

	public int getTeamMemberCount() {
		return teamMemberCount;
	}

	public int getPerioTime() {
		return perioTime;
	}

	public int getWarOpenTime() {
		return warOpenTime;
	}

	public int getForceMoveBackTime() {
		return forceMoveBackTime;
	}

	public String getMapSize() {
		return mapSize;
	}

	public int getRewardlimittimes() {
		return rewardlimittimes;
	}

	public int getMinBackServerWaitTime() {
		return minBackServerWaitTime;
	}

	public int getMaxBackServerWaitTime() {
		return maxBackServerWaitTime;
	}

	
	
	public int getInviteCD() {
		return inviteCD;
	}


	public int getRecordtimes() {
		return recordtimes;
	}


	public int getNegativeintegral() {
		return negativeintegral;
	}
	
	
	public boolean isOpen(){
		if(this.limitServerList == null){
			return true;
		}
		if(this.limitServerList.isEmpty()){
			return true;
		}
		String serverId = GsConfig.getInstance().getServerId();
		if(this.limitServerList.contains(serverId)){
			return true;
		}
		return false;
	}
	
	
	
	
	
}
