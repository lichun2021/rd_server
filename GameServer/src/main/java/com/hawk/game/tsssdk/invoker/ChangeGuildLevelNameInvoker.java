package com.hawk.game.tsssdk.invoker;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.invoker.GuildChangeLvlNameInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.CHANGE_GUILD_LEVEL_NAME)
public class ChangeGuildLevelNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String info, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.Error.GUILD_LEVELNAME_ILLEGAL, 0);
		} else {
			JSONObject json = JSONObject.parseObject(callback);
			String[] names = new String[5];
			if (json.containsKey("L1")) {
				names[0] = json.getString("L1");
			}
			if (json.containsKey("L2")) {
				names[1] = json.getString("L2");
			}
			if (json.containsKey("L3")) {
				names[2] = json.getString("L3");
			}
			if (json.containsKey("L4")) {
				names[3] = json.getString("L4");
			}
			if (json.containsKey("L5")) {
				names[4] = json.getString("L5");
			}
			GuildService.getInstance().dealMsg(MsgId.GUILD_LEVEL_NAME, new GuildChangeLvlNameInvoker(player, names, info, protocol));
		}
		
		return 0;
	}
	
}
