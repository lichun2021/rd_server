package com.hawk.game.tsssdk.invoker;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.guild.GuildCreateObj;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.module.PlayerGuildModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.CREATE_GUILD)
public class CreateGuildInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String guildTag, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAINS_FILTER_WORD_VALUE, 0);
			return 0;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		if (player.getCityLv() < GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			consume.addConsumeInfo(PlayerAttr.GOLD, GuildConstProperty.getInstance().getCreateGuildCostGold());
			if (!consume.checkConsume(player, protocol)) {
				player.sendError(protocol, Status.Error.GOLD_NOT_ENOUGH, 0);
				return 0;
			}
		}
		
		JSONObject json = JSONObject.parseObject(callback);
		String name = json.getString("name");
		String announcement = json.getString("announce");
		GuildCreateObj obj = new GuildCreateObj(name, announcement, protocol, consume);
		
		PlayerGuildModule module = player.getModule(GsConst.ModuleType.GUILD_MODULE);
		module.tryGuildCreate(obj);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.CREATE_GUILD_NAME, "", name);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.CREATE_GUILD_ANNOUNCE, "", announcement);
		
		return 0;
	}

}
