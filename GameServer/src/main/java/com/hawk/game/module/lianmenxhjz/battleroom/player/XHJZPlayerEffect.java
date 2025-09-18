package com.hawk.game.module.lianmenxhjz.battleroom.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hawk.os.HawkException;

import com.google.common.collect.Table;
import com.hawk.game.module.lianmenxhjz.battleroom.IXHJZWorldPoint;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.IXHJZBuilding;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildType;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class XHJZPlayerEffect extends IXHJZPlayerEffect {
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public XHJZPlayerEffect(XHJZPlayerData playerData) {
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
			int fuelBuf = fuelBufadd(effType, effParams);

			int result = getSource().getEffVal(effType, targetId, effParams) + effectmap.getOrDefault(effType, 0) + buildingAdd(effType, effParams) + fuelBuf;
			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int fuelBufadd(EffType effType, EffectParams effParams) {
		int fuelBuf = 0;
		if (effParams.getImarch() != null && effParams.getImarch() instanceof IXHJZWorldMarch) {
			try {
				IXHJZWorldMarch march = (IXHJZWorldMarch) effParams.getImarch();
				fuelBuf = march.getFuelBuff(effType);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return fuelBuf;
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

			if (effParams.getTroopEffType() != null) {
				Table<XHJZBuildType, EffType, Integer> specialEffectTable = getParent().getParent().getCampBase(getParent().getGuildId()).specialEffectTable;
				if (specialEffectTable.containsColumn(effType)) {
					XHJZBattleRoom room = getParent().getParent();
					Optional<IXHJZWorldPoint> pointOp = room.getWorldPoint(effParams.getBattlePoint());
					if (pointOp.isPresent() && pointOp.get() instanceof IXHJZBuilding) {
						IXHJZBuilding build = (IXHJZBuilding) pointOp.get();
						if (specialEffectTable.contains(build.getBuildType(), effType)) {
							result = result + specialEffectTable.get(build.getBuildType(), effType);
						}
					}
				}
			}

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
