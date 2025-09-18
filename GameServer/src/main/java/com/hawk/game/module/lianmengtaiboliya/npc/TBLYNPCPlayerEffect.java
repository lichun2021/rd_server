package com.hawk.game.module.lianmengtaiboliya.npc;

import java.util.HashMap;
import java.util.Map;

import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayerEffect;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class TBLYNPCPlayerEffect extends ITBLYPlayerEffect {
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public TBLYNPCPlayerEffect(TBLYNPCPlayerData result) {
		super(result);
	}

	@Override
	public void resetEffectDress(Player player) {
	}

	@Override
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {
		TBLYNpcPlayer parent = (TBLYNpcPlayer) getParent();
		return parent.getSource().getEffect().getEffVal(effType, targetId, effParams);
	}

	@Override
	public int getEffectTech(int effId) {
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

	public Map<EffType, Integer> getEffectmap() {
		return effectmap;
	}

	public void setEffectmap(Map<EffType, Integer> effectmap) {
		this.effectmap = effectmap;
	}

}
