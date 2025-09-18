package com.hawk.game.module.spacemecha.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.WorldAwardPushInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceConst;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.MailRewards;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;


public interface SpaceMechaStrongHoldMarch extends BasedMarch {
	
	@Override
	default void onMarchReach(Player leader) {
		// 行军
		WorldMarch leaderMarch = getMarchEntity();
		// 目标点
		int terminalId = leaderMarch.getTerminalId();
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 点为空
		if (point == null) {
			strongHoldMarchReturn();
			sendPointErrorMail(leader);
			HawkLog.errPrintln("spaceMecha attack stronghold march reach, point null, playerId: {}, guildId: {}, terminalId: {}", leader.getId(), leader.getGuildId(), terminalId);
			return;
		}

		// 非据点类型
		if (point.getPointType() != WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			strongHoldMarchReturn();
			sendPointErrorMail(leader);
			HawkLog.errPrintln("spaceMecha attack stronghold march reach, point not stronghold point, playerId: {}, guildId: {}, terminalId: {}", leader.getId(), leader.getGuildId(), terminalId);
			return;
		}
		
		if (HawkOSOperator.isEmptyString(leader.getGuildId())) {
			strongHoldMarchReturn();
			sendPointErrorMail(leader);
			HawkLog.errPrintln("spaceMecha attack stronghold march reach, player has no guild, playerId: {}, terminalId: {}", leader.getId(), terminalId);
			return;
		}
		
		if (!leader.getGuildId().equals(point.getGuildId())) {
			strongHoldMarchReturn();
			sendPointErrorMail(leader);
			HawkLog.errPrintln("spaceMecha attack stronghold march reach, point owner error, playerId: {}, guildId: {}, terminalId: {}", leader.getId(), leader.getGuildId(), terminalId);
			return;
		}

		StrongHoldWorldPoint strongPoint = (StrongHoldWorldPoint) point;
		int strongHoldId = strongPoint.getStrongHoldId();
		// 据点配置
		SpaceMechaStrongholdCfg strongHoldCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, strongHoldId);
		if (strongHoldCfg == null) {
			strongHoldMarchReturn();
			sendPointErrorMail(leader);
			HawkLog.errPrintln("spaceMecha attack stronghold march reach, config error, playerId: {}, guildId: {}, terminalId: {}, strongholdId: {}", leader.getId(), leader.getGuildId(), terminalId, strongHoldId);
			return;
		}

		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(leader);
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		// 填充参与集结信息
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(GlobalData.getInstance().makesurePlayer(massJoinMarch.getPlayerId()));
			atkMarchs.add(massJoinMarch);
		}

		// 战斗数据输入
		PveBattleIncome battleIncome = BattleService.getInstance().initStrongHoldBattleData(BattleType.ATTACK_STRONG_HOLD_PVE, strongPoint, atkMarchs, atkPlayers);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		
		int oldBlood = strongPoint.getRemainBlood();
		// 战斗结果处理
		MailRewards mailRewards = doBattleResult(leader, strongPoint, strongHoldCfg, atkPlayers, battleOutcome);
		// 据点PVE战斗邮件发放     ------- 随机buff的参数
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_STRONG_HOLD_PVE, battleIncome, battleOutcome, mailRewards);
		
		SpaceMechaStrongholdCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, strongPoint.getStrongHoldId());
		boolean atkWin = strongPoint.getRemainBlood() <= 0;
		boolean monsterDead = strongPoint.getRemainBlood() <= 0 && strongPoint.getHpNum() >= cfg.getHpNumber();
		// 攻击方玩家部队
		List<ArmyInfo> mergAllPlayerArmy = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		// 发送战斗结果
		WorldMarchService.getInstance().sendBattleResultInfo(this, atkWin, mergAllPlayerArmy, Collections.emptyList(), monsterDead);
		
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, battleOutcome.getAftArmyMapAtk().get(massJoinMarch.getMarchEntity().getPlayerId()), 0);
		}
		
		SpaceMechaService.getInstance().logAtkStrongHold(leader, strongPoint);
		HawkLog.logPrintln("spaceMecha attack stronghold march reach, guildId: {}, playerId: {}, posX: {}, poxY: {}, strongHoldId: {}, oldBlood: {}, remain blood: {}, hpNum: {}", 
				strongPoint.getGuildId(), leaderMarch.getPlayerId(), strongPoint.getX(), strongPoint.getY(), strongHoldId, oldBlood, strongPoint.getRemainBlood(), strongPoint.getHpNum());
		
		if (strongPoint.getRemainBlood() <= 0) {
			if (strongPoint.getHpNum() >= cfg.getHpNumber()) {
				// 战斗胜利，移除点
				MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(strongPoint.getGuildId());
				spaceObj.removeStrongHold(strongPoint.getId(), strongHoldId);
			} else {
				strongPoint.setHpNum(strongPoint.getHpNum() + 1);
				strongPoint.setDefArmyList(cfg.getArmyList());
				strongPoint.setRemainBlood(cfg.getBlood());
			}
		}
		
		WorldPointService.getInstance().notifyPointUpdate(strongPoint.getX(), strongPoint.getY());
	}
	
	/**
	 * 集结打怪行军返回
	 */
	default void strongHoldMarchReturn() {
		// 队长行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());

		// 队员行军返回
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			// 行军返回
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, massJoinMarch.getMarchEntity().getArmys(), getMarchEntity().getTerminalId());
		}
	}

	/**
	 * 发邮件：目标野怪消失
	 */
	default void sendPointErrorMail(Player leader) {
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(leader);

		// 填充参与集结信息
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(GlobalData.getInstance().makesurePlayer(massJoinMarch.getPlayerId()));
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(leader.getGuildId());
		if (spaceObj != null) {
			int strongHoldId = 0;
			if (spaceObj.getSpStrongHoldPoint().getId() == this.getTerminalId()) {
				strongHoldId = spaceObj.getSpStrongHoldPoint().getStrongHoldId();
			} else {
				SpaceMechaLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, spaceObj.getLevel());
				strongHoldId = cfg.getStage2HoldNum().first;
			}
			Object[] object = new Object[] { spaceObj.getLevel(), strongHoldId };
			for (Player player : atkPlayers) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.SPACE_MECHA_STRONGHOLD_DISPEAR)
						.addContents(object)
						.build());
			}
		}
	}
	
	/**
	 * 战斗结果处理(奖励、邮件处理)
	 */
	default MailRewards doBattleResult(Player leader, StrongHoldWorldPoint strongPoint, SpaceMechaStrongholdCfg strongHoldCfg, List<Player> atkPlayers, BattleOutcome battleOutcome) {
		// 总击杀怪物数量(血量)
		int totalKillCount = getTotalKillCount(battleOutcome);
		// 攻打前怪物剩余血量
		int beforeBlood = strongPoint.getRemainBlood();
		// 攻击后怪物剩余血量
		int afterBlood = (beforeBlood >= totalKillCount) ? (beforeBlood - totalKillCount) : 0;
		// 设置怪物剩余血量
		strongPoint.setRemainBlood(afterBlood > 0 ? afterBlood : 0);
		
		boolean kill = strongPoint.getRemainBlood() <= 0 && strongPoint.getHpNum() >= strongHoldCfg.getHpNumber();
		if (kill) {
			HawkTuple2<Integer, Integer> effValTuple = strongHoldCfg.randomEffect();
			strongPoint.storeEffectTuple(effValTuple);
		}
		
		for (Player atkPlayer : atkPlayers) {
			SpaceMechaService.getInstance().addAtkStrongHoldTimes(atkPlayer);
		}
		
		boolean atkWin = battleOutcome.isAtkWin();
		List<ArmyInfo> mergAllPlayerArmy = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		WorldMarchService.getInstance().sendBattleResultInfo(this, atkWin, mergAllPlayerArmy, new ArrayList<ArmyInfo>(), atkWin);
		
		MailRewards mailRewards = new MailRewards();
		List<ItemInfo> atkAwardItemList = strongHoldCfg.getAtkAwardItemList();
		if (!atkAwardItemList.isEmpty()) {
			AwardItems commonRewards = AwardItems.valueOf();
			commonRewards.addItemInfos(atkAwardItemList);
			for (Player atkPlayer : atkPlayers) {
				int awardTimes = SpaceMechaService.getInstance().getAtkStrongHoldAwardTimesToday(atkPlayer);
				SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
				List<ItemInfo> showArwards = new ArrayList<>();
				CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(atkPlayer, MechaSpaceConst.PERSONAL_STRONGHOD_AWARD_TOTAL);
				if (awardTimes < constCfg.getStrongholdAwardLimit() && customData.getValue() < constCfg.getStrongholdAwardPersonLimit()) {
					atkPlayer.dealMsg(MsgId.WORLD_AWARD_PUSH, new WorldAwardPushInvoker(atkPlayer, commonRewards, Action.SPACE_MECHA_ATK_STRONG_AWARD, false, null));
					SpaceMechaService.getInstance().addAtkStrongHoldAwardTimes(atkPlayer);
					customData.setValue(customData.getValue() + 1);
					showArwards.addAll(atkAwardItemList);
				}
				mailRewards.addSelfRewards(atkPlayer.getId(), showArwards);
				SpaceMechaService.getInstance().getGuildSpace(atkPlayer.getGuildId()).syncSpaceMacheInfo(atkPlayer.getId());
			}
		}
		
		return mailRewards;
	}
	
	/**
	 * 获取击杀总数量
	 */
	default int getTotalKillCount(BattleOutcome battleOutcome) {
		int totalKillCount = 0;
		Map<String, List<ArmyInfo>> aftArmyMapAtk = battleOutcome.getAftArmyMapAtk();
		for(Entry<String, List<ArmyInfo>> entry : aftArmyMapAtk.entrySet()){
			List<ArmyInfo> armyInfos = entry.getValue();
			int selfTotalCnt = 0;
			for (ArmyInfo armyInfo : armyInfos) {
				selfTotalCnt  += armyInfo.getKillCount();
			}
			totalKillCount += selfTotalCnt;
		}
		
		return totalKillCount;
	}

	/**
	 * 获取伤害比率
	 */
	default int getKillCount(BattleOutcome battleOutcome, Player player) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			playerKillCount += playerArmyInfo.getKillCount();
		}
		
		// 伤害比率
		return playerKillCount;
	}
	
	/**
	 * 获取受伤部队数量
	 */
	default int getWoundCount(BattleOutcome battleOutcome, Player player) {
		int woundCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			woundCount += playerArmyInfo.getWoundedCount();
		}
		return woundCount;
	}

	@Override
	default boolean needShowInGuildWar() {
		return this.isMassMarch();
	}
	
	/**
	 * 获取防守方联盟战争界面信息
	 */
	@Override
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		int terminalId = this.getMarchEntity().getTerminalId();
		int[] pos = GameUtil.splitXAndY(terminalId);
		builder.setPointType(WorldPointType.SPACE_MECHA_STRONG_HOLD);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		if (worldPoint != null && worldPoint instanceof StrongHoldWorldPoint) {
			StrongHoldWorldPoint strongPoint = (StrongHoldWorldPoint) worldPoint;
			builder.setMonsterId(strongPoint.getStrongHoldId());
		}
		return builder;
	}
	
}
