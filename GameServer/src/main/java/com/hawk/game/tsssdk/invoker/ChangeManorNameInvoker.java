package com.hawk.game.tsssdk.invoker;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.CHANGE_GUILD_MANOR_NAME)
public class ChangeManorNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String content, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		JSONObject json = JSONObject.parseObject(callback);
		String guildId = json.getString("guildId");
		int checkResult = json.getIntValue("checkResult");
		json.remove("guildId");
		json.remove("checkResult");
		for (String key : json.keySet()) {
			GuildManorObj manor = GuildManorService.getInstance().getManorByIdx(guildId, Integer.parseInt(key));
			if(manor != null){
				manor.changeManorName(json.getString(key));
			}
		}
		
		RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_MANOR_NAME, HawkApp.getInstance().getCurrentTime());
		if (checkResult > 0) {
			RedisProxy.getInstance().removeChangeContentCDTime(guildId, ChangeContentType.CHANGE_GUILD_MANOR_NAME);
		}
		
		//推送变化消息
		GuildManorList.Builder builder = GuildManorList.newBuilder();
		//领地哨塔列表
		GuildManorService.getInstance().makeManorBastion(builder, guildId);
		//广播消息
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
		LogUtil.logSecTalkFlow(player, null, LogMsgType.CHANGE_MANOR_NAME, "", content);
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.GUILD_MANOR, Action.CHANGE_MANOR_NAME, 
				Params.valueOf("guildId", guildId),
				Params.valueOf("nameData", content));
		player.responseSuccess(HP.code.CHANGE_MANOR_NAMES_C_VALUE);
		
		return 0;
	}

}
