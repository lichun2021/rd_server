package com.hawk.game.world.march.impl;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;

/**
 * 抓将遣返
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class CaptiveReleaseMarch extends PlayerMarch implements BasedMarch{

	public CaptiveReleaseMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.CAPTIVE_RELEASE;
	}

	@Override
	public void onMarchReach(Player player) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onWorldMarchReturn(Player player) {
		// TODO Auto-generated method stub
		
	}
}
