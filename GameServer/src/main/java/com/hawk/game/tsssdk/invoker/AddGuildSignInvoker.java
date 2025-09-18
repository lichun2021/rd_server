package com.hawk.game.tsssdk.invoker;

import org.hawk.os.HawkException;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.invoker.GuildAddSignInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.ADD_GUILD_SIGN)
public class AddGuildSignInvoker implements TsssdkInvoker {
	
	@Override
	public int invoke(Player player, int result, String info, int protocol, String builderValue) {
		if (result != 0) {
			player.sendError(protocol, Status.Error.GUILD_SIGN_CFG_ERROR, 0);
		} else {
			GuildSign.Builder builder = GuildSign.newBuilder();
			try {
				JsonFormat.merge(builderValue, builder);
			} catch (ParseException e) {
				HawkException.catchException(e);
				return 0;
			}
			player.msgCall(MsgId.ADD_GUILD_SIGN, GuildService.getInstance(), new GuildAddSignInvoker(player, builder.build()));
		}
		
		return 0;
	}

}
