package com.hawk.game.invoker;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.guild.manor.building.GuildManorWarehouse;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.module.PlayerManorWarehouseModule;
import com.hawk.game.player.Player;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 存款行军到达
 */
public class StoreMarchReachMsgInvoker extends HawkMsgInvoker {
	GuildManorWarehouse wareHouse;
	IWorldMarch march;
	Player player;
	PlayerManorWarehouseModule module;

	public StoreMarchReachMsgInvoker(GuildManorWarehouse wareHouse, IWorldMarch march, Player player, PlayerManorWarehouseModule module) {
		super();
		this.wareHouse = wareHouse;
		this.march = march;
		this.player = player;
		this.module = module;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		List<ItemInfo> toStoreAwardItems = march.getMarchEntity().getAwardItems().getAwardItems();
		List<ItemInfo> takeBack = wareHouse.store(player, toStoreAwardItems);
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(takeBack);
		march.getMarchEntity().setAwardItems(awardItems);

		module.syncHouseInfo();

		BehaviorLogger.log4Service(player, Source.GUILD_MANOR, Action.WARE_HOUSE_STORE_MARCH_REACH,
				Params.valueOf("marchId", march.getMarchId()), 
				Params.valueOf("resToStore", toStoreAwardItems), 
				Params.valueOf("resTakeback", takeBack),
				Params.valueOf("playerDeposit", wareHouse.playerDeposit(player.getId())));

		return true;
	}

	public GuildManorWarehouse getWareHouse() {
		return wareHouse;
	}

	public void setWareHouse(GuildManorWarehouse wareHouse) {
		this.wareHouse = wareHouse;
	}

	public IWorldMarch getMarch() {
		return march;
	}

	public void setMarch(IWorldMarch march) {
		this.march = march;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public PlayerManorWarehouseModule getModule() {
		return module;
	}

	public void setModule(PlayerManorWarehouseModule module) {
		this.module = module;
	}
	
}
