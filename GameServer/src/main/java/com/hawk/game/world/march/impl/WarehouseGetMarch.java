package com.hawk.game.world.march.impl;

import com.hawk.game.module.PlayerManorWarehouseModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;

/**
 * 去仓库取回
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class WarehouseGetMarch extends PlayerMarch implements BasedMarch {

	public WarehouseGetMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.WAREHOUSE_GET;
	}

	@Override
	public void onMarchReach(Player player) {
		PlayerManorWarehouseModule module = player.getModule(GsConst.ModuleType.WARE_HOUSE);
		module.msgTakeMarchReach(this);
	}

	@Override
	public void onWorldMarchReturn(Player player) {
		PlayerManorWarehouseModule module = player.getModule(GsConst.ModuleType.WARE_HOUSE);
		module.msgTakeMarchBack(this.getMarchEntity());
	}

	@Override
	public void moveCityProcess(long currentTime) {
		this.onWorldMarchReturn(getPlayer());
	}
}
