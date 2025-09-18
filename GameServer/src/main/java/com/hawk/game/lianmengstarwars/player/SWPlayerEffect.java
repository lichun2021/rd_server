package com.hawk.game.lianmengstarwars.player;

import com.hawk.game.config.SWCommandCenterCfg;
import com.hawk.game.lianmengstarwars.GuildStaticInfo;
import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.worldpoint.SWCommandCenter;
import com.hawk.game.lianmengstarwars.worldpoint.SWHeadQuarters;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class SWPlayerEffect extends ISWPlayerEffect {

	public SWPlayerEffect(SWPlayerData playerData) {
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

			int result = getSource().getEffVal(effType, targetId, effParams) + getParent().getParent().getBuff().getOrDefault(effType, 0) + buildingAdd(effType, effParams);
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

	private int buildingAdd(EffType effType, EffectParams effParams) {
		int result = 0;
		int towerAdd = 0;
		SWCommandCenterCfg tcfg = SWCommandCenter.getCfg();
		if (tcfg.getControlCountBuff1Map().containsKey(effType) || tcfg.getControlCountBuff2Map().containsKey(effType)) {
			ISWWorldPoint point = getParent().getParent().getWorldPoint(effParams.getBattlePoint()).orElse(null);
			GuildStaticInfo gsinfo = getParent().getParent().getGuildStatisticMap().get(getParent().getGuildId());
			if (point instanceof SWHeadQuarters && gsinfo != null && gsinfo.getTowerCnt() > 0) {
				switch (gsinfo.getTowerCnt()) {
				case 0:
					break;
				case 1:
					towerAdd = tcfg.getControlCountBuff1Map().getOrDefault(effType, 0);
					break;
				default:// 2个或以上
					towerAdd = tcfg.getControlCountBuff2Map().getOrDefault(effType, 0);
					break;
				}
			}
		}
		return result + towerAdd;
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

}
