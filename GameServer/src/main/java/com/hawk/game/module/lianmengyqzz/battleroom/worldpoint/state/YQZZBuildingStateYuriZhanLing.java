package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.YQZZDeclareWar;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;

public class YQZZBuildingStateYuriZhanLing extends IYQZZBuildingState {

	public YQZZBuildingStateYuriZhanLing(IYQZZBuilding build) {
		super(build);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMarchReach(IYQZZWorldMarch leaderMarch) {
		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();

		// 进攻方行军
		List<IYQZZWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (IYQZZWorldMarch iWorldMarch : atkMarchList) {
//			// 去程到达目标点，变成停留状态
//			iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
//			iWorldMarch.getMarchEntity().setReachTime(leaderMarch.getMarchEntity().getEndTime());
//			iWorldMarch.updateMarch();
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}
		

		// 战斗信息
		PveBattleIncome battleIncome = BattleService.getInstance().initFoggyBattleData(BattleConst.BattleType.ATTACK_FOGGY, this.getEntity(), atkPlayers, atkMarchs);
		battleIncome.setMonsterId(getParent().getCfgId());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.YQZZ);
		/**********************战斗数据组装及战斗***************************/

		// 战斗结果处理
		// MailRewards mailRewards = doFoggyBattleResult(battleOutcome, atkPlayers, this.getMonsterId());
		// 攻击方玩家部队
		List<ArmyInfo> mergAllPlayerArmy = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		// 播放战斗动画
		leaderMarch.sendBattleResultInfo(battleOutcome.isAtkWin(), mergAllPlayerArmy, new ArrayList<ArmyInfo>());
		// 据点PVE战斗邮件发放
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.YQZZ_BUILD_PVE, battleIncome, battleOutcome, null);

		
		leaderMarch.updateDefMarchAfterWar(atkMarchList, battleOutcome.getAftArmyMapAtk());

		if (battleOutcome.isAtkWin()) {
			getParent().setLeaderMarch(leaderMarch);
			for (IYQZZWorldMarch iWorldMarch : atkMarchList) {
				iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				iWorldMarch.updateMarch();
			}

			getParent().setStateObj(new YQZZBuildingStateZhanLingZhong(getParent()));
			
		} else {
			// 队员行军返回
			for (IYQZZWorldMarch tmpMarch : atkMarchList) {
				tmpMarch.onMarchReturn(getParent().getPointId(), tmpMarch.getParent().getPointId(), tmpMarch.getArmys());
			}
		}

	}

	@Override
	public boolean onTick() {
		return true;
	}

	@Override
	public YQZZBuildState getState() {
		// TODO Auto-generated method stub
		return YQZZBuildState.ZHAN_LING;
	}

	@Override
	public void init() {
		YQZZDeclareWar lastguild = YQZZDeclareWar.newBuilder().setGuildId(YQZZConst.YURI_GUILD).build();
		getParent().setOnwerGuild(lastguild);

	}

	private WorldPoint getEntity() {
		WorldPoint worldPoint = new WorldPoint();
		worldPoint.setX(getParent().getX());
		worldPoint.setY(getParent().getY());
		worldPoint.setId(getParent().getPointId());
		worldPoint.initFoggyInfo(getParent().getFoggyInfoObj());
		worldPoint.setMonsterId(getParent().getFoggyFortressId());
//		worldPoint.setMonsterId(101);
		return worldPoint;
	}

}
