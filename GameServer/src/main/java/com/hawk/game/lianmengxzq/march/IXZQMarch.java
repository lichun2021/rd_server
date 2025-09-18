package com.hawk.game.lianmengxzq.march;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.lianmengxzq.XZQGift;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.XZQTlog;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 超级武器行军
 * @author golden
 *
 */
public interface IXZQMarch extends BasedMarch {

	// @Override
	// default void heartBeats() {
	// 通过覆盖这个方法是可以把tick 能力转移到Service的
	// }

	@Override
	default void onMarchReach(Player player) {
		// 统一按多人一起处理
		List<IWorldMarch> massMarchList = getMassMarchList(this);
		// 玩家联盟判断
		if (!player.hasGuild()) {
			returnMarchList(massMarchList);
			return;
		}
		List<Player> atkPlayers = new ArrayList<>();
		for (IWorldMarch march : massMarchList) {
			atkPlayers.add(march.getPlayer());
		}
		// 目标点
		int terminalId = this.getMarchEntity().getTerminalId();
		// 超级武器点
		XZQWorldPoint xzqPoint = XZQService.getInstance().getXZQPoint(terminalId);
		if (xzqPoint.isPeace()) {
			returnMarchList(massMarchList);
			return;
		}
		//如果不是自己联盟占领,需要判断占领个数是否达到上线
		int canAttack =  XZQService.getInstance().canAttackXZQWorldPoint(player, xzqPoint);
		if(canAttack > 0){
			for(Player atker :atkPlayers){
				atker.sendError(HP.code.XZQ_SINGLE_MARCH_C_VALUE, canAttack, 0);
			}
			returnMarchList(massMarchList);
			return;
		} 
		int termId = XZQService.getInstance().getXZQTermId();
		// 如果有npc占领
		if (xzqPoint.hasNpc()) {
			TemporaryMarch npcMarch = xzqPoint.getNpcMarch();
			PveBattleIncome battleIncome = BattleService.getInstance().initXZQPveBattleData(BattleConst.BattleType.ATTACK_XZQ_PVE, xzqPoint.getId(),
					npcMarch, massMarchList, xzqPoint.getXzqCfg().getId());
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			boolean attackWin = battleOutcome.isAtkWin();
			List<ArmyInfo> npcLeftArmy = battleOutcome.getAftArmyMapDef().get(npcMarch.getPlayerId());
			npcMarch.setArmys(npcLeftArmy);
			// 据点PVE战斗邮件发放
			FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_XZQ_PVE, battleIncome, battleOutcome, null);
			// 发送战斗结果，用于前端播放动画
			WorldMarchService.getInstance().sendBattleResultInfo(this, attackWin, battleOutcome.getAftArmyMapAtk().get(player.getId()), Collections.emptyList(), false);
			if (attackWin) {
				xzqPoint.fightNpcWin(player.getId(),player.getName(), player.getGuildId());
				xzqPoint.doXZQAttackWin(player, null);
				//攻破奖励
				XZQGift.getInstance().sendAttackAward(xzqPoint, player.getGuildId(), massMarchList, atkPlayers);
				//添加伤害
				xzqPoint.addFirstOccupyDamage(battleOutcome, atkPlayers);
				//添加攻破刻字
				xzqPoint.addFirstOccupyRecord(atkPlayers);
				//添加攻占记录
				xzqPoint.addOccupuHistory(player.getGuildId());
				// 通知场景本点数据更新
				assitenceWarPoint(massMarchList, xzqPoint, player);
				//打败守军TLog
				XZQTlog.XZQNPCAttacked(player, termId, player.getGuildId(),xzqPoint.getXzqCfg().getId() );
			} else {
				// 进攻方行军返回
				for (IWorldMarch march : massMarchList) {
					WorldMarchService.getInstance().onMarchReturn(march, battleOutcome.getAftArmyMapAtk().get(march.getPlayerId()), xzqPoint.getId());
				}
				//添加伤害统计
				xzqPoint.addFirstOccupyDamage(battleOutcome, atkPlayers);
			}
			//Tlog
			XZQTlog.XZQPlayerParticipate(atkPlayers, termId, xzqPoint.getXzqCfg().getId());
			return;
		}
		// 本盟占领，走援助逻辑 它盟占领，走攻击逻辑
		Player occupyLeader = WorldMarchService.getInstance().getXZQLeader(xzqPoint.getId());
		if (Objects.nonNull(occupyLeader) && Objects.equals(occupyLeader.getGuildId(), player.getGuildId())) {
			// 援助战斗点
			assitenceWarPoint(massMarchList, xzqPoint, player);
		} else {
			boolean attackWin = true;
			// 有行军驻扎走战斗逻辑， 没有则直接驻扎
			if (Objects.nonNull(occupyLeader)) {
				attackWin = attackWarPoint(massMarchList, xzqPoint, player);
			} else {
				for (IWorldMarch march : massMarchList) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE,
							march.getMarchEntity().getArmys(), xzqPoint);
				}
			}
			if (attackWin) {
				String controlGuild = xzqPoint.getGuildControl();
				boolean isSend = HawkOSOperator.isEmptyString(controlGuild)
						|| !controlGuild.equals(player.getGuildId());
				if(isSend){
					XZQGift.getInstance().sendAttackAward(xzqPoint, player.getGuildId(), massMarchList, atkPlayers);
				}
				xzqPoint.doXZQAttackWin(player, occupyLeader);
				xzqPoint.addOccupuHistory(player.getGuildId());
			}
		}
		XZQTlog.XZQPlayerParticipate(atkPlayers, termId, xzqPoint.getXzqCfg().getId());
	}

	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		// 之前没有行军占领，刷新联盟战争显示
		String guildId = this.getPlayer().getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		if (!WorldMarchService.getInstance().hasXZQMarch(targetPoint.getId())) {
			Collection<IWorldMarch> worldPointMarch = WorldMarchService.getInstance().getWorldPointMarch(targetPoint.getX(), targetPoint.getY());
			for (IWorldMarch march : worldPointMarch) {
				if (march.isReturnBackMarch() || march.isMassJoinMarch()) {
					continue;
				}
				if (GuildService.getInstance().isInTheSameGuild(march.getPlayerId(), this.getPlayerId())) {
					continue;
				}
				WorldMarchService.getInstance().addGuildMarch(guildId, march.getMarchId());
				march.updateMarch();
			}
		}
		WorldMarchService.getInstance().addXZQMarch(targetPoint.getId(), this, false);
	}


	/**
	 * 获取被动方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		builder.setPointType(WorldPointType.XIAO_ZHAN_QU);
		builder.setX(point.getX());
		builder.setY(point.getY());
		// 队长id
		Player leader = WorldMarchService.getInstance().getXZQLeader(pointId);
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
		String leaderMarchId = WorldMarchService.getInstance().getXZQLeaderMarchId(pointId);
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(leaderMarchId);
		builder.setLeaderMarch(leaderInfo);

		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());

		List<IWorldMarch> marchs = WorldMarchService.getInstance().getXZQStayMarchs(pointId);
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

	


	@Override
	public default Set<IWorldMarch> getQuarterMarch() {
		Set<IWorldMarch> retMarchs = new HashSet<>();
		if (this.isReachAndStopMarch()) {
			BlockingDeque<String> marchIds = WorldMarchService.getInstance().getXZQMarchs(this.getTerminalId());
			for (String marchId : marchIds) {
				retMarchs.add(WorldMarchService.getInstance().getMarch(marchId));
			}
		}
		return retMarchs;
	}

	@Override
	public default boolean isXZQMarch() {
		return true;
	}

}
