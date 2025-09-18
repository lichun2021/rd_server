package com.hawk.game.tsssdk.invoker;

import java.util.Collection;

import org.hawk.msg.HawkMsgManager;
import org.hawk.xid.HawkXID;

import com.hawk.game.invoker.SendGuildMailMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.LogConst.SnsType;

@Category(scene = GameMsgCategory.SEND_GUILD_MAIL)
public class SendGuildMailInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String message, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}

		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(player.getGuildId());
		for (String memId : memberIds) {
			// 发送聊天室邮件
			if (RelationService.getInstance().isBlacklist(memId, player.getId())) {
				continue;
			}
			HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, memId);
			HawkMsgManager.getInstance().msgCall(MsgId.SEND_CHAT, xid, new SendGuildMailMsgInvoker(player, memId, message));
		}

		LogUtil.logChatInfo(player, "", SnsType.GUILD_ALL_MEMBER_MAIL, message, 0);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_ALL_MEMBER_MAIL, "", message);

		player.responseSuccess(protocol);
		
		return 0;
	}

}
