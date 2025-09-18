package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;
/**
 * 踢出联盟成员
 * @author Jesse
 *
 */
public class GuildKickMemberInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 目标玩家Id */
	private String targetId;

	/** 协议Id */
	private int hpCode;

	public GuildKickMemberInvoker(Player player, String targetId, int hpCode) {
		this.player = player;
		this.targetId = targetId;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		String guildId = player.getGuildId();
		int operationResult = GuildService.getInstance().onKickMember(guildId, player, targetId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			//发送邮件---被逐出联盟
			String guildName = GuildService.getInstance().getGuildName(guildId);
			int icon = GuildService.getInstance().getGuildFlag(guildId);
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
	                .setPlayerId(targetId)
	                .setMailId(MailId.BE_FIRED_FROM_GUILD)
	                .addSubTitles(guildName)
	                .addContents(GameUtil.guild4MailContents(guildId))
	                .setIcon(icon)
	                .build());

			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_KICK,
									Params.valueOf("guildId", guildId), Params.valueOf("targetPlayer", targetId));

			player.responseSuccess(HP.code.GUILDMANAGER_KICK_C_VALUE);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getTargetId() {
		return targetId;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
