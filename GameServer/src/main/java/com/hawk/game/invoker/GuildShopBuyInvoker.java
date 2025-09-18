package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.GuildShopCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.HPGetGuildShopInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst.GuildConst;

/**
 * 联盟商店购买
 * 
 * @author Jesse
 *
 */
public class GuildShopBuyInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 道具Id */
	private int itemId;

	/** 道具数量 */
	private int count;

	/** 商品配置 */
	private GuildShopCfg cfg;

	/** 协议Id */
	private int hpCode;

	public GuildShopBuyInvoker(Player player, int itemId, int count, GuildShopCfg cfg, ConsumeItems consume, int hpCode) {
		this.player = player;
		this.itemId = itemId;
		this.count = count;
		this.cfg = cfg;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		// 刷新联盟商店商品数量
		GuildInfoObject guildObject = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
		guildObject.updateGuildShopItem(itemId, -count);

		// 记录商店日志
		GuildService.getInstance().addGuildShopLog(player, itemId, count, cfg.getPrice() * count, GuildConst.SHOP_LOG_TYPE_BUY);
		// 返回商品信息
		HPGetGuildShopInfoResp.Builder builder = GuildService.getInstance().buildShopInfo(player, cfg);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SHOP_BUY_S_VALUE, builder));
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

	public GuildShopCfg getCfg() {
		return cfg;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
