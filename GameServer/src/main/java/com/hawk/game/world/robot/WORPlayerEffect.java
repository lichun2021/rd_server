package com.hawk.game.world.robot;

import java.util.Objects;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class WORPlayerEffect extends PlayerEffect {
	private String sourPlayerId;

	public WORPlayerEffect(WORPlayerData playerData) {
		super(playerData);
		sourPlayerId = playerData.getSourPlayerId();
	}

	@Override
	public void init() {
	}

	@Override
	public int getEffVal(EffType effType, EffectParams effParams) {
		Player sourcePlayer = GlobalData.getInstance().makesurePlayer(sourPlayerId);
		if (Objects.isNull(sourcePlayer)) {
			return 0;
		}
		return sourcePlayer.getEffect().getEffVal(effType, effParams);
	}

	@Override
	public int getEffVal(EffType effType) {

		return getEffVal(effType, EffectParams.getDefaultVal());
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {

		return getEffVal(effType, EffectParams.getDefaultVal());
	}

}
