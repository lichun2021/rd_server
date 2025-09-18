package com.hawk.game.tsssdk.invoker;

import org.apache.commons.lang.StringUtils;
import org.hawk.os.HawkTime;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.GuildManager.GuildBBSMessage;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.LogConst.SnsType;

@Category(scene = GameMsgCategory.POST_GUILD_MSG)
public class PostGuildMesssageInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String message, int protocol, String guildId) {
		if (result != 0) {
			player.sendError(protocol, Status.Error.GUILD_MESSAGE_ILLEGAL, 0);
			return 0;
		}

		GuildBBSMessage.Builder info = GuildBBSMessage.newBuilder();
		info.setIcon(player.getIcon());
		if (StringUtils.isNotEmpty(player.getPfIcon())) {
			info.setPfIcon(player.getPfIcon());
		}
		info.setPlayerId(player.getId());
		info.setPlayerName(player.getName());
		info.setTime(HawkTime.getMillisecond());
		info.setPower(player.getPower());
		info.setVipLvl(player.getVipLevel());
		info.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		if (player.hasGuild()) {
			info.setGuildTag(GuildService.getInstance().getGuildTag(player.getGuildId()));
		}

		info.setMessage(message);

		int operationResult = GuildService.getInstance().onPostGuildMessage(guildId, info);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			LogUtil.logChatInfo(player, "", SnsType.GUILD_ALL_MEMBER_MSG, message, 0);
			LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_ALL_MEMBER_MSG, "", message);
			player.responseSuccess(protocol);
			return 0;
		}

		player.sendError(protocol, operationResult, 0);
		
		return 0;
	}

}
