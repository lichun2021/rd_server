package com.hawk.game.president;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;

import com.hawk.game.GsApp;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.President.PresidentEvent;
import com.hawk.game.protocol.President.PresidentEventType;
import com.hawk.game.protocol.President.PresidentHistory;
import com.hawk.game.service.GuildService;

/**
 * 国王战相关历史记录
 * 
 * @author hawk
 *
 */
public class PresidentRecord {
	/**
	 * 全局实例对象
	 */
	static PresidentRecord instance = null;

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static PresidentRecord getInstance() {
		if (instance == null) {
			instance = new PresidentRecord();
		}
		return instance;
	}

	/**
	 * 构造
	 * 
	 * @param xid
	 */
	private PresidentRecord() {

	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		// 注册更新机制
		PresidentFightService.getInstance().addTickable(new HawkPeriodTickable(PresidentConstCfg.getInstance().getTickPeriod()) {
			@Override
			public void onPeriodTick() {
				onTickUpdate();
			}
		});
		return true;
	}

	/**
	 * 帧更新检测
	 */
	protected void onTickUpdate() {

	}
	
	/**
	 * 国王变更之后的通知(需要判断两个id是否一致, 连任的情况)
	 */
	public void onPresidentChanged(String lastPresidentId, String currPresidentId, String guildId) {
		try {
			// 国王当选进入和平时期
			if (!HawkOSOperator.isEmptyString(currPresidentId)) {				
				String leaderId = GuildService.getInstance().getGuildLeaderId(guildId);
				//取不到工会的leader信息说明是跨服国王, 这个逻辑已经看不清了.
				if (HawkOSOperator.isEmptyString(leaderId)) {					
					addPresidentEventRecord(PresidentEventType.PRESIDENT_ELECTED_VALUE, currPresidentId, null, guildId);
					
					//添加本次当选记录
					addPresidentElectedRecord(currPresidentId, guildId);
				} else {
					addPresidentEventRecord(PresidentEventType.PRESIDENT_ELECTED_VALUE, leaderId, null, guildId);
					
					//添加本次当选记录
					addPresidentElectedRecord(currPresidentId, guildId);
				}				
			} 
				
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 添加国王当选记录
	 * 
	 */
	public void addPresidentElectedRecord(String presidentId, String guildId) {
		PresidentHistory.Builder builder = PresidentHistory.newBuilder();
		builder.setTurnCount(PresidentFightService.getInstance().getPresidentCity().getTurnCount());
		builder.setTurnTime(HawkTime.getMillisecond());
		
						
		if (GlobalData.getInstance().isLocalPlayer(presidentId)) {
			Player presidentPlayer = GlobalData.getInstance().makesurePlayer(presidentId);
			builder.setPlayerId(presidentId);
			builder.setPlayerName(presidentPlayer.getName());
			builder.setPlayerIcon(presidentPlayer.getIcon());
			builder.setPfIcon(presidentPlayer.getPfIcon() == null ? "" : presidentPlayer.getPfIcon());
			
			builder.setGuildId(presidentPlayer.getGuildId());
			builder.setGuildName(presidentPlayer.getGuildName());
			builder.setGuildTag(GuildService.getInstance().getGuildTag(presidentPlayer.getGuildId()));
			builder.setGuildFlag(presidentPlayer.getGuildFlag());
			builder.setServerId(presidentPlayer.getMainServerId());
		} else {
			CrossPlayerStruct leader = RedisProxy.getInstance().getCrossGuildLeaderInfo(guildId);
			builder.setPlayerId(presidentId);
			builder.setPlayerName(leader.getName());
			builder.setPlayerIcon(leader.getIcon());
			builder.setPfIcon(leader.getPfIcon() == null ? "" : leader.getPfIcon());
			
			builder.setGuildId(leader.getGuildID());
			builder.setGuildName(leader.getGuildName());
			builder.setGuildTag(leader.getGuildTag());
			builder.setGuildFlag(leader.getGuildFlag());
			builder.setServerId(GlobalData.getInstance().getMainServerId(leader.getServerId()));
		}
						
		// 添加到缓存
		LocalRedis.getInstance().addElectedPresident(builder);
	}
	
	/**
	 * 添加国王事件记录
	 */
	public void addPresidentEventRecord(int eventType, String attackPlayerId, String defancePlayerId, String guildId) {
		PresidentEvent.Builder builder = PresidentEvent.newBuilder();
		
		builder.setEventTime(GsApp.getInstance().getCurrentTime());
		builder.setEventType(eventType);

		// 当选国王记录特殊处理下
		if (eventType == PresidentEventType.PRESIDENT_ELECTED_VALUE) {
			if (GlobalData.getInstance().isLocalPlayer(attackPlayerId)) {
				Player attackPlayer = GlobalData.getInstance().makesurePlayer(attackPlayerId);
				builder.setGuildTag(attackPlayer.getGuildTag());
				builder.setPlayerName(attackPlayer.getName());
				builder.setServerId(attackPlayer.getMainServerId());
				
			} else {
				CrossPlayerStruct crossLeader = RedisProxy.getInstance().getCrossGuildLeaderInfo(guildId);
				builder.setGuildTag(crossLeader.getGuildTag());
				builder.setPlayerName(crossLeader.getName());
				builder.setServerId(GlobalData.getInstance().getMainServerId(crossLeader.getServerId()));
			}
		} else {
			Player attackPlayer = GlobalData.getInstance().makesurePlayer(attackPlayerId);
			builder.setGuildTag(attackPlayer.getGuildTag());
			builder.setPlayerName(attackPlayer.getName());
			builder.setServerId(attackPlayer.getMainServerId());
			
			if (!HawkOSOperator.isEmptyString(defancePlayerId)) {
				Player defancePlayer = GlobalData.getInstance().makesurePlayer(defancePlayerId);
				builder.setEnemyGuildTag(defancePlayer.getGuildTag());
				builder.setEnemyPlayerName(defancePlayer.getName());
				builder.setEnemyServerId(defancePlayer.getMainServerId());
			}
		}
		
		// 添加到缓存
		LocalRedis.getInstance().addPresidentEvent(builder);
	}
	
	/**
	 * 添加国王战箭塔事件记录
	 * @param eventType
	 * @param attackPlayerId
	 * @param defancePlayerId
	 */
	public void addPresidentTowerEventRecord(int eventType, String attackPlayerId, String defancePlayerId, int towerIndex) {
		PresidentEvent.Builder builder = PresidentEvent.newBuilder();
		builder.setEventTime(GsApp.getInstance().getCurrentTime());
		builder.setEventType(eventType);
		
		Player attackPlayer = GlobalData.getInstance().makesurePlayer(attackPlayerId);
		builder.setGuildTag(attackPlayer.getGuildTag());
		builder.setPlayerName(attackPlayer.getName());
		builder.setServerId(attackPlayer.getMainServerId());
		
		if (!HawkOSOperator.isEmptyString(defancePlayerId)) {
			Player defancePlayer = GlobalData.getInstance().makesurePlayer(defancePlayerId);
			builder.setEnemyGuildTag(defancePlayer.getGuildTag());
			builder.setEnemyPlayerName(defancePlayer.getName());
			builder.setEnemyServerId(defancePlayer.getMainServerId());
		}
		
		// 添加到缓存
		LocalRedis.getInstance().addPresidentTowerEvent(builder, towerIndex);
	}
}
