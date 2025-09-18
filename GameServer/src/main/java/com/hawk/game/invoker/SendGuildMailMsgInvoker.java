package com.hawk.game.invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.global.GlobalData;
import com.hawk.game.module.PlayerMailModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.RedisMail.ChatType;
import com.hawk.game.service.MailService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.mail.PersonalMailService;
import com.hawk.game.util.GsConst;

public class SendGuildMailMsgInvoker extends HawkMsgInvoker {
	public final static String GMSG1V1 = "（对全体发送）";
	private Player player;
	private String memId;
	private String message;

	public SendGuildMailMsgInvoker(Player player, String memId, String message) {
		this.player = player;
		this.memId = memId;
		this.message = message;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		Player toPlayer = GlobalData.getInstance().makesurePlayer(memId);
		if (null == toPlayer) {
			return true;
		}

		List<Player> members = new ArrayList<>(2);
		members.add(player);
		// A邮件, guildid-playerId 特殊类型
		// B邮件, guildid -pid-mid
		PlayerMailModule mailModule = player.getModule(GsConst.ModuleType.MAIL_MODULE);
		String roomId = PersonalMailService.getInstance().chatRoomId1V1(player.getId(), toPlayer.getId());
		if (Objects.equals(player.getId(), toPlayer.getId())) {// 发送A邮件
			if (!PersonalMailService.getInstance().sendChatRoomMsg(player,roomId, message, HP.code.MAIL_SEND_GUILD_MAIL_C_VALUE, 0)) {
				MailService.getInstance().createChatRoom(player, members, message, ChatType.GUILD_MAIL_SELF, roomId);
			}
		} else {// B邮件
			members.add(toPlayer);
			if (!PersonalMailService.getInstance().sendChatRoomMsg(player,roomId, message, HP.code.MAIL_SEND_GUILD_MAIL_C_VALUE, 0)) {
				MailService.getInstance().createChatRoom(player, members, message, ChatType.P2P, roomId);
				PushService.getInstance().pushMsg(memId, PushMsgType.ALLIANCE_NEW_MSG_VALUE, player.getName());
			}
		}

		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getMemId() {
		return memId;
	}

	public String getMessage() {
		return message;
	}

}
