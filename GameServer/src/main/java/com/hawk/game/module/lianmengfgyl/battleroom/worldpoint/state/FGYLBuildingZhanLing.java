package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.state;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLBuildState;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.world.march.IWorldMarch;

public class FGYLBuildingZhanLing extends IFGYLBuildingState{

	public FGYLBuildingZhanLing(IFGYLBuilding build) {
		super(build);
		// TODO Auto-generated constructor stub
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

		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (IFGYLWorldMarch iWorldMarch : atkMarchList) {
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}
		for (IFGYLWorldMarch atkmarch : atkMarchList) {
			atkmarch.onMarchReturn(getParent().getPointId(), atkmarch.getParent().getPointId(), atkmarch.getArmys());
		}
	}
	@Override
	public boolean onTick() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FGYLBuildState getState() {
		return FGYLBuildState.ZHAN_LING;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

}
