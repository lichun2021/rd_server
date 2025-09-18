package com.hawk.game.lianmengjunyan.player;

import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class LMJYPlayerEffect extends ILMJYPlayerEffect {
	private PlayerEffect source;

	public LMJYPlayerEffect(LMJYPlayerData playerData) {
		super(playerData);
		source = playerData.getSource().getPlayerEffect();
	}

	@Override
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {
		try {
			return source.getEffVal(effType, targetId, effParams);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public int getEffectTech(int effId) {
		try {
			return source.getEffectTech(effId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getEffVal(EffType effType) {
		try {
			return source.getEffVal(effType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		try {
			return source.getEffVal(effType, targetId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
