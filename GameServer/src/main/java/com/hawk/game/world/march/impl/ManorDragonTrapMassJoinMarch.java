package com.hawk.game.world.march.impl;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.ManorMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

/**
 * 集结攻占联盟领地参与者
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class ManorDragonTrapMassJoinMarch extends PassiveMarch implements MassJoinMarch, ManorMarch{

	public ManorDragonTrapMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.DRAGON_ATTACT_MASS_JOIN;
	}

	@Override
	public void onMarchReach(Player player) {
		MassJoinMarch.super.onMarchReach(player);
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
	}
	
	
	


}
