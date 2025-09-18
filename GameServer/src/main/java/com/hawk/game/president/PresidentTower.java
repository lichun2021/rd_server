package com.hawk.game.president;

import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.President.PresidentPeriod;
import com.hawk.game.protocol.President.PresidentTowerInfoSync;
import com.hawk.game.protocol.President.PresidentTowerStatus;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 王城箭塔
 * @author zhenyu.shang
 * @since 2017年12月4日
 */
public class PresidentTower {
	
	/** 箭塔需要 */
	private int index;
	
	/** 当前所属联盟 */
	private String guildId;
	
	/** 占领者ID */
	private String leaderId;
	
	/** 占领者名称 */
	private String leaderName;
	
	/** 箭塔状态 */
	private int towerStatus;
	
	private long occupyTime;
	
	private long lastTickTime;
	
	public boolean init() {
		setTowerStatus(PresidentTowerStatus.TOWER_PEACE_VALUE);
		String guildId = LocalRedis.getInstance().getPresidentDataByKey("tower" + index, String.class);
		if(guildId != null) {
			this.guildId = guildId;
			if(!guildId.equals(PresidentFightService.getInstance().getCurrentGuildId()) && PresidentFightService.getInstance().getPresidentPeriodType() == PresidentPeriod.WARFARE_VALUE){
				setTowerStatus(PresidentTowerStatus.TOWER_FIGHT_VALUE);
			}
		}
		lastTickTime = HawkTime.getMillisecond();
		return true;
	}
	
	/**
	 * 箭塔tick
	 * 此tick只在战争状态调用
	 * 
	 * 在王城的tick中驱动
	 */
	public void tick(){
		String presidentGuild = PresidentFightService.getInstance().getCurrentGuildId();
		if(guildId == null || guildId.equals(presidentGuild)){
			return;
		}
		
		// 跨服情况下,同阵营不开打
//		if (CrossActivityService.getInstance().isOpen()) {
//			String towerServer = CrossActivityService.getInstance().getCrossTowerOwner(guildId);
//			int camp1 = CrossActivityService.getInstance().getCamp(towerServer);
//			
//			String presidentServer = GuildService.getInstance().getGuildServerId(presidentGuild);
//			if (HawkOSOperator.isEmptyString(presidentServer)) {
//				return;
//			}
//			
//			int camp2 = CrossActivityService.getInstance().getCamp(presidentServer);
//			if (camp1 == camp2) {
//				return;
//			}
//		}
		
		long currTime = HawkApp.getInstance().getCurrentTime();
		long tickPeriod = PresidentConstCfg.getInstance().getTickPeriod();
		if(currTime > lastTickTime + tickPeriod){
			//如果箭塔和王城占领者不一致，则开始打
			BlockingDeque<String> presidentMarchs = WorldMarchService.getInstance().getPresidentMarchs();
			for (String marchId : presidentMarchs) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				
				// 可攻击则进行一次攻击,并记录攻击结果,添加数据
				List<ArmyInfo> armys = march.getMarchEntity().getArmys();
				for (ArmyInfo armyInfo : armys) {
					int towerAtk = PresidentConstCfg.getInstance().getTowerAtk();
					armyInfo.killByTower(towerAtk);
				}
				march.getMarchEntity().setArmys(armys);
			}
			lastTickTime = currTime;
		}
	}
	
	/**
	 * 广播箭塔信息
	 */
	public void broadcastPresidentTowerInfo(Player player) {
		try {
			PresidentTowerInfoSync.Builder infoBuilder = PresidentTowerInfoSync.newBuilder();
			
			infoBuilder.setTowerIdx(getIndex());
			int pos[] = GameUtil.splitXAndY(getIndex());
			infoBuilder.setX(pos[0]);
			infoBuilder.setY(pos[1]);
			
			infoBuilder.setStatus(PresidentTowerStatus.valueOf(getTowerStatus()));
			if(!HawkOSOperator.isEmptyString(guildId) && PresidentFightService.getInstance().isFightPeriod()){
				
				try {
					if (CrossActivityService.getInstance().isOpen()) {
						Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
						if (leader != null && !leader.getGuildId().equals(guildId)) {
							setGuildId(leader.getGuildId());
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				infoBuilder.setLeaderGuildId(getGuildId());
				infoBuilder.setLeaderGuildName(GuildService.getInstance().getGuildName(getGuildId()));
				infoBuilder.setLeaderGuildTag(GuildService.getInstance().getGuildTag(getGuildId()));
				infoBuilder.setLeaderGuildFlagId(GuildService.getInstance().getGuildFlag(getGuildId()));
				infoBuilder.setOccupyTime(getOccupyTime());
				
				Player leader = WorldMarchService.getInstance().getPresidentTowerLeader(getIndex());
				if (leader != null) {
					infoBuilder.setLeaderId(leader.getId());
					infoBuilder.setLeaderName(leader.getName());
					infoBuilder.setLeaderIcon(leader.getIcon());
					infoBuilder.setLeaderPfIcon(leader.getPfIcon());
					infoBuilder.setLeaderServerId(leader.getMainServerId());
					infoBuilder.setLeaderOfficerId(GameUtil.getOfficerId(leader.getId()));
				}
				
				if (CrossActivityService.getInstance().isOpen()) {
					infoBuilder.setCrossOwnerServer(CrossActivityService.getInstance().getCrossTowerOwner(guildId));
					if (leader != null) {
						infoBuilder.setIsCrossFightGuild(RedisProxy.getInstance().isCrossFightGuild(leader.getMainServerId(), guildId));
					}
				}
			}

			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PRESIDENT_TOWER_INFO_SYNC, infoBuilder);
			if (player != null) {
				player.sendProtocol(protocol);
			} else {
				for (Player sendPlayer : GlobalData.getInstance().getOnlinePlayers()) {
					sendPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_TOWER_INFO_SYNC, infoBuilder));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getGuildId() {
		return guildId;
	}

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public String getLeaderName() {
		return leaderName;
	}

	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}

	public int getTowerStatus() {
		return towerStatus;
	}

	public void setTowerStatus(int towerStatus) {
		this.towerStatus = towerStatus;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
		LocalRedis.getInstance().updatePresidentDataByKey("tower" + index, guildId);
	}
	
	public boolean canFight(){
		return this.getTowerStatus() == PresidentTowerStatus.TOWER_FIGHT_VALUE;
	}

	public long getOccupyTime() {
		return occupyTime;
	}

	public void setOccupyTime(long occupyTime) {
		this.occupyTime = occupyTime;
	}
	
	/**
	 * 遣返行军
	 */
	public boolean repatriateMarch(Player player, String targetPlayerId, HawkProtocol protocol) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getPresidentTowerLeader(index);
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REPATRIATE_MARCH) {
			@Override
			public boolean onInvoke() {
				List<IWorldMarch> marchs = WorldMarchService.getInstance().getPresidentTowerStayMarchs(index);
				for (IWorldMarch iWorldMarch : marchs) {
					if (iWorldMarch.isReturnBackMarch()) {
						continue;
					}
					if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
						continue;
					}
					WorldMarchService.logger.info("marchRepatriate, playerId:{}, tarPlayerId:{}, marchId:{}", player.getId(), targetPlayerId, iWorldMarch.getMarchId());
					WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, HawkApp.getInstance().getCurrentTime());
				}
				PresidentFightService.getInstance().sendPresidentTowerQuarterInfo(player, index);
				broadcastPresidentTowerInfo(player);
				return true;
			}
		});

		return true;
	}
	
	/**
	 * 任命队长
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId, HawkProtocol protocol) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getPresidentTowerLeader(index);
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().changePresidentTowerMarchLeader(index, targetPlayerId);
				PresidentFightService.getInstance().sendPresidentTowerQuarterInfo(player, index);
				broadcastPresidentTowerInfo(null);
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 进入和平状态
	 */
	public void enterPace() {
		setGuildId(null);
		setLeaderId(null);
		setLeaderName(null);
		setOccupyTime(0);
		setTowerStatus(PresidentTowerStatus.TOWER_PEACE_VALUE);		
	}

	/**
	 * 获取跨服王战的时候归属的国家
	 * @return
	 */
	public String getCrossOwnerServer() {
		return null;
	}
}
