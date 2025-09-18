package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZCommandCenter;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;

public abstract class IYQZZBuildingState {
	private IYQZZBuilding parent;

	public IYQZZBuildingState(IYQZZBuilding build) {
		this.parent = build;
	}

	public abstract boolean onTick();

	public abstract YQZZBuildState getState();
	public abstract void init();
	public IYQZZBuilding getParent() {
		return parent;
	}
	
	public void onMarchReach(IYQZZWorldMarch leaderMarch) {
		IYQZZPlayer player = leaderMarch.getParent();
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<IYQZZWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (IYQZZWorldMarch iWorldMarch : atkMarchList) {
			// 去程到达目标点，变成停留状态
			iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
			iWorldMarch.getMarchEntity().setReachTime(leaderMarch.getMarchEntity().getEndTime());
			iWorldMarch.updateMarch();
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		List<IWorldMarch> defMarchs = new ArrayList<>();
		// 防守方行军
		List<IYQZZWorldMarch> defMarchList = getParent().getParent().getPointMarches(getParent().getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		IYQZZWorldMarch enemyLeadmarch = getParent().getLeaderMarch();
		if (Objects.nonNull(enemyLeadmarch)) {// 队长排第一
			defMarchList.remove(enemyLeadmarch);
			defMarchList.add(0, enemyLeadmarch);
		}
		for (IYQZZWorldMarch iWorldMarch : defMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}

		if (defMarchs.isEmpty()) {
			getParent().setLeaderMarch(leaderMarch);
			for (IYQZZWorldMarch iWorldMarch : atkMarchList) {
				iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				iWorldMarch.updateMarch();
			}
			return;
		}
		
		if (!Objects.equals(player.getGuildId(), defPlayers.get(0).getGuildId()) && Objects.equals(player.getMainServerId(), defPlayers.get(0).getMainServerId())) { // 同国家不能攻击同建筑
			for (IYQZZWorldMarch iWorldMarch : atkMarchList) {
				iWorldMarch.onMarchReturn(getParent().getPointId(), iWorldMarch.getParent().getPointId(), iWorldMarch.getArmys());
			}
			return;
		}

		if (Objects.equals(player.getGuildId(), defPlayers.get(0).getGuildId())) { // 同阵营
			try {
				getParent().assitenceWarPoint(atkMarchList, defMarchList);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			// for (IYQZZWorldMarch iWorldMarch : atkMarchList) {
			// iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			// iWorldMarch.updateMarch();
			// }
			return;
		}

		BattleType btype = BattleConst.BattleType.ATTACK_PRESIDENT;
		if (getParent() instanceof YQZZCommandCenter) {
			btype = BattleConst.BattleType.ATTACK_PRESIDENT_TOWER;
		}
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(btype, getParent().getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setYQZZMail(getParent().getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setYqzzNationMilly(true);
		battleOutcome.setDuntype(DungeonMailType.YQZZ);
		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		/********* 击杀/击伤部队数据 *********/
		getParent().getParent().calcKillAndHurtPower(battleOutcome, atkPlayers, defPlayers);
		/********* 击杀/击伤部队数据 *********/

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		// 计算损失兵力
		List<ArmyInfo> atkArmyLeft = WorldUtil.mergAllPlayerArmy(atkArmyLeftMap);
		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		// 计算损失兵力
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);

		// 发送战斗结果给前台播放动画
		leaderMarch.sendBattleResultInfo(isAtkWin, atkArmyLeft, defArmyList);

		for (IYQZZWorldMarch iWorldMarch : atkMarchList) {
			iWorldMarch.notifyMarchEvent(MarchEvent.MARCH_DELETE);
		}

		leaderMarch.updateDefMarchAfterWar(atkMarchList, atkArmyLeftMap);
		leaderMarch.updateDefMarchAfterWar(defMarchList, defArmyLeftMap);

		List<IYQZZWorldMarch> winMarches = null;
		List<IYQZZWorldMarch> losMarches = null;

		if (isAtkWin) {
			getParent().setLeaderMarch(leaderMarch); 
			winMarches = atkMarchList;
			losMarches = defMarchList;
		} else {
			winMarches = defMarchList;
			losMarches = atkMarchList;
		}

		for (IYQZZWorldMarch atkM : winMarches) {
			atkM.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			atkM.updateMarch();
		}
		for (IYQZZWorldMarch defM : losMarches) {
			defM.onMarchReturn(getParent().getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}

		FightMailService.getInstance().sendFightMail(getParent().getPointType().getNumber(), battleIncome, battleOutcome, null);
	}

	public void fillBuilder(WorldPointPB.Builder builder) {
	}

	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {

	}
}
