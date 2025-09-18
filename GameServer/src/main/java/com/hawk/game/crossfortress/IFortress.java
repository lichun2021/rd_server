package com.hawk.game.crossfortress;

import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.activity.event.impl.OccupyFortressEvent;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossFortressConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventOccupyCrossFortressSecond;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 要塞
 * @author golden
 *
 */
public class IFortress {

	private final int[] pos;
	
	private final int pointId;
	
	private boolean hasNpc;
	
	private long lastTickScoreTime;
	
	private long occupyTime;
	
	private long lastTickMissionTime;
	
	public IFortress(int[] pos) {
		this.pos = pos;
		this.pointId = GameUtil.combineXAndY(pos[0], pos[1]);
		int fortressNpc = LocalRedis.getInstance().getFortressNpc(pointId);
		hasNpc = (fortressNpc > 0);
		occupyTime = LocalRedis.getInstance().getFortressOccupyTime(pointId);
		lastTickMissionTime = HawkTime.getMillisecond();
	}

	public void doFightNpcWin() {
		setHasNpc(false);
	}
	
	public void doFightWin(Player atkLeader, Player defLeader) {
		changeOccuption(atkLeader);
		
		if (defLeader != null && defLeader.hasGuild()) {
			GuildMailService.getInstance().sendGuildMail(defLeader.getGuildId(), MailParames.newBuilder()
					.setMailId(MailId.FORTRESS_OCCUPY_CHANGE_DEF)
					.addContents(getPosX(), getPosY(), atkLeader.getName()));
		}
		
		if (atkLeader != null && atkLeader.hasGuild()) {
			GuildMailService.getInstance().sendGuildMail(atkLeader.getGuildId(), MailParames.newBuilder()
					.setMailId(MailId.FORTRESS_OCCUPY_CHANGE_ATK)
					.addContents(getPosX(), getPosY()));
		}
	}
	
	/**
	 * 和平状态tick
	 */
	public void doPeaceTick() {
		
	}
	
	/**
	 * 战争状态tick
	 */
	public void doWarfareTick() {
		// 检测占领时长任务
		checkOccupyMission();
		
		long currentTime = HawkTime.getMillisecond();
		
		long scoreTickPeriod = CrossFortressConstCfg.getInstance().getScoreTickPeriod();
		if (currentTime - lastTickScoreTime < scoreTickPeriod) {
			return;
		}
		lastTickScoreTime = currentTime;
		
		for (IWorldMarch march : WorldMarchService.getInstance().getFortressStayMarchs(pointId)) {
			OccupyFortressEvent event = new OccupyFortressEvent(march.getPlayerId());
			int armyTotalCount = 0;
			for (ArmyInfo armyInfo : march.getArmys()) {
				armyTotalCount += armyInfo.getTotalCount();
			}
			event.setArmyCount(armyTotalCount);
			CrossActivityService.getInstance().postEvent(event);
		}
		
		 Player leader = WorldMarchService.getInstance().getFortressLeader(pointId);
		 if (leader != null) {
			 LogUtil.logCrossFortressOccupyTime(leader.getMainServerId(), (int)(scoreTickPeriod/1000));
		 }
	}
	
	/**
	 * 状态切换
	 */
	public void doStateChange() {
		setHasNpc(true);
		
		BlockingDeque<String> marchIds = WorldMarchService.getInstance().getFortressMarchs(pointId);
		for (String marchId : marchIds) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null) {
				continue;
			}
			WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
		}
	}
	
	/**
	 * 更换占领
	 */
	public void changeOccuption(Player player) {
		setOccupyTime(HawkTime.getMillisecond());
		broadcastSingleInfo(null);
		CrossFortressService.getInstance().addOccupyInfo(pointId, player);
	}
	
	/**
	 * 广播更新
	 */
	public void broadcastSingleInfo(Player player) {
		try {
			SuperWeaponInfo.Builder infoBuilder = genFortressInfoBuilder();
			if (player != null) {
				HawkProtocol protocol = HawkProtocol.valueOf(HP.code.CROSS_FORTRESS_SINGLE_INFO_S, infoBuilder);
				player.sendProtocol(protocol);
			} else {
				for (Player sendPlayer : GlobalData.getInstance().getOnlinePlayers()) {
					HawkProtocol protocol = HawkProtocol.valueOf(HP.code.CROSS_FORTRESS_SINGLE_INFO_S, infoBuilder);
					sendPlayer.sendProtocol(protocol);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public SuperWeaponInfo.Builder genFortressInfoBuilder() {
		try {
			SuperWeaponInfo.Builder infoBuilder = SuperWeaponInfo.newBuilder();
			infoBuilder.setPeriodType(CrossFortressService.getInstance().getCurrentState());
			infoBuilder.setTurnCount(0);
			infoBuilder.setPeriodEndTime(0);
			infoBuilder.setWarStartTime(0);
			infoBuilder.setOccupyTime(getOccupyTime());
			infoBuilder.setHasSignUp(false);
			infoBuilder.setHasAutoSignUp(false);
			
			int[] pos = GameUtil.splitXAndY(pointId);
			infoBuilder.setX(pos[0]);
			infoBuilder.setY(pos[1]);

			Player player = WorldMarchService.getInstance().getFortressLeader(pointId);
			if (player != null && player.hasGuild()) {
				String guildId = player.getGuildId();
				infoBuilder.setLeaderGuildId(guildId);
				infoBuilder.setLeaderGuildTag(GuildService.getInstance().getGuildTag(guildId));
				infoBuilder.setLeaderGuildName(GuildService.getInstance().getGuildName(guildId));
				infoBuilder.setLeaderGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
				
				infoBuilder.setLeaderId(player.getId());
				infoBuilder.setLeaderName(player.getName());
				infoBuilder.setLeaderIcon(player.getIcon());
				infoBuilder.setLeaderPfIcon(player.getPfIcon());
				infoBuilder.setServerId(player.getMainServerId());
			}
			infoBuilder.setHasNpc(hasNpc);
			return infoBuilder;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	public int[] getPos() {
		return pos;
	}
	
	public int getPosX() {
		return pos[0];
	}
	
	public int getPosY() {
		return pos[1];
	}
	
	public int getPointId() {
		return pointId;
	}

	public boolean hasNpc() {
		return hasNpc;
	}

	public boolean isHasNpc() {
		return hasNpc;
	}

	public void setHasNpc(boolean hasNpc) {
		this.hasNpc = hasNpc;
		LocalRedis.getInstance().setFortressNpc(pointId, hasNpc ? 1 : 0);
	}
	
	public long getOccupyTime() {
		return occupyTime;
	}

	public void setOccupyTime(long occupyTime) {
		this.occupyTime = occupyTime;
		LocalRedis.getInstance().setFortressOccupyTime(pointId, occupyTime);
	}

	public boolean repatriateMarch(Player player, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild()) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getFortressLeader(pointId);
		if (leader != null && leader.hasGuild() && !player.getGuildId().equals(leader.getGuildId())) {
			return false;
		}
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REPATRIATE_MARCH) {
			@Override
			public boolean onInvoke() {
				List<IWorldMarch> marchs = WorldMarchService.getInstance().getFortressStayMarchs(pointId);
				for (IWorldMarch iWorldMarch : marchs) {
					if (iWorldMarch.isReturnBackMarch()) {
						continue;
					}
					if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
						continue;
					}
					
					WorldMarchService.logger.info("marchRepatriate, playerId:{}, tarPlayerId:{}, marchId:{}", player.getId(), targetPlayerId, iWorldMarch.getMarchId());
					
					WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, HawkApp.getInstance().getCurrentTime());
					
					WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(getPointId());
					if (worldPoint != null) {
						WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
					}
				}
				CrossFortressService.getInstance().sendCrossFortressQuarterInfo(player, IFortress.this);
				broadcastSingleInfo(player);
				return true;
			}
		});

		return true;
	}
	
	/**
	 * 任命队长
	 */
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild()) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getFortressLeader(pointId);
		if (leader != null && leader.hasGuild() && !player.getGuildId().equals(leader.getGuildId())) {
			return false;
		}
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().changeFortressMarchLeader(pointId, targetPlayerId);
				CrossFortressService.getInstance().sendCrossFortressQuarterInfo(player, IFortress.this);
				broadcastSingleInfo(player);
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 检测占领时长任务
	 */
	private void checkOccupyMission() {
		try {
			long peroid = CrossConstCfg.getInstance().getFortressOccupyMissionPeriod();
			long currentTime = HawkTime.getMillisecond();
			if (currentTime - lastTickMissionTime < peroid) {
				return;
			}
			// 任务增加的时间
			long addTime = currentTime - lastTickMissionTime;
			lastTickMissionTime = currentTime;
			
			List<IWorldMarch> marchs = WorldMarchService.getInstance().getFortressStayMarchs(pointId);
			for (IWorldMarch mrach : marchs) {
				MissionManager.getInstance().postMsg(mrach.getPlayer(), new EventOccupyCrossFortressSecond(addTime));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
