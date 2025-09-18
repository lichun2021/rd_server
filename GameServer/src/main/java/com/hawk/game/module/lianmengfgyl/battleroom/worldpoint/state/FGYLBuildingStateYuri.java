package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLBuildState;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;

public class FGYLBuildingStateYuri extends IFGYLBuildingState {

	private int remainBlood;
	private int maxBlood;

	public FGYLBuildingStateYuri(IFGYLBuilding build) {
		super(build);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTick() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMarchReach(IFGYLWorldMarch leaderMarch) {
		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<IFGYLWorldMarch> atkMarchList = new ArrayList<>();
		List<IWorldMarch> defMarchList = new ArrayList<>();
		TemporaryMarch yuriMarch = getParent().getYuriMarch();
		defMarchList.add(yuriMarch);

		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (IFGYLWorldMarch iWorldMarch : atkMarchList) {
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}

		// 战斗信息
		PveBattleIncome battleIncome = BattleService.getInstance().initXZQPveBattleData(BattleConst.BattleType.ATTACK_XZQ_PVE, getParent().getPointId(), yuriMarch, atkMarchs, 0);
		battleIncome.getBattle().setDuntype(DungeonMailType.FGYL);
		battleIncome.getAtkCalcParames().setConvertRate(GsConst.RANDOM_MYRIABIT_BASE - getParent().getParent().getCfg().getBattleWounded());
		battleIncome.setMonsterId(getParent().getCfgId());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.FGYL);
		battleOutcome.setAtkWin(true);
		Map<String,Integer> fgylSkill = new HashMap<>();
		for (IFGYLWorldMarch iWorldMarch : atkMarchList) {
			fgylSkill.put(iWorldMarch.getPlayerId(), iWorldMarch.getFgylSkill());
		}
		battleOutcome.setFgylSkill(fgylSkill);
		/**********************战斗数据组装及战斗***************************/

		// 战斗结果处理
		// MailRewards mailRewards = doFoggyBattleResult(battleOutcome, atkPlayers, this.getMonsterId());
		// 攻击方玩家部队
		List<ArmyInfo> mergAllPlayerArmy = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		// 播放战斗动画
		leaderMarch.sendBattleResultInfo(battleOutcome.isAtkWin(), mergAllPlayerArmy, new ArrayList<ArmyInfo>());

		leaderMarch.updateDefMarchAfterWar(atkMarchList, battleOutcome.getAftArmyMapAtk());

		for (IFGYLWorldMarch atkmarch : atkMarchList) {
			atkmarch.onMarchReturn(getParent().getPointId(), atkmarch.getParent().getPointId(), atkmarch.getArmys());
		}

		int blood = getTotalKillCount(battleOutcome);
		remainBlood = Math.max(0, remainBlood - blood);// 扣血
		if (remainBlood <= 0) {
			getParent().setStateObj(new FGYLBuildingZhanLing(getParent()));
		}

		for (IFGYLWorldMarch atkm : atkMarchList) {
			int playerKillCount = getKillCount(battleOutcome, atkm.getParent());
			getParent().addHurtVal(atkm.getParent(), playerKillCount);
			atkm.setUseHonor(0);
		}
		battleOutcome.setFgylMaxBlood(maxBlood);
		battleOutcome.setFgylRemainBlood(remainBlood);
		battleOutcome.setFgylKillBlood(blood);
		// 据点PVE战斗邮件发放
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.FGYL_BUILD_PVE, battleIncome, battleOutcome, null);
	}

	/**
	 * 获取伤害比率
	 */
	int getKillCount(BattleOutcome battleOutcome, IFGYLPlayer player) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			playerKillCount += playerArmyInfo.getKillCount();
		}
		// 伤害比率
		return playerKillCount;
	}

	int getTotalKillCount(BattleOutcome battleOutcome) {
		int totalKillCount = 0;
		Map<String, List<ArmyInfo>> aftArmyMapAtk = battleOutcome.getAftArmyMapAtk();
		for (Entry<String, List<ArmyInfo>> entry : aftArmyMapAtk.entrySet()) {
			List<ArmyInfo> armyInfos = entry.getValue();
			int selfTotalCnt = 0;
			for (ArmyInfo armyInfo : armyInfos) {
				selfTotalCnt += armyInfo.getKillCount();
			}
			totalKillCount += selfTotalCnt;
		}

		return totalKillCount;
	}

	@Override
	public FGYLBuildState getState() {
		return FGYLBuildState.YOULING;
	}

	@Override
	public void init() {

		maxBlood = getParent().getCfg().getMaxHp();
		remainBlood = maxBlood;
	}



	public int getRemainBlood() {
		return remainBlood;
	}

	public void setRemainBlood(int remainBlood) {
		this.remainBlood = remainBlood;
	}

	public int getMaxBlood() {
		return maxBlood;
	}

	public void setMaxBlood(int maxBlood) {
		this.maxBlood = maxBlood;
	}

}
