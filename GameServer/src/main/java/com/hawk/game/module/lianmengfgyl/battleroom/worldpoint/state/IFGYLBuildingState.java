package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.state;

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
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLMassJoinMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLBuildState;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;

public abstract class IFGYLBuildingState {
	private IFGYLBuilding parent;

	public IFGYLBuildingState(IFGYLBuilding build) {
		this.parent = build;
	}

	public abstract boolean onTick();

	public abstract FGYLBuildState getState();

	public abstract void init();

	public IFGYLBuilding getParent() {
		return parent;
	}

	public void onMarchReach(IFGYLWorldMarch leaderMarch) {
	}

}
