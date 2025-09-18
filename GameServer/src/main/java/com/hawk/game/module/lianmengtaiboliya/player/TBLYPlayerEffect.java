package com.hawk.game.module.lianmengtaiboliya.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYFuelBank;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYNian;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class TBLYPlayerEffect extends ITBLYPlayerEffect {
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public TBLYPlayerEffect(TBLYPlayerData playerData) {
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

		TBLYBattleRoom room = getParent().getParent();
		Optional<ITBLYWorldPoint> pointOp = room.getWorldPoint(effParams.getBattlePoint());
		switch (effType) { // 新增号令，提升在泰伯利亚矿的战斗能力，需要以下3个作用号
		case TBLY_EFF_1719:
		case TBLY_EFF_1720:
		case TBLY_EFF_1721:
			if (!pointOp.isPresent() || !(pointOp.get() instanceof TBLYFuelBank)) {
				return 0;
			}
			break;
		default:
			break;
		}

		try {
			int result = getSource().getEffVal(effType, targetId, effParams) + effectmap.getOrDefault(effType, 0) + buildingAdd(effType);
			if(WarEff.ATK.check(effParams.getTroopEffType()) && pointOp.isPresent() && pointOp.get() instanceof ITBLYBuilding ){
				ITBLYBuilding build = (ITBLYBuilding) pointOp.get();
				result  = result + build.getAtkbuff(effType);
			}
			// 加小龙buff
			{
				int nianKillCount = getParent().getCamp() == CAMP.A ? getParent().getParent().campANianKillCount : getParent().getParent().campBNianKillCount;
				result = result + TBLYNian.getCfg().getKillBuff(effType, nianKillCount);
			}
			// 号令加成
			{
				int val = getParent().getParent().getOrderEffect(getParent().getCamp(), effType);
				result = result + val;
			}
			result += room.getCurBuff530Val(effType);
			return result;

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

	private int buildingAdd(EffType effType) {
		int result = 0;

		for (ITBLYBuilding build : getParent().getParent().getTBLYBuildingList()) {
			result += build.getControlBuff(getParent(), effType);
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
		return ((TBLYPlayer) getParent()).getSource().getEffect();
	}

}
