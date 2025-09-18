package com.hawk.game.invoker;

import java.util.Collection;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.task.HawkTaskManager;

import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.PlayerLockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerLockImageMsg.LockParam;
import com.hawk.game.msg.PlayerLockImageMsg.LockType;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.GuildOperType;
/**
 * 转让联盟盟主
 * @author Jesse
 *
 */
public class GuildDemiseLeaderInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 目标玩家Id */
	private String targetId;

	/** 协议Id */
	private int hpCode;

	public GuildDemiseLeaderInvoker(Player player, String targetId, int hpCode) {
		this.player = player;
		this.targetId = targetId;
		this.hpCode = hpCode;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onDemiseLeader(player.getGuildId(), player.getId(), targetId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			
			Collection<String> memIds = GuildService.getInstance().getGuildMembers(player.getGuildId());
			GuildMemberObject target = GuildService.getInstance().getGuildMemberObject(targetId);
			int icon  = GuildService.getInstance().getGuildFlag(target.getGuildId());
			for(String memberId : memIds){
				// 发送邮件---联盟成员取代盟主（联盟全体成员）
				GuildMailService.getInstance().sendMail(MailParames.newBuilder()
	                    .setPlayerId(memberId)
	                    .setMailId(MailId.GUILD_MEM_DEMISE_LEADER)
	                    .addSubTitles(player.getName(), target.getPlayerName())
	                    .addContents(player.getName(),target.getPlayerName())
	                    .setIcon(icon)
	                    .build());
			}
			
			player.getPush().syncGuildInfo();
			player.responseSuccess(HP.code.GUILDMANAGER_DEMISELEADER_C_VALUE);

			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_DEMISELEADER,
					Params.valueOf("guildId", player.getGuildId()),
					Params.valueOf("targetPlayer", targetId));
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
			//target解锁头像
			HawkTaskManager.getInstance().postMsg(targetPlayer.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.MENGZHU));
			//source上锁头像
			HawkTaskManager.getInstance().postMsg(player.getXid(), PlayerLockImageMsg.valueOf(LockType.PLAYERSTAT, LockParam.NO_MENGZHU));
			// 记录打点日志
			LogUtil.logGuildFlow(player, GuildOperType.GUILD_DEMISE, player.getGuildId(), targetId);
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
