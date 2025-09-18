package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLBattleCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.roomstate.FGYLGameOver;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.chat.ChatParames;

/**
 * 1. 1个5级岛屿（岛上带建筑）
 *
 */
public class FGYLHeadQuarter extends IFGYLBuilding {
	private Set<Integer> killEnemyList = new HashSet<>();
	boolean notice480 = true;

	public FGYLHeadQuarter(FGYLBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		if (notice480 && getParent().getCurTimeMil() > getProtectedEndTime()) {
			notice480 = false;
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.FGYL_START)
					.build();
			getParent().addWorldBroadcastMsg(parames);
		}

		List<FGYLTower> towerList = getParent().getFGYLBuildingByClass(FGYLTower.class);
		for (FGYLTower tower : towerList) {
			if (tower.getState() == FGYLBuildState.ZHAN_LING && !killEnemyList.contains(tower.getCfgId())) {
				killEnemyList.add(tower.getCfgId());
				this.worldPointUpdate();
			}
		}
		if (getState() == FGYLBuildState.ZHAN_LING) {
			getParent().setWinCamp(FGYL_CAMP.A);
			getParent().setGameOver(true);
			getParent().setState(new FGYLGameOver(getParent()));
		}
		return super.onTick();
	}

	@Override
	public WorldPointPB.Builder toBuilder(IFGYLPlayer viewer) {
		// TODO Auto-generated method stub
		WorldPointPB.Builder builder = super.toBuilder(viewer);
		builder.addAllFgylKillEnemy(killEnemyList);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IFGYLPlayer viewer) {
		// TODO Auto-generated method stub
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewer);
		builder.addAllFgylKillEnemy(killEnemyList);
		return builder;
	}

	@Override
	public TemporaryMarch getYuriMarch() {
		TemporaryMarch asmarch = super.getYuriMarch();
		NpcPlayer ghostplayer = (NpcPlayer) asmarch.getPlayer();
		List<FGYLTower> towerList = getParent().getFGYLBuildingByClass(FGYLTower.class);
		FGYLBattleCfg battlecfg = getParent().getCfg();
		int cnt = 0;
		for (FGYLTower tower : towerList) {
			if (tower.getState() == FGYLBuildState.YOULING) {
				cnt++;
			}
		}
		for (Entry<EffType, Integer> ent : battlecfg.getSpecialEffectList(cnt).entrySet()) {
			ghostplayer.addEffectVal(ent.getKey(), ent.getValue());
		}

		return asmarch;
	}

	public Set<Integer> getKillEnemyList() {
		return killEnemyList;
	}

	public void setKillEnemyList(Set<Integer> killEnemyList) {
		this.killEnemyList = killEnemyList;
	}

}
