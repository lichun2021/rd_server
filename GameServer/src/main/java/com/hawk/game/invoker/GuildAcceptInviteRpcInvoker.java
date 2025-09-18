package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.GuildInviteMail;
import com.hawk.game.protocol.Mail.InviteState;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildOperType;
import com.hawk.log.Source;

/**
 * 接受联盟邀请
 * @author Jesse
 *
 */
public class GuildAcceptInviteRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟Id */
	private String guildId;

	/** 邀请邮件 */
	private MailLiteInfo.Builder mail;

	/** 邀请信息 */
	private GuildInviteMail.Builder builder;

	/** 协议Id */
	private int hpCode;

	public GuildAcceptInviteRpcInvoker(Player player, String guildId, MailLiteInfo.Builder mail, GuildInviteMail.Builder builder, int hpCode) {
		this.player = player;
		this.guildId = guildId;
		this.mail = mail;
		this.builder = builder;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onAcceptInvite(player.getId(), player.getName(), player.getPower(), guildId);
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int operationResult = (int) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.joinGuild(guildId, false);
			builder.setInviteState(InviteState.ACCEPTED);
			MailService.getInstance().updateMailContent(mail.build(), MailParames.newBuilder().addContents(builder).setMailId(MailId.GUILD_INVITE).build().getContent());
			
			LogUtil.logGuildFlow(player, GuildOperType.GUILD_JOIN, guildId, null);
			
			GetGuildInfoResp.Builder guildInfo = GuildService.getInstance().getGuildInfo(player.getGuildId(), true);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETINFO_S, guildInfo));

			player.getPush().syncGuildInfo();
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_ACCEPTINVITE,
					Params.valueOf("mailId", mail.getMailId()),
					Params.valueOf("guildId", builder.getGuildId()),
					Params.valueOf("guildName", builder.getGuildName()));

			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getGuildId() {
		return guildId;
	}

	public MailLiteInfo.Builder getMail() {
		return mail;
	}

	public GuildInviteMail.Builder getBuilder() {
		return builder;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
