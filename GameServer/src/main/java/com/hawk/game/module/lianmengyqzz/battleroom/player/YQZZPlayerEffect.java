package com.hawk.game.module.lianmengyqzz.battleroom.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZPylonBuffCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBase;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZPylon;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class YQZZPlayerEffect extends IYQZZPlayerEffect {
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public YQZZPlayerEffect(YQZZPlayerData playerData) {
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
		try {
			int effteshude = 0;
			if (effType == EffType.DEAD_TO_HURT_1522) {
				Optional<IYQZZWorldPoint> pointOp = getParent().getParent().getWorldPoint(effParams.getBattlePoint());
				if (pointOp.isPresent() && pointOp.get() instanceof IYQZZBuilding) {
					IYQZZBuilding build = (IYQZZBuilding) pointOp.get();
					effteshude = build.getBuildTypeCfg().getHeal();
				}
				if (pointOp.isPresent() && pointOp.get() instanceof YQZZPylon) {
					YQZZPylon build = (YQZZPylon) pointOp.get();
					effteshude = build.getCfg().getDeadToWound();
				}
			}

			int order = getParent().getBase().getOrderCollection().getEffectVal(effType, effParams);
			int result = getSource().getEffVal(effType, targetId, effParams);// + getParent().getParent().getBuff().getOrDefault(effType, 0) + buildingAdd(effType);
			int extryVal = effectmap.getOrDefault(effType, 0);

			int pylonVal = pylongBuff(effType);

			return result + buildingAdd(effType) + effteshude + extryVal + order + pylonVal;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int pylongBuff(EffType effType) {
		YQZZBase baseByCamp = getParent().getParent().getBaseByCamp(getParent().getCamp());
		if (baseByCamp.pylonCnt == 0) {
			return 0;
		}
		return YQZZPylonBuffCfg.getEffectVal(baseByCamp.pylonCnt, effType);
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

	private int buildingAdd(EffType effType) {
		int result = getParent().getParent().getCampBase(getParent().getGuildId()).battleEffVal.getOrDefault(effType, 0);
		return result;
	}

	@Override
	public int getEffVal(EffType effType) {
		return getEffVal(effType, EffectParams.getDefaultVal());
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		return getEffVal(effType, EffectParams.getDefaultVal());
	}

	public PlayerEffect getSource() {
		return getParent().getSource().getEffect();
	}

	public void putEffVal(EffType effType, int val) {
		effectmap.put(effType, val);
	}
}
