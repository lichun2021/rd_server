package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.HPGetGuildShopInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
/**
 * 联盟商店补货
 * @author Jesse
 *
 */
public class GuildAddShopItemInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 道具Id */
	private int itemId;

	/** 道具数量 */
	private int count;

	/** 协议Id */
	private int hpCode;

	public GuildAddShopItemInvoker(Player player, int itemId, int count, int hpCode) {
		this.player = player;
		this.itemId = itemId;
		this.count = count;
		this.hpCode = hpCode;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder> result = GuildService.getInstance().addGuildShopItem(player, itemId, count);
		if(result.first == Status.SysError.SUCCESS_OK_VALUE){
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_ADD_SHOP_ITEM_S_VALUE, result.second));
			return true;
		}
		player.sendError(hpCode, result.first, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
