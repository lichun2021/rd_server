package com.hawk.game.world.march.impl;

import com.hawk.game.module.PlayerManorWarehouseModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;

/**
 * 去仓库存放
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class WarehouseStoreMarch extends PlayerMarch implements BasedMarch {

	public WarehouseStoreMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.WAREHOUSE_STORE;
	}

	@Override
	public void onMarchReach(Player player) {
		PlayerManorWarehouseModule module = player.getModule(GsConst.ModuleType.WARE_HOUSE);
		module.msgStoreMarchReach(this);
	}

	@Override
	public void onWorldMarchReturn(Player player) {
		PlayerManorWarehouseModule module = player.getModule(GsConst.ModuleType.WARE_HOUSE);
		module.msgStoreMarchBack(this.getMarchEntity());
	}
	
	@Override
	public void moveCityProcess(long currentTime) {
		this.onWorldMarchReturn(getPlayer());
	}
}
