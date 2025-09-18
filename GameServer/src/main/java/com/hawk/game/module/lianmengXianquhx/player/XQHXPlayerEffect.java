package com.hawk.game.module.lianmengXianquhx.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZFuelBank;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.DYZZEnergyWell;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZInTower;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZOutTower;
import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.XQHXConst;
import com.hawk.game.module.lianmengXianquhx.XQHXGuildBaseInfo;
import com.hawk.game.module.lianmengXianquhx.worldpoint.IXQHXBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class XQHXPlayerEffect extends IXQHXPlayerEffect {
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public XQHXPlayerEffect(XQHXPlayerData playerData) {
		super(playerData);
	}

	@Override
	public void resetEffectDress(Player player) {
		getSource().resetEffectDress(player);
	}

	@Override
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {
		if(effType == EffType.XQHX_10052){
			
		}
		
		switch (effType) {
		case XQHX_10052:
		case XQHX_10053:
		case XQHX_10054:
		case XQHX_10055:
		case XQHX_10056:
		case XQHX_10057:
			XQHXBattleRoom room = getParent().getParent();
			Optional<IXQHXWorldPoint> pointOp = room.getWorldPoint(effParams.getBattlePoint());
			if (!pointOp.isPresent() || !(pointOp.get() instanceof IXQHXBuilding)) {
				return 0;
			}
			IXQHXBuilding build = (IXQHXBuilding) pointOp.get();
			if(build.getBuildTypeId() != XQHXConst.BuildType1){
				return 0;
			}
			break;
		default:
			break;
		}
		
		
		if (effType == EffType.CITY_ENEMY_MARCH_SPD) {
			return 0;
		}

		try {
			int result = getSource().getEffVal(effType, targetId, effParams) + effectmap.getOrDefault(effType, 0) + getSource().getXqhxBuff(effType);
			XQHXGuildBaseInfo campBase = getParent().getParent().getCampBase(getParent().getCamp());
			int orderBuff = campBase.getOrderEffVal(effType);
			int battleBuff = campBase.battleEffVal.getOrDefault(effType,0);
			return result + orderBuff + battleBuff;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
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
