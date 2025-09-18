package com.hawk.game.world.march.submarch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.EnterSuperWeaponEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.SuperWeaponAwardCfg;
import com.hawk.game.config.SuperWeaponCfg;
import com.hawk.game.config.SuperWeaponSoldierCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.SuperWeaponAwardType;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPointService;

/**
 * 超级武器行军
 * @author golden
 *
 */
public interface SuperWeaponMarch extends BasedMarch {
	
	@Override
	default boolean isSuperWeaponMarch() {
		return true;
	}
	
	@Override
	default void onMarchReach(Player player) {
		// 统一按多人一起处理
		List<IWorldMarch> massMarchList = getMassMarchList(this);
		// 玩家联盟判断
		if (!player.hasGuild()) {
			returnMarchList(massMarchList);
			return;
		}
		
		Set<String> atkPlayerIds = new HashSet<>();
		for (IWorldMarch march : massMarchList) {
			atkPlayerIds.add(march.getPlayerId());
		}
		
		// 目标点
		int terminalId = this.getMarchEntity().getTerminalId();
		// 超级武器
		IWeapon weapon = SuperWeaponService.getInstance().getWeapon(terminalId);
		if (weapon.getStatus() != SuperWeaponPeriod.WARFARE_VALUE) {
			doPeriodChange(massMarchList);
			return;
		}
		
		// 超级武器点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 当前占领联盟
		String guildId = weapon.getGuildId();
		// 是否有npc占领
		boolean hasNpc = weapon.hasNpc();
		
		// 如果有npc占领
		if (hasNpc) {
			PveBattleIncome battleIncome =  BattleService.getInstance().initSuperWeaponPveBattleData(BattleConst.BattleType.ATTACK_SUPER_WEAPON_PVE, worldPoint.getId(), getSuperWeaponSoldierCfg(), massMarchList, weapon.getWeaponCfg().getId());
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			boolean attackWin = battleOutcome.isAtkWin();
			// 据点PVE战斗邮件发放
			FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_SUPER_WEAPON_PVE, battleIncome, battleOutcome, null);
			// 发送战斗结果，用于前端播放动画
			WorldMarchService.getInstance().sendBattleResultInfo(this, attackWin, battleOutcome.getAftArmyMapAtk().get(player.getId()), Collections.emptyList(), false);
			
			if (attackWin) {
				assitenceWarPoint(massMarchList, worldPoint, player);
				weapon.fightNpcWin(player.getName(), player.getGuildId());
				weapon.doSuperWeaponAttackWin(player, null);
				sendAttackAward(weapon, player.getGuildId(), massMarchList, atkPlayerIds);
				
				// 通知场景本点数据更新
				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
				weapon.addOccupuHistory(player.getGuildId());
				
				for (String playerId : atkPlayerIds) {
					ActivityManager.getInstance().postEvent(new EnterSuperWeaponEvent(playerId));
				}
			} else {
				// 进攻方行军返回
				for (IWorldMarch march : massMarchList) {
					WorldMarchService.getInstance().onMarchReturn(march, battleOutcome.getAftArmyMapAtk().get(march.getPlayerId()), worldPoint.getId());
				}
			}
			
			return;
		}
		
		// 本盟占领，走援助逻辑 它盟占领，走攻击逻辑
		if (!HawkOSOperator.isEmptyString(guildId) && guildId.equals(player.getGuildId())) {
			// 援助成功处理
			weapon.doSuperWeaponAssistance(this);
			// 援助战斗点
			assitenceWarPoint(massMarchList, worldPoint, player);
			
			for (String playerId : atkPlayerIds) {
				ActivityManager.getInstance().postEvent(new EnterSuperWeaponEvent(playerId));
			}
			
		} else {
			boolean attackWin = true;
			// 超级武器里是否有行军
			boolean hasMarchBefore = WorldMarchService.getInstance().hasSuperWeaponMarch(terminalId);
			// 有行军驻扎走战斗逻辑， 没有则直接驻扎
			if (hasMarchBefore) {
				attackWin = attackWarPoint(massMarchList, worldPoint, player);
			} else {
				weapon.doSuperWeaponAttackWin(player, null);
				for (IWorldMarch march : massMarchList) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, march.getMarchEntity().getArmys(), worldPoint);
				}
			}
			
			if (attackWin && !weapon.checkOccupyHistory(player.getGuildId())) {
				sendAttackAward(weapon, player.getGuildId(), massMarchList, atkPlayerIds);
			}
			
			if (attackWin) {
				weapon.addOccupuHistory(player.getGuildId());
				
				for (String playerId : atkPlayerIds) {
					ActivityManager.getInstance().postEvent(new EnterSuperWeaponEvent(playerId));
				}
			}
		}
		
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		// 之前没有行军占领，刷新联盟战争显示
		if (!WorldMarchService.getInstance().hasSuperWeaponMarch(targetPoint.getId())) {
			Collection<IWorldMarch> worldPointMarch = WorldMarchService.getInstance().getWorldPointMarch(targetPoint.getX(), targetPoint.getY());
			for (IWorldMarch march : worldPointMarch) {
				if (march.isReturnBackMarch() || march.isMassJoinMarch()) {
					continue;
				}
				if (GuildService.getInstance().isInTheSameGuild(march.getPlayerId(), this.getPlayerId())) {
					continue;
				}
				WorldMarchService.getInstance().addGuildMarch(march);
				march.updateMarch();
			}
		}
		
		WorldMarchService.getInstance().addSuperWeaponMarch(targetPoint.getId(), this, false);
	}
	
	/**
	 * 超级武器状态变更处理 : 发邮件, 并返回行军
	 * @param massMarchList
	 */
	default void doPeriodChange(List<IWorldMarch> massMarchList) {
		returnMarchList(massMarchList);
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		builder.setPointType(WorldPointType.SUPER_WEAPON);
		builder.setX(point.getX());
		builder.setY(point.getY());

		// 队长id
		Player leader = WorldMarchService.getInstance().getSuperWeaponLeader(pointId);
		if (leader == null) {
			return builder;
		}
		
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());
			builder.setGuildTag(guildTag);
		}

		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(leader.getId());
		leaderInfo.setPlayerName(leader.getName());
		leaderInfo.setIconId(leader.getIcon());
		leaderInfo.setPfIcon(leader.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		String leaderMarchId = WorldMarchService.getInstance().getSuperWeaponLeaderMarchId(pointId);
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(leaderMarchId);
		builder.setLeaderMarch(leaderInfo);
		
		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getSuperWeaponStayMarchs(pointId);
		for (IWorldMarch stayMarch : marchs) {
			if (stayMarch.getMarchId().equals(leaderMarchId)) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(stayMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(stayMarch.getMarchEntity().getArmys());
		}
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}
	
	default void sendAttackAward(IWeapon superWeapon, String guildId, List<IWorldMarch> marchList, Set<String> atkPlayerIds) {
		SuperWeaponCfg superWeaponCfg = AssembleDataManager.getInstance().getSuperWeaponCfg(superWeapon.getPointId());
		// 攻占者奖励
		AwardItems memberAwards = AwardItems.valueOf();
		List<SuperWeaponAwardCfg> memberAwardCfgs = AssembleDataManager.getInstance().getSuperWeaponAwards(superWeapon.getPointId(), SuperWeaponAwardType.ATTACK_MEMBER_AWARD);
		for (SuperWeaponAwardCfg cfg : memberAwardCfgs) {
			for (int i = 0; i < cfg.getTotalNumber(); i++) {
				memberAwards.addItemInfos(cfg.getRewardItem());
			}
		}
		int[] pos = GameUtil.splitXAndY(superWeapon.getPointId());
		for (String playerId : atkPlayerIds) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.addContents(superWeaponCfg.getId(), pos[0],pos[1])
					.setMailId(MailId.SUPER_WEAPON_ATTACK_MEMBER_AWARD)
					.setRewards(memberAwards.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
		
		// 联盟成员奖励
		AwardItems guildAwards = AwardItems.valueOf();
		List<SuperWeaponAwardCfg> guildAwardCfgs = AssembleDataManager.getInstance().getSuperWeaponAwards(superWeapon.getPointId(), SuperWeaponAwardType.ATTACK_GUILD_MEMBER_AWARD);
		for (SuperWeaponAwardCfg cfg : guildAwardCfgs) {
			for (int i = 0; i < cfg.getTotalNumber(); i++) {
				guildAwards.addItemInfos(cfg.getRewardItem());
			}
		}
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.addContents(superWeaponCfg.getId(), pos[0],pos[1])
					.setMailId(MailId.SUPER_WEAPON_ATTACK_GUILD_AWARD)
					.setRewards(guildAwards.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
		
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		// 礼包
		List<SuperWeaponAwardCfg> leaderSendAwardCfgs = AssembleDataManager.getInstance().getSuperWeaponAwards(superWeapon.getPointId(), SuperWeaponAwardType.ATTACK_LEADER_SEND_AWARD);
		for (SuperWeaponAwardCfg cfg : leaderSendAwardCfgs) {
			String superWeaponGiftInfo = LocalRedis.getInstance().getSuperWeaponGiftInfo(turnCount, superWeapon.getPointId(), guildId, cfg.getId());
			String afterInfo = null;
			if (HawkOSOperator.isEmptyString(superWeaponGiftInfo)) {
				int sendCount = 0;
				int totalCount = cfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			} else {
				String[] splitInfo = superWeaponGiftInfo.split("_");
				int sendCount = Integer.parseInt(splitInfo[0]);
				int totalCount = Integer.parseInt(splitInfo[1]) + cfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			}
			LocalRedis.getInstance().updateSuperWeaponGiftInfo(turnCount, superWeapon.getPointId(), guildId, cfg.getId(), afterInfo);
		}
	}
	
	/**
	 * 获取npc驻守兵力
	 * @return
	 */
	default SuperWeaponSoldierCfg getSuperWeaponSoldierCfg() {
		int worldMaxLevel = WorldMonsterService.getInstance().getMaxCommonMonsterLvl();
		SuperWeaponSoldierCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSoldierCfg.class, worldMaxLevel);
		if (config == null) {
			int configSize = HawkConfigManager.getInstance().getConfigSize(SuperWeaponSoldierCfg.class);
			config = HawkConfigManager.getInstance().getConfigByIndex(SuperWeaponSoldierCfg.class, configSize - 1);
		}
		return config;
	}
	
	@Override
	public default Set<IWorldMarch> getQuarterMarch() {
		Set<IWorldMarch> retMarchs = new HashSet<>();
		
		if (this.isReachAndStopMarch()) {
			BlockingDeque<String> marchIds = WorldMarchService.getInstance().getSuperWeaponMarchs(this.getTerminalId());
			for (String marchId : marchIds) {
				retMarchs.add(WorldMarchService.getInstance().getMarch(marchId));
			}
		}
		return retMarchs;
	}
}
