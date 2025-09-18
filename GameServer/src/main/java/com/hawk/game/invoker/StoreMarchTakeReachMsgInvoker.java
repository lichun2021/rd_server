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

public class StoreMarchTakeReachMsgInvoker extends HawkMsgInvoker {
	IWorldMarch march;
	GuildManorWarehouse wareHouse;
	Player player;
	PlayerManorWarehouseModule module;

	public StoreMarchTakeReachMsgInvoker(IWorldMarch march, GuildManorWarehouse wareHouse, Player player, PlayerManorWarehouseModule module) {
		super();
		this.march = march;
		this.wareHouse = wareHouse;
		this.player = player;
		this.module = module;
	}

	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		AwardItems awardItems = march.getMarchEntity().getAwardItems();
		List<ItemInfo> itemInfos = wareHouse.take(player, awardItems.getAwardItems());

		awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(itemInfos);
		march.getMarchEntity().setAwardItems(awardItems);
		module.syncHouseInfo();

		BehaviorLogger.log4Service(player, Source.GUILD_MANOR,
				Action.WARE_HOUSE_TAKE_MARCH_REACH,
				Params.valueOf("marchId", march.getMarchId()), 
				Params.valueOf("resTake", awardItems),
				Params.valueOf("playerDeposit", wareHouse.playerDeposit(player.getId())));

		return true;
	}

	public IWorldMarch getMarch() {
		return march;
	}

	public void setMarch(IWorldMarch march) {
		this.march = march;
	}

	public GuildManorWarehouse getWareHouse() {
		return wareHouse;
	}

	public void setWareHouse(GuildManorWarehouse wareHouse) {
		this.wareHouse = wareHouse;
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
