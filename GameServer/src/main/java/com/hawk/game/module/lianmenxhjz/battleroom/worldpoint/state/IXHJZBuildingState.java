package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZMassJoinMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.IXHJZBuilding;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.XHJZQuateredState;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;

public abstract class IXHJZBuildingState {
	private IXHJZBuilding parent;

	public IXHJZBuildingState(IXHJZBuilding build) {
		this.parent = build;
	}

	public abstract boolean onTick();

	public abstract XHJZBuildState getState();

	public abstract void init();

	public IXHJZBuilding getParent() {
		return parent;
	}

	public void onMarchReach(IXHJZWorldMarch leaderMarch) {
		List<IXHJZWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		// 去程到达目标点，变成停留状态
		for (IXHJZWorldMarch iWorldMarch : atkMarchList) {
			iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
			iWorldMarch.getMarchEntity().setReachTime(leaderMarch.getMarchEntity().getEndTime());
			iWorldMarch.updateMarch();
		}

		List<IXHJZWorldMarch> defMarchList = getParent().getParent().getPointMarches(getParent().getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Collections.sort(defMarchList, Comparator.comparingLong(IXHJZWorldMarch::getReachTime).reversed());
		if (defMarchList.isEmpty()) {
			getParent().setLeaderMarch(leaderMarch);
			for (IXHJZWorldMarch iWorldMarch : atkMarchList) {
				iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				iWorldMarch.updateMarch();
			}
			return;
		}

		if (Objects.equals(leaderMarch.getParent().getGuildId(), defMarchList.get(0).getParent().getGuildId())) { // 同阵营
			try {
				for (IXHJZWorldMarch iWorldMarch : atkMarchList) {
					iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
					iWorldMarch.updateMarch();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return;
		}

		for (IXHJZWorldMarch defMarch : defMarchList) {
			if (defMarch instanceof IXHJZMassJoinMarch) {
				continue;
			}
			boolean isAtkWin = doBattle(leaderMarch, defMarch);
			if (!isAtkWin) {
				return;
			}
		}
	}

	private boolean doBattle(IXHJZWorldMarch leaderMarch, IXHJZWorldMarch defMarch) {
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<IXHJZWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (IXHJZWorldMarch iWorldMarch : atkMarchList) {
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		List<IWorldMarch> defMarchs = new ArrayList<>();
		// 防守方行军
		List<IXHJZWorldMarch> defMarchList = new ArrayList<>();
		defMarchList.add(defMarch);
		defMarchList.addAll(defMarch.getMassJoinMarchs(true));
		for (IXHJZWorldMarch iWorldMarch : defMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}

		BattleType btype = BattleConst.BattleType.ATTACK_PRESIDENT;
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(btype, getParent().getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setXHJZMail(getParent().getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.XHJZ);
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

		for (IXHJZWorldMarch iWorldMarch : atkMarchList) {
			iWorldMarch.notifyMarchEvent(MarchEvent.MARCH_DELETE);
		}

		leaderMarch.updateDefMarchAfterWar(atkMarchList, atkArmyLeftMap, battleOutcome.getBattleArmyMapAtk());
		leaderMarch.updateDefMarchAfterWar(defMarchList, defArmyLeftMap, battleOutcome.getBattleArmyMapDef());

		List<IXHJZWorldMarch> winMarches = null;
		List<IXHJZWorldMarch> losMarches = null;

		if (isAtkWin) {
			getParent().setLeaderMarch(leaderMarch);
			winMarches = atkMarchList;
			losMarches = defMarchList;
		} else {
			winMarches = defMarchList;
			losMarches = atkMarchList;
		}

		for (IXHJZWorldMarch atkM : winMarches) {
			if (atkM.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				atkM.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				atkM.updateMarch();
			}
		}
		for (IXHJZWorldMarch defM : losMarches) {
			if (defM.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				defM.onMarchReturn(getParent().getPointId(), defM.getParent().getPointId(), defM.getArmys());
			}
		}

		FightMailService.getInstance().sendFightMail(WorldPointType.XHJZ_BUILDING_VALUE, battleIncome, battleOutcome, null);
		return isAtkWin;
	}

	public void fillBuilder(WorldPointPB.Builder builder) {
	}

	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {

	}

	public XHJZQuateredState getMarchQuateredStatus(IXHJZWorldMarch march) {
		return XHJZQuateredState.getDefaultInstance();
		
	}

}
