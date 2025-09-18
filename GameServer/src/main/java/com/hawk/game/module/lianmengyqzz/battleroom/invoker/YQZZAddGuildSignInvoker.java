package com.hawk.game.module.lianmengyqzz.battleroom.invoker;

import org.hawk.os.HawkException;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.invoker.TsssdkInvoker;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.YQZZ_ADD_GUILD_SIGN)
public class YQZZAddGuildSignInvoker implements TsssdkInvoker {

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
			GuildSign guildSign = builder.build();
			IYQZZPlayer gamer = YQZZRoomManager.getInstance().makesurePlayer(player.getId());
			int operationResult = gamer.getParent().onAddGuildSign(gamer, guildSign);
			if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
				player.responseSuccess(HP.code.GUILD_ADD_SIGN_C_VALUE);
				return 0;
			}
			player.sendError(HP.code.GUILD_ADD_SIGN_C_VALUE, operationResult, 0);
		}

		return 0;
	}

}
