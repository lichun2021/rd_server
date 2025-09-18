package com.hawk.game.module.lianmengfgyl.battleroom.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.FGYLBuildingMarchMassJoin;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class FGYLPlayerEffect extends IFGYLPlayerEffect {
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public FGYLPlayerEffect(FGYLPlayerData playerData) {
		super(playerData);
	}

	@Override
	public void resetEffectDress(Player player) {
		getSource().resetEffectDress(player);
	}

	@Override
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {
		if (effType == EffType.CITY_ENEMY_MARCH_SPD) {
			return 0;
		}
		int result = 0;
		boolean bfalse = false;
		if (effParams.getImarch() != null && effParams.getImarch() instanceof FGYLBuildingMarchMassJoin) {
			FGYLBuildingMarchMassJoin march = (FGYLBuildingMarchMassJoin) effParams.getImarch();
			bfalse = march.getFgylSkill() > 0;
		}

		if (bfalse) {
			try {
				FGYLBuildingMarchMassJoin march = (FGYLBuildingMarchMassJoin) effParams.getImarch();
				IFGYLWorldMarch leaderMarch = march.leaderMarch().get();
				result = leaderMarch.getParent().getEffect().getEffVal(effType, targetId, leaderMarch.getMarchEntity().getEffectParams());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else {
			int buildAdd = 0;
			Optional<IFGYLWorldPoint> pointOp = getParent().getParent().getWorldPoint(effParams.getBattlePoint());
			if (pointOp.isPresent() && pointOp.get() instanceof IFGYLBuilding) {
				IFGYLBuilding build = (IFGYLBuilding) pointOp.get();
				buildAdd = build.getCfg().getEffectList().getOrDefault(effType, 0);
			}
			result = getSource().getEffVal(effType, targetId, effParams) + effectmap.getOrDefault(effType, 0) + buildingAdd(effType, effParams) + buildAdd;

		}
		return result;
	}

	@Override
	public int getEffectTech(int effId) {
		try {
			return getSource().getEffectTech(effId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int buildingAdd(EffType effType, EffectParams effParams) {
		int result = 0;
		try {
			result = getParent().getParent().getCampBase(getParent().getGuildId()).battleEffVal.getOrDefault(effType, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return result;
	}

	@Override
	public int getEffVal(EffType effType) {
		return this.getEffVal(effType, EffectParams.getDefaultVal());
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		return this.getEffVal(effType, EffectParams.getDefaultVal());
	}

	public PlayerEffect getSource() {
		return getParent().getSource().getEffect();
	}

}
