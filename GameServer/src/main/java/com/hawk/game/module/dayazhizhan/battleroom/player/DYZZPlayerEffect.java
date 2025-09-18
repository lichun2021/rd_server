package com.hawk.game.module.dayazhizhan.battleroom.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZAreaCfg;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZFuelBank;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.DYZZEnergyWell;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZInTower;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZOutTower;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.IDYZZTower;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class DYZZPlayerEffect extends IDYZZPlayerEffect {
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public DYZZPlayerEffect(DYZZPlayerData playerData) {
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
			DYZZBattleRoom room = getParent().getParent();
			Optional<IDYZZWorldPoint> pointOp = room.getWorldPoint(effParams.getBattlePoint());
			switch (effType) {
			case DYZZ_9022:
			case DYZZ_9023:
			case DYZZ_9024:
				if (!pointOp.isPresent() || !(pointOp.get() instanceof DYZZOutTower)) {
					return 0;
				}
				break;
			case DYZZ_9025:
			case DYZZ_9026:
			case DYZZ_9027:
				if (!pointOp.isPresent() || !(pointOp.get() instanceof DYZZInTower)) {
					return 0;
				}
				break;
			case DYZZ_9028:
			case DYZZ_9029:
			case DYZZ_9030:
				if (!pointOp.isPresent() || !(pointOp.get() instanceof IDYZZFuelBank)) {
					return 0;
				}
				break;
			case DYZZ_9031:
			case DYZZ_9032:
			case DYZZ_9033:
				if (!pointOp.isPresent() || !(pointOp.get() instanceof DYZZEnergyWell)) {
					return 0;
				}
				break;
			default:
				break;
			}

			int extryVal = effectmap.getOrDefault(effType, 0);
			// int result = getSource().getEffVal(effType, effParams) + effectmap.getOrDefault(effType, 0) ;
			// 英雄官职增益
			int heroOffice = getHerosOfficeEffVal(effType,effParams);
			// 英雄出征
			int effHero = getHeroMarchEffVal(effType, effParams);
			// 神兽出征
			int effSS = getSuperSoldierMarchEffVal(effType, effParams.getSuperSoliderId());

			// 号令加成
			int orderval = room.getOrderEffect(getParent().getCamp(), effType);

			int towerVal = 0;
			if (pointOp.isPresent()) {
				IDYZZWorldPoint point = pointOp.get();
				// TODO 配置值, 区域, 塔buff
				if (point instanceof IDYZZTower && room.getCurTimeMil() < room.getBattleStartTime()) {
					IDYZZTower tower = (IDYZZTower) point;
					if (getParent().getCamp() == tower.getBornCamp()) {
						towerVal = tower.getEffVal(effType);
					}
				}
			}

			int areaVal = 0;
			DYZZAreaCfg acfg = DYZZAreaCfg.getPointArea(effParams.getBattlePoint());
			if (Objects.nonNull(acfg) && !getParent().getParent().isNoBuffArea(acfg.getArea()) && acfg.getCamp() == getParent().getCamp().intValue()) {
				areaVal = acfg.getCollectBuffMap().getOrDefault(effType, 0);
			}

			int baeBuff = getParent().getParent().getCfg().getBaseadditionBuffMap().getOrDefault(effType, 0);
			int roguebuff = getParent().getRogueCollec().getEffVal(effType);
			return extryVal + baeBuff + heroOffice + effHero + effSS + orderval + towerVal + areaVal + roguebuff;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
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

	public void putEffVal(EffType effType, int val) {
		effectmap.put(effType, val);
	}
}
